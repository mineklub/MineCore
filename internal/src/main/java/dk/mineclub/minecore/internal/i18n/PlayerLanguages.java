package dk.mineclub.minecore.internal.i18n;

import dk.mineclub.minecore.internal.InternalPlugin;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/** Non-blocking reads; authoritative preferences are refreshed off the game thread. */
public final class PlayerLanguages {
    private static final long TTL = TimeUnit.SECONDS.toNanos(30);
    private static final ConcurrentHashMap<UUID, Entry> cache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, CompletableFuture<Language>> pending =
            new ConcurrentHashMap<>();

    private record Entry(Language language, long expires) {}

    private PlayerLanguages() {}

    public static Language get(UUID uuid) {
        Entry entry = cache.get(uuid);
        if (entry == null || System.nanoTime() >= entry.expires()) refresh(uuid);
        return entry == null ? Language.ENGLISH : entry.language();
    }

    public static CompletableFuture<Language> refresh(UUID uuid) {
        var result = new CompletableFuture<Language>();
        var existing = pending.putIfAbsent(uuid, result);
        if (existing != null) return existing;
        Entry before = cache.get(uuid);
        Thread.startVirtualThread(
                () -> {
                    Language language;
                    try {
                        language = InternalPlugin.getInstance().getLanguage(uuid);
                    } catch (RuntimeException error) {
                        language = before == null ? Language.ENGLISH : before.language();
                    }
                    final Language loaded = language;
                    // Never overwrite a more recent saved choice or an invalidation.
                    Entry effective =
                            cache.compute(
                                    uuid,
                                    (id, current) ->
                                            current == before
                                                    ? new Entry(loaded, System.nanoTime() + TTL)
                                                    : current);
                    pending.remove(uuid, result);
                    result.complete(effective == null ? loaded : effective.language());
                    long cutoff = System.nanoTime() - TimeUnit.MINUTES.toNanos(10);
                    cache.entrySet()
                            .removeIf(
                                    entry ->
                                            entry.getValue().expires() > 0
                                                    && entry.getValue().expires() < cutoff);
                });
        return result;
    }

    public static void remember(UUID uuid, Language language) {
        cache.put(uuid, new Entry(language, System.nanoTime() + TTL));
    }

    public static void invalidateAll() {
        cache.replaceAll((id, entry) -> new Entry(entry.language(), 0));
        cache.keySet()
                .forEach(
                        id ->
                                refresh(id)
                                        .thenRun(
                                                () -> {
                                                    Entry entry = cache.get(id);
                                                    if (entry != null && entry.expires() == 0)
                                                        refresh(id);
                                                }));
    }

    public static void forget(UUID uuid) {
        cache.remove(uuid);
    }
}

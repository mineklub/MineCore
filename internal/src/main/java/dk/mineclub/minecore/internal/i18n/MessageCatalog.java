package dk.mineclub.minecore.internal.i18n;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

/** Immutable catalogs. Rendering never consults network state or changes a global locale. */
public final class MessageCatalog {
    private final Map<Language, ConfigurationNode> catalogs;

    public MessageCatalog(InputStream english, InputStream danish) throws IOException {
        catalogs = Map.of(Language.ENGLISH, read(english), Language.DANISH, read(danish));
    }

    private static ConfigurationNode read(InputStream stream) throws IOException {
        if (stream == null) throw new IOException("Missing language catalog");
        try (var reader =
                new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return YamlConfigurationLoader.builder().source(() -> reader).build().load();
        }
    }

    public List<String> strings(Language language, String key) {
        ConfigurationNode node = catalogs.get(language).node((Object[]) key.split("\\."));
        if (node.virtual()) node = catalogs.get(Language.ENGLISH).node((Object[]) key.split("\\."));
        if (node.virtual())
            throw new IllegalArgumentException("Missing English translation: " + key);
        try {
            if (node.isList()) return node.getList(String.class, List.of());
            String value = node.getString();
            if (value == null)
                throw new IllegalArgumentException("Message is not a string: " + key);
            return List.of(value);
        } catch (org.spongepowered.configurate.serialize.SerializationException error) {
            throw new IllegalArgumentException("Invalid message: " + key, error);
        }
    }

    public List<Component> list(Language language, String key, TagResolver... args) {
        return strings(language, key).stream()
                .map(
                        text ->
                                MiniMessage.miniMessage()
                                        .deserialize(text, args)
                                        .applyFallbackStyle(TextDecoration.ITALIC.withState(false)))
                .toList();
    }

    public Component get(Language language, String key, TagResolver... args) {
        return Component.join(JoinConfiguration.newlines(), list(language, key, args));
    }
}

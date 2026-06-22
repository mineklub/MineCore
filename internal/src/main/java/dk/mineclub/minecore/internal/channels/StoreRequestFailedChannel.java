package dk.mineclub.minecore.internal.channels;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dk.mineclub.minecore.internal.InternalPlugin;
import java.time.Instant;
import java.util.UUID;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import redis.clients.jedis.JedisPubSub;

public class StoreRequestFailedChannel extends JedisPubSub {
    private final String CHANNEL = "MINECORE:REQUEST:JOIN:FAILED";
    private final InternalPlugin plugin;
    private static final Gson GSON = new Gson();

    public StoreRequestFailedChannel(InternalPlugin plugin) {
        this.plugin = plugin;
        new Thread(() -> plugin.getJedis().subscribe(this, CHANNEL)).start();
    }

    @Override
    public void onMessage(String channel, String message) {
        try {
            if (!message.trim().isEmpty() && channel.equals(CHANNEL)) {
                handleMessage(message);
            }
        } catch (Exception e) {
            plugin.getLogger()
                    .warn("Failed to handle failed-store-request message: {}", message, e);
        }
    }

    public void handleMessage(String message) {
        JsonObject json = GSON.fromJson(message, JsonObject.class);
        if (json == null || !json.has("type") || !json.has("player")) {
            return;
        }

        String type = json.get("type").getAsString();
        UUID playerUuid = UUID.fromString(json.get("player").getAsString());
        plugin.getServer()
                .getPlayer(playerUuid)
                .ifPresent(
                        player -> {
                            switch (type) {
                                case "ban" -> handleBan(player, json);
                                case "connection" ->
                                        player.sendMessage(
                                                plugin.getLang()
                                                        .get("request.accept-errors.network"));
                                default -> {}
                            }
                        });
    }

    private void handleBan(com.velocitypowered.api.proxy.Player player, JsonObject json) {
        JsonObject ban = json.getAsJsonObject("ban");
        if (ban == null) {
            return;
        }

        boolean permanent = ban.has("permanent") && ban.get("permanent").getAsBoolean();
        String reason = getStringOrDefault(ban, "reason", "Unknown");

        if (permanent) {
            player.sendMessage(
                    plugin.getLang()
                            .get("request.banned.perm", Placeholder.unparsed("reason", reason)));
            return;
        }

        String expiry = getStringOrDefault(ban, "expiry", Instant.now().toString());
        player.sendMessage(
                plugin.getLang()
                        .get(
                                "request.banned.temp",
                                Placeholder.unparsed("reason", reason),
                                Placeholder.unparsed("expiry", expiry)));
    }

    private String getStringOrDefault(JsonObject json, String key, String defaultValue) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonPrimitive()) {
            return defaultValue;
        }
        return element.getAsString();
    }
}

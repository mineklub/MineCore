package dk.mineclub.minecore.internal.channels;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dk.mineclub.minecore.internal.InternalPlugin;
import java.util.Objects;
import java.util.UUID;
import redis.clients.jedis.JedisPubSub;

public class StoreRequestTimeoutChannel extends JedisPubSub {
    private final String CHANNEL = "MINECORE:REQUEST:TIMEOUT";
    private final InternalPlugin plugin;
    private final StoreRequestMessageParser parser = new StoreRequestMessageParser();
    private static final Gson GSON = new Gson();

    public StoreRequestTimeoutChannel(InternalPlugin plugin) {
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
            e.printStackTrace();
        }
    }

    private void publishReturn(StoreRequestMessage message) {
        JsonObject object = new JsonObject();
        object.addProperty("service", message.data().service().id());
        object.addProperty("mcaccount", message.data().mcaccount().uuid());
        plugin.getJedis().publish("MINECORE:RETURN", object.toString());
    }

    private String getRequestPayload(JsonObject messageObject, String fallbackMessage) {
        JsonObject requestObject = messageObject.getAsJsonObject("data");
        if (requestObject == null) {
            return fallbackMessage;
        }

        JsonObject nestedRequestObject = requestObject.getAsJsonObject("data");
        return Objects.requireNonNullElse(nestedRequestObject, requestObject).toString();
    }

    public void handleMessage(String message) {
        JsonObject messageObject = GSON.fromJson(message, JsonObject.class);
        parser.parse(getRequestPayload(messageObject, message))
                .ifPresentOrElse(
                        parsed -> {
                            plugin.getServer()
                                    .getPlayer(UUID.fromString(parsed.data().mcaccount().uuid()))
                                    .ifPresent(
                                            player -> {
                                                player.sendMessage(
                                                        plugin.getLang().get("request.timeout"));
                                                publishReturn(parsed);
                                            });
                        },
                        () -> plugin.getLogger().warn("Received invalid store message: {}", message));
    }
}

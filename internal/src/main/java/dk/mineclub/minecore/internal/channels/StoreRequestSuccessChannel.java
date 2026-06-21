package dk.mineclub.minecore.internal.channels;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import dk.mineclub.minecore.internal.InternalPlugin;
import dk.mineclub.minecore.internal.handler.OldVersionHandler;
import java.util.UUID;
import redis.clients.jedis.JedisPubSub;

public class StoreRequestSuccessChannel extends JedisPubSub {
    private final String CHANNEL = "MINECORE:REQUEST:JOIN:SUCCESS";
    private final InternalPlugin plugin;
    private final StoreRequestMessageParser parser = new StoreRequestMessageParser();
    private static final Gson GSON = new Gson();

    public StoreRequestSuccessChannel(InternalPlugin plugin) {
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

    public void handleMessage(String message) {
        JsonObject messageObject = GSON.fromJson(message, JsonObject.class);
        System.out.println(messageObject);
        parser.parse(
                        messageObject
                                .get("request")
                                .getAsJsonObject()
                                .getAsJsonObject("request")
                                .toString())
                .ifPresentOrElse(
                        parsed -> {
                            Player player =
                                    plugin.getServer()
                                            .getPlayer(
                                                    UUID.fromString(
                                                            parsed.data().mcaccount().uuid()))
                                            .orElse(null);
                            if (player == null) {
                                return;
                            }

                            if (player.getProtocolVersion()
                                            .compareTo(ProtocolVersion.MINECRAFT_1_21_6)
                                    < 0) {
                                OldVersionHandler.handle(plugin, player, parsed);
                            } else {
                                OldVersionHandler.handle(plugin, player, parsed);
                            }
                        },
                        () -> System.out.println("Received invalid store message: " + message));
    }
}

package dk.mineclub.minecore.internal.channels;

import dk.mineclub.minecore.internal.InternalPlugin;
import redis.clients.jedis.JedisPubSub;

public class StoreRequestChannel extends JedisPubSub {
    private final String CHANNEL = "MINECORE:REQUEST";
    private final InternalPlugin plugin;

    public StoreRequestChannel(InternalPlugin plugin) {
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
        // Handle the message here
    }
}

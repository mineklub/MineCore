package dk.mineclub.minecore.internal.channels;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dk.mineclub.minecore.internal.InternalPlugin;
import redis.clients.jedis.JedisPubSub;

public class StoreRequestFailedChannel extends JedisPubSub {
	private final String CHANNEL = "MINECORE:REQUEST:JOIN:FAILED";
	private final InternalPlugin plugin;
	private final StoreRequestMessageParser parser = new StoreRequestMessageParser();
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
			e.printStackTrace();
		}
	}

	public void handleMessage(String message) {
		parser.parse(message)
			.ifPresentOrElse(
				parsed -> {
					// Cancel the request
				},
				() -> System.out.println("Received invalid store message: " + message));
	}
}

package dk.mineclub.minecore.platform.paper;

import dk.mineclub.minecore.api.MineCoreApi;
import dk.mineclub.minecore.platform.paper.bridge.PaperEventBridge;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

/** Main entry point for the Paper platform module. */
public class MineCorePaperPlugin extends JavaPlugin {
    @Getter private MineCoreApi api;
    private PaperEventBridge eventBridge;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String configToken = normalize(getConfig().getString("token", null));
        String envToken = normalize(System.getenv("TOKEN"));
        String token = configToken != null && configToken.length() > 1 ? configToken : envToken;

        this.api = new MineCoreApi(null, token);
        this.eventBridge = new PaperEventBridge(this);
        this.api.getAsyncEventBus().register(eventBridge);

        getLogger().info("MineCore Paper plugin enabled");
    }

    @Override
    public void onDisable() {
        if (api != null && eventBridge != null) {
            try {
                api.getAsyncEventBus().unregister(eventBridge);
            } catch (IllegalArgumentException ignored) {
                // Listener was not registered or was already removed.
            }
        }

        if (api != null) {
            api.shutdown();
        }

        eventBridge = null;
        api = null;
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}

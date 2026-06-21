package dk.mineclub.minecore.platform.common;

import dk.mineclub.minecore.api.MineCoreApi;
import dk.mineclub.minecore.platform.common.bridge.CommonEventBridge;
import dk.mineclub.minecore.platform.common.hooks.CommonHookLoader;
import org.bukkit.plugin.java.JavaPlugin;

/** Shared plugin lifecycle for Bukkit-compatible platform modules. */
public abstract class BaseMineCorePlugin extends JavaPlugin {
    private MineCoreApi api;
    private CommonEventBridge eventBridge;
    private CommonHookLoader hookLoader;

    protected abstract String platformName();

    public MineCoreApi getApi() {
        return api;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String configToken = normalize(getConfig().getString("token", null));
        String envToken = normalize(System.getenv("TOKEN"));
        String token = configToken != null && configToken.length() > 1 ? configToken : envToken;

        this.api = new MineCoreApi(null, token);
        this.eventBridge = new CommonEventBridge(this);
        this.api.getAsyncEventBus().register(eventBridge);
        this.hookLoader = new CommonHookLoader(this);
        this.hookLoader.enable();

        getLogger().info("MineCore " + platformName() + " plugin enabled");
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

        if (hookLoader != null) {
            hookLoader.disable();
        }

        if (api != null) {
            api.shutdown();
        }

        eventBridge = null;
        hookLoader = null;
        api = null;
    }

    private static String normalize(String value) {
        return value == null || value.trim().isEmpty() ? null : value;
    }
}

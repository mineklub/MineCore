package dk.mineclub.minecore.platform.common;

import dk.mineclub.minecore.api.MineCoreApi;
import dk.mineclub.minecore.platform.common.bridge.CommonEventBridge;
import dk.mineclub.minecore.platform.common.hooks.CommonHookLoader;
import dk.mineclub.minecore.platform.common.hooks.SkriptHookDownloader;
import dk.mineclub.minecore.platform.common.staff.StaffCommandBridge;
import dk.mineclub.minecore.platform.common.staff.StaffFeedListener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class BaseMineCorePlugin extends JavaPlugin {
    private MineCoreApi api;
    private CommonEventBridge eventBridge;
    private CommonHookLoader hookLoader;

    protected abstract String platformName();

    protected abstract SkriptHookDownloader createSkriptDownloader();

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

        boolean autoInstallSkript = getConfig().getBoolean("hooks.auto-install-skript", true);
        this.hookLoader = new CommonHookLoader(this, createSkriptDownloader());
        this.hookLoader.enable(autoInstallSkript);

        getServer().getPluginManager().registerEvents(new StaffFeedListener(api), this);
        StaffCommandBridge.register(this, api);

        getLogger().info("MineCore " + platformName() + " plugin enabled");
    }

    @Override
    public void onDisable() {
        if (api != null && eventBridge != null) {
            try {
                api.getAsyncEventBus().unregister(eventBridge);
            } catch (IllegalArgumentException ignored) {
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

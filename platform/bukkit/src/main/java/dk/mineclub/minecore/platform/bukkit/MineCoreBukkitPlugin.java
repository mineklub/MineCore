package dk.mineclub.minecore.platform.bukkit;

import dk.mineclub.minecore.platform.common.BaseMineCorePlugin;
import dk.mineclub.minecore.platform.common.hooks.SkriptHookDownloader;

/** Main entry point for the Bukkit platform module. */
public class MineCoreBukkitPlugin extends BaseMineCorePlugin {
    @Override
    protected String platformName() {
        return "Bukkit";
    }

    @Override
    protected SkriptHookDownloader createSkriptDownloader() {
        return new BukkitSkriptHookDownloader(this);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (getApi() == null) {
            return;
        }

        getServer().getPluginManager().registerEvents(new StaffFeedListener(getApi()), this);
        StaffCommandBridge.register(this, getApi());
    }
}

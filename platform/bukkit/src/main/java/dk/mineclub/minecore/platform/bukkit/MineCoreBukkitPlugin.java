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
}

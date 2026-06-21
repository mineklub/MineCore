package dk.mineclub.minecore.platform.bukkit;

import dk.mineclub.minecore.platform.common.BaseMineCorePlugin;

/** Main entry point for the Bukkit platform module. */
public class MineCoreBukkitPlugin extends BaseMineCorePlugin {
    @Override
    protected String platformName() {
        return "Bukkit";
    }
}

package dk.mineclub.minecore.hooks.skript;

import org.bukkit.plugin.java.JavaPlugin;

/** Handles optional Skript integration for MineCore. */
public class SkriptHook implements Hook {
    private boolean enabled = false;

    @Override
    public String name() {
        return "Skript";
    }

    @Override
    public void enable(JavaPlugin plugin) {
        if (enabled) {
            return;
        }

        if (plugin.getServer().getPluginManager().getPlugin("Skript") == null) {
            plugin.getLogger().warning("Skript is not installed; skipping MineCore Skript hook");
            return;
        }

        try {
            SkriptRegistrations.register();
            enabled = true;
            plugin.getLogger().info("MineCore Skript hook enabled");
        } catch (Exception ex) {
            plugin.getLogger()
                    .warning("Failed to register MineCore Skript hooks: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void disable() {
        enabled = false;
    }
}

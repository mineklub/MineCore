package dk.mineclub.minecore.hooks.skript;

import ch.njol.skript.Skript;
import org.bukkit.plugin.java.JavaPlugin;
import org.skriptlang.skript.addon.SkriptAddon;

/** Handles optional Skript integration for MineCore. */
public class SkriptHook implements Hook {
    private SkriptAddon addon;

    @Override
    public String name() {
        return "Skript";
    }

    @Override
    public void enable(JavaPlugin plugin) {
        if (addon != null) {
            return;
        }

        if (plugin.getServer().getPluginManager().getPlugin("Skript") == null) {
            plugin.getLogger().warning("Skript is not installed; skipping MineCore Skript hook");
            return;
        }

        // Use the owning plugin class so Paper can resolve the providing plugin classloader.
        addon = Skript.instance().registerAddon(plugin.getClass(), "MineCore");
        SkriptRegistrations.register(addon);
    }

    @Override
    public void disable() {
        addon = null;
    }
}

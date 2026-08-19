package dk.mineclub.minecore.platform.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("deprecation")
/** Finds Skript plugin version using Bukkit's plugin manager. */
public final class BukkitSkriptVersionFinder {

    private BukkitSkriptVersionFinder() {}

    /**
     * Finds the Skript plugin version using Bukkit's plugin manager.
     *
     * @param plugin the plugin instance for logging
     * @return the Skript version (e.g., "2.14.3") or null if not found
     */
    public static String findSkriptPluginVersion(Plugin plugin) {
        try {
            Plugin skriptPlugin = Bukkit.getPluginManager().getPlugin("Skript");

            if (skriptPlugin != null) {
                String version = skriptPlugin.getDescription().getVersion();
                plugin.getLogger()
                        .info(
                                "Found Skript plugin version: "
                                        + version
                                        + ". Selecting compatible hook...");
                return version;
            }
        } catch (Exception ignored) {
            // Continue without version detection
        }
        return null;
    }
}

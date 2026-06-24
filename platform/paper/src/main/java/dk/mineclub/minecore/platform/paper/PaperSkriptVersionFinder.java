package dk.mineclub.minecore.platform.paper;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/** Finds Skript plugin version using Paper's PluginMeta API. */
public final class PaperSkriptVersionFinder {

    private PaperSkriptVersionFinder() {}

    /**
     * Finds the Skript plugin version using Paper's PluginMeta API.
     *
     * @param plugin the plugin instance for logging
     * @return the Skript version (e.g., "2.14.3") or null if not found
     */
    public static String findSkriptPluginVersion(Plugin plugin) {
        try {
            String version =
                    Bukkit.getPluginManager().getPlugin("Skript").getPluginMeta().getVersion();
            plugin.getLogger()
                    .info(
                            "Found Skript plugin version: "
                                    + version
                                    + ". Selecting compatible hook...");
            return version;
        } catch (Exception ignored) {
            // Continue without version detection
        }
        return null;
    }
}

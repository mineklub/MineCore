package dk.mineclub.minecore.platform.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("deprecation")
public final class BukkitSkriptVersionFinder {

    private BukkitSkriptVersionFinder() {}

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
        }
        return null;
    }
}

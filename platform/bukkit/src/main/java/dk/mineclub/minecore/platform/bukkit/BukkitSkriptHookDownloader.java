package dk.mineclub.minecore.platform.bukkit;

import dk.mineclub.minecore.platform.common.hooks.SkriptHookDownloader;
import java.nio.file.Path;
import org.bukkit.plugin.Plugin;

/** Bukkit implementation of SkriptHookDownloader using standard plugin manager. */
public final class BukkitSkriptHookDownloader extends SkriptHookDownloader {

    public BukkitSkriptHookDownloader(Plugin plugin) {
        super(plugin);
    }

    @Override
    protected String findSkriptPluginVersion(Path hooksDir) {
        return BukkitSkriptVersionFinder.findSkriptPluginVersion(plugin);
    }
}

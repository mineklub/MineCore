package dk.mineclub.minecore.platform.paper;

import dk.mineclub.minecore.platform.common.hooks.SkriptHookDownloader;
import java.nio.file.Path;
import org.bukkit.plugin.Plugin;

/** Paper implementation of SkriptHookDownloader using Paper's PluginMeta API. */
public final class PaperSkriptHookDownloader extends SkriptHookDownloader {

    public PaperSkriptHookDownloader(Plugin plugin) {
        super(plugin);
    }

    @Override
    protected String findSkriptPluginVersion(Path hooksDir) {
        return PaperSkriptVersionFinder.findSkriptPluginVersion(plugin);
    }
}

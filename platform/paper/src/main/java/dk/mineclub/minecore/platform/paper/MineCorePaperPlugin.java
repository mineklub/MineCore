package dk.mineclub.minecore.platform.paper;

import dk.mineclub.minecore.platform.common.BaseMineCorePlugin;
import dk.mineclub.minecore.platform.common.hooks.SkriptHookDownloader;

/** Main entry point for the Paper platform module. */
public class MineCorePaperPlugin extends BaseMineCorePlugin {
    @Override
    protected String platformName() {
        return "Paper";
    }

    @Override
    protected SkriptHookDownloader createSkriptDownloader() {
        return new PaperSkriptHookDownloader(this);
    }
}

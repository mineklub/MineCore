package dk.mineclub.minecore.hooks.skript;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

public final class HookBootstrap {
    private final List<Hook> hooks = new ArrayList<>();

    public void enable(JavaPlugin plugin) {
        hooks.clear();
        hooks.add(new SkriptHook());

        for (Hook hook : hooks) {
            try {
                hook.enable(plugin);
                plugin.getLogger().info("Enabled hook: " + hook.name());
            } catch (Exception ex) {
                plugin.getLogger()
                        .warning("Failed to enable hook " + hook.name() + ": " + ex.getMessage());
            }
        }
    }

    public void disable() {
        for (int i = hooks.size() - 1; i >= 0; i--) {
            try {
                hooks.get(i).disable();
            } catch (Exception ignored) {
                // Individual hooks should not block shutdown.
            }
        }

        hooks.clear();
    }
}

package dk.mineclub.minecore.hooks;

import org.bukkit.plugin.java.JavaPlugin;

public interface Hook {
    String name();

    void enable(JavaPlugin plugin);

    void disable();
}

package dk.mineclub.minecore.hooks.skript;

import org.bukkit.plugin.java.JavaPlugin;

interface Hook {
    String name();

    void enable(JavaPlugin plugin);

    void disable();
}

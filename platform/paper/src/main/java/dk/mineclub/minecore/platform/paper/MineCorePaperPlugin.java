package dk.mineclub.minecore.platform.paper;

import dk.mineclub.minecore.api.service.SocketGatewayService;
import java.util.Arrays;

import dk.mineclub.minecore.common.manager.MinecoreRequestManager;
import org.bukkit.plugin.java.JavaPlugin;

/** Main entry point for the Paper platform module. */
public class MineCorePaperPlugin extends JavaPlugin {
    private static final String ENV_TOKEN = "TOCKET";

    private SocketGatewayService socketGatewayService;
    private MinecoreRequestManager requestManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        boolean enabled = getConfig().getBoolean("socket.enabled", true);
        if (!enabled) {
            getLogger().info("Socket listener is disabled by config.");
            return;
        }

        String socketToken = System.getenv(ENV_TOKEN);

        try {
            socketGatewayService = new SocketGatewayService(socketToken);
            socketGatewayService.registerDefaultLogs();
            socketGatewayService.connect();
            getLogger().info("MineCore Paper socket listener enabled for event: " + socketEvent);
        } catch (IllegalArgumentException ex) {
            getLogger().severe("Failed to initialize socket listener: " + ex.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (socketGatewayService != null && socketGatewayService.isConnected()) {
            socketGatewayService.disconnect();
            getLogger().info("MineCore Paper socket listener disconnected.");
        }

        getLogger().info("MineCore Paper platform disabled.");
    }
}

package dk.mineclub.minecore.platform.paper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dk.mineclub.minecore.api.service.SocketGatewayService;
import dk.mineclub.minecore.common.manager.MinecoreRequestManager;
import org.bukkit.plugin.java.JavaPlugin;

/** Main entry point for the Paper platform module. */
public class MineCorePaperPlugin extends JavaPlugin {
    private static final String ENV_SOCKET_TOKEN = "MINECORE_SOCKET_TOKEN";

    private final Gson gson = new GsonBuilder().serializeNulls().create();

    private SocketGatewayService socketGatewayService;
    private MinecoreRequestManager requestManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        requestManager = new MinecoreRequestManager();

        boolean enabled = getConfig().getBoolean("socket.enabled", true);
        if (!enabled) {
            getLogger().info("Socket listener is disabled by config.");
            return;
        }

        String socketToken = resolveSetting();

        try {
            socketGatewayService = new SocketGatewayService(socketToken);
            socketGatewayService.registerDefaultLogs();
            socketGatewayService.connect();
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

    private String resolveSetting() {
        String configValue = getConfig().getString("socket.token", "");
        String envValue = System.getenv(MineCorePaperPlugin.ENV_SOCKET_TOKEN);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue.trim();
        }

        return configValue.trim();
    }
}

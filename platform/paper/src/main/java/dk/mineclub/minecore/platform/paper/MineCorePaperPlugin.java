package dk.mineclub.minecore.platform.paper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dk.mineclub.minecore.api.service.SocketGatewayService;
import dk.mineclub.minecore.common.dto.CreateMinecoreRequestDto;
import dk.mineclub.minecore.common.manager.MinecoreRequestManager;
import java.io.IOException;
import java.util.Arrays;
import org.bukkit.plugin.java.JavaPlugin;

/** Main entry point for the Paper platform module. */
public class MineCorePaperPlugin extends JavaPlugin {
    private static final String ENV_SOCKET_URL = "MINECORE_SOCKET_URL";
    private static final String ENV_SOCKET_TOKEN = "MINECORE_SOCKET_TOKEN";
    private static final String ENV_SOCKET_EVENT = "MINECORE_SOCKET_EVENT";

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

        String socketUrl = resolveSetting("socket.url", ENV_SOCKET_URL, "https://api.mineclub.dk");
        String socketToken = resolveSetting("socket.token", ENV_SOCKET_TOKEN, "");
        String socketEvent = resolveSetting("socket.event", ENV_SOCKET_EVENT, "store_request");

        if (socketUrl == null || socketUrl.trim().isEmpty()) {
            getLogger().warning(
                    "Socket URL is empty; set socket.url or MINECORE_SOCKET_URL to enable socket startup.");
            return;
        }

        try {
            socketGatewayService = new SocketGatewayService(socketUrl, socketToken);
            socketGatewayService.registerDefaultLogs();
            socketGatewayService.connect();
            getLogger().info(
                    "MineCore Paper socket listener enabled for event: "
                            + socketEvent
                            + " at "
                            + socketUrl);
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

    private String resolveSetting(String configPath, String envKey, String defaultValue) {
        String configValue = getConfig().getString(configPath, defaultValue);
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue.trim();
        }

        return configValue == null ? null : configValue.trim();
    }
}

package dk.mineclub.minecore.api.service;

import dk.mineclub.minecore.common.socket.SocketIoClientManager;
import io.socket.emitter.Emitter;
import java.util.function.Consumer;

/**
 * API-facing wrapper around {@link SocketIoClientManager}.
 *
 * <p>Use this service in API handlers/controllers to listen for socket events without directly
 * depending on lower-level socket configuration code.
 */
@SuppressWarnings("unused")
public class SocketGatewayService {

    private static final String DEFAULT_SERVER_URL = "https://api.mineclub.dk";

    private final SocketIoClientManager socketClientManager;

    /**
     * Creates a socket gateway service.
     *
     * @param bearerToken optional bearer token
     */
    @SuppressWarnings("unused")
    public SocketGatewayService(String bearerToken) {
        this(DEFAULT_SERVER_URL, bearerToken);
    }

    /**
     * Creates a socket gateway service for a specific server URL.
     *
     * @param serverUrl socket.io server URL
     * @param bearerToken optional bearer token
     */
    public SocketGatewayService(String serverUrl, String bearerToken) {
        this.socketClientManager = new SocketIoClientManager(serverUrl, bearerToken);
    }

    /** Connects to socket server. */
    public void connect() {
        socketClientManager.connect();
    }

    /** Disconnects from socket server. */
    public void disconnect() {
        socketClientManager.disconnect();
    }

    /**
     * Registers event listener.
     *
     * @param event event name
     * @param listener callback
     */
    public void on(String event, Emitter.Listener listener) {
        socketClientManager.on(event, listener);
    }

    /**
     * Registers event listener without exposing socket.io listener types to callers.
     *
     * @param event event name
     * @param listener callback that receives event arguments
     */
    public void onEvent(String event, Consumer<Object[]> listener) {
        on(event, args -> listener.accept(args));
    }

    /**
     * Returns connection status.
     *
     * @return true if connected
     */
    public boolean isConnected() {
        return socketClientManager.isConnected();
    }

    /** Registers default lifecycle logs to aid integration/debugging. */
    public void registerDefaultLogs() {
        socketClientManager.registerDefaultLogs();
    }
}

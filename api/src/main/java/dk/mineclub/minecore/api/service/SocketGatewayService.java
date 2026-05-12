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
public class SocketGatewayService {

    private final SocketIoClientManager socketClientManager;

    /**
     * Creates a socket gateway service.
     *
     * @param bearerToken optional bearer token
     */
    public SocketGatewayService(String bearerToken) {
        this.socketClientManager = new SocketIoClientManager("https://api.mineclub.dk", bearerToken);
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
        socketClientManager.on(
                event,
                new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        listener.accept(args);
                    }
                });
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

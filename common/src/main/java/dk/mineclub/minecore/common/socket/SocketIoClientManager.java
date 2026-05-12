package dk.mineclub.minecore.common.socket;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Lightweight Socket.IO client helper for Minecore modules.
 *
 * <p>This manager wraps common operations (connect, disconnect, on) behind a small API that can be
 * reused across modules.
 */
public class SocketIoClientManager {

    private static final Logger LOGGER = Logger.getLogger(SocketIoClientManager.class.getName());

    private final Socket socket;

    /**
     * Creates a Socket.IO client manager.
     *
     * @param serverUrl socket.io server URL (for example: https://api.mineclub.dk)
     * @throws IllegalArgumentException if the URL is invalid
     */
    public SocketIoClientManager(String serverUrl) {
        this(serverUrl, null);
    }

    /**
     * Creates a Socket.IO client manager with optional bearer auth token.
     *
     * @param serverUrl socket.io server URL
     * @param bearerToken optional bearer token, may be null or blank
     * @throws IllegalArgumentException if the URL is invalid
     */
    public SocketIoClientManager(String serverUrl, String bearerToken) {
        try {
            IO.Options options = new IO.Options();
            options.reconnection = true;
            options.forceNew = false;
            options.timeout = 10_000;

            if (bearerToken != null && !bearerToken.trim().isEmpty()) {
                Map<String, java.util.List<String>> headers =
                        new HashMap<String, java.util.List<String>>();
                headers.put("Authorization", Collections.singletonList("Bearer " + bearerToken));
                options.extraHeaders = headers;
            }

            this.socket = IO.socket(serverUrl, options);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid socket server URL: " + serverUrl, e);
        }
    }

    /** Connects the socket client. */
    public void connect() {
        socket.connect();
    }

    /** Disconnects the socket client if connected. */
    public void disconnect() {
        socket.disconnect();
    }

    /**
     * Adds an event listener.
     *
     * @param event event name
     * @param listener event callback
     */
    public void on(String event, Emitter.Listener listener) {
        socket.on(event, listener);
    }

    /**
     * Removes all listeners for an event.
     *
     * @param event event name
     */
    public void off(String event) {
        socket.off(event);
    }

    /**
     * Returns whether the socket is currently connected.
     *
     * @return true when connected
     */
    public boolean isConnected() {
        return socket.connected();
    }

    /**
     * Waits for a connection event for up to the provided timeout.
     *
     * @param timeout timeout value
     * @param unit timeout unit
     * @return true if connected before timeout
     * @throws InterruptedException if the calling thread is interrupted while waiting
     */
    public boolean awaitConnected(long timeout, TimeUnit unit) throws InterruptedException {
        if (socket.connected()) {
            return true;
        }

        final CountDownLatch latch = new CountDownLatch(1);
        Emitter.Listener onConnect =
                new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        latch.countDown();
                    }
                };

        socket.on(Socket.EVENT_CONNECT, onConnect);
        try {
            return latch.await(timeout, unit);
        } finally {
            socket.off(Socket.EVENT_CONNECT, onConnect);
        }
    }

    /**
     * Registers baseline lifecycle logs (connect/disconnect/connect_error).
     *
     * <p>Useful for debugging while integrating with a new backend.
     */
    public void registerDefaultLogs() {
        on(
                Socket.EVENT_CONNECT,
                new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        LOGGER.info("Socket connected");
                    }
                });

        on(
                Socket.EVENT_DISCONNECT,
                new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        LOGGER.info("Socket disconnected");
                    }
                });

        on(
                Socket.EVENT_CONNECT_ERROR,
                new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        String message =
                                (args != null && args.length > 0)
                                        ? String.valueOf(args[0])
                                        : "unknown";
                        LOGGER.warning("Socket connect error: " + message);
                    }
                });
    }
}

package dk.mineclub.minecore.api.manager;

import dk.mineclub.minecore.api.MineCoreApi;
import dk.mineclub.minecore.api.events.ReceiveRequestEvent;
import dk.mineclub.minecore.api.events.ReceiveVoteEvent;
import dk.mineclub.minecore.api.model.MappedVote;
import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import io.socket.client.IO;
import io.socket.client.Socket;
import java.net.URI;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

public class SocketIOManager {
    private static final Logger LOGGER = Logger.getLogger(SocketIOManager.class.getName());

    private final MineCoreApi mineCoreApi;
    @Getter private @Nullable Socket socket;
    private final String socketUrl;
    private boolean isConnected = false;

    public SocketIOManager(MineCoreApi mineCoreApi) {
        this(mineCoreApi, System.getenv("SOCKET_IO_URL"));
    }

    public SocketIOManager(MineCoreApi mineCoreApi, String socketUrl) {
        this.mineCoreApi = mineCoreApi;
        this.socketUrl = socketUrl != null ? socketUrl : "https://api.mineclub.dk";
    }

    /** Connects to the Socket.IO server with authentication token from environment */
    public void connect() {
        String token = System.getenv("TOKEN");
        connect(token);
    }

    /**
     * Connects to the Socket.IO server with authentication token
     *
     * @param token the authentication token
     */
    public void connect(@Nullable String token) {
        if (isConnected && socket != null) {
            return;
        }

        try {
            IO.Options options = new IO.Options();
            options.reconnection = true;
            options.reconnectionAttempts = 5;
            options.reconnectionDelay = 1000;

            if (token != null) {
                options.auth =
                        new HashMap<String, String>() {
                            {
                                put("token", token);
                            }
                        };
            }

            socket = IO.socket(URI.create(socketUrl), options);

            setupDefaultListeners();
            Socket currentSocket = socket;
            if (currentSocket != null) {
                currentSocket.connect();
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to connect to Socket.IO server", ex);
        }
    }

    public void disconnect() {
        if (socket != null) {
            socket.disconnect();
            socket.close();
            socket = null;
        }

        isConnected = false;
    }

    /** Sets up default listeners for connection events */
    private void setupDefaultListeners() {
        if (socket == null) {
            return;
        }

        socket.on(
                Socket.EVENT_CONNECT,
                ignored -> {
                    isConnected = true;
                    System.out.println("Socket.IO connected");
                });

        socket.on(
                Socket.EVENT_DISCONNECT,
                ignored -> {
                    isConnected = false;
                    System.out.println("Socket.IO disconnected");
                });

        socket.on(
                Socket.EVENT_CONNECT_ERROR,
                args -> {
                    if (args.length > 0) {
                        System.out.println("Socket.IO connection error: " + args[0]);
                    } else {
                        System.out.println("Socket.IO connection error");
                    }
                });

        socket.on(
                "request",
                args -> {
                    if (args.length == 0 || args[0] == null) {
                        System.out.println("Socket.IO request event received without payload");
                        return;
                    }

                    try {
                        RequestEnvelope envelope =
                                mineCoreApi
                                        .getGson()
                                        .fromJson(String.valueOf(args[0]), RequestEnvelope.class);

                        if (envelope == null || envelope.data == null) {
                            System.out.println("Socket.IO request event payload was empty");
                            return;
                        }

                        if (envelope.type != null
                                && !"accept".equalsIgnoreCase(envelope.type)
                                && !"cancel".equalsIgnoreCase(envelope.type)) {
                            System.out.println(
                                    "Socket.IO request event ignored for type: " + envelope.type);
                            return;
                        }

                        new ReceiveRequestEvent(envelope.type, envelope.data).callEvent();
                    } catch (Exception ex) {
                        System.out.println(
                                "Failed to parse Socket.IO request event: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });
        socket.on(
                "vote",
                args -> {
                    if (args.length == 0 || args[0] == null) {
                        System.out.println("Socket.IO vote event received without payload");
                        return;
                    }

                    try {
                        VoteEnvelope envelope =
                                mineCoreApi
                                        .getGson()
                                        .fromJson(String.valueOf(args[0]), VoteEnvelope.class);

                        if (envelope == null || envelope.data == null) {
                            System.out.println("Socket.IO vote event payload was empty");
                            return;
                        }

                        if (envelope.type != null && !"newVote".equalsIgnoreCase(envelope.type)) {
                            System.out.println(
                                    "Socket.IO vote event ignored for type: " + envelope.type);
                            return;
                        }

                        new ReceiveVoteEvent(envelope.data).callEvent();
                    } catch (Exception ex) {
                        System.out.println(
                                "Failed to parse Socket.IO vote event: " + ex.getMessage());
                    }
                });
    }

    @SuppressWarnings("unused")
    private static final class RequestEnvelope {
        private String type;
        private StoreCreatedRequest data;
    }

    @SuppressWarnings("unused")
    private static final class VoteEnvelope {
        private String type;
        private MappedVote data;
    }
}

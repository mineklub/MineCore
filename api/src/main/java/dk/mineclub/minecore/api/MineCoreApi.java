package dk.mineclub.minecore.api;

import com.google.common.eventbus.AsyncEventBus;
import com.google.gson.Gson;
import dk.mineclub.minecore.api.events.ReceiveRequestEvent;
import dk.mineclub.minecore.api.manager.RequestManager;
import dk.mineclub.minecore.api.manager.SocketIOManager;
import dk.mineclub.minecore.api.model.GetRequestsOptions;
import dk.mineclub.minecore.api.model.GetRequestsResponse;
import dk.mineclub.minecore.api.model.MappedRequest;
import dk.mineclub.minecore.api.model.RequestStatusQuery;
import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

public final class MineCoreApi {
    @Getter private static MineCoreApi instance;
    @Getter private final AsyncEventBus asyncEventBus;
    @Getter private final RequestManager minecoreRequestManager;
    @Getter private final SocketIOManager socketIOManager;
    @Getter private final Gson gson = new Gson();
    @Getter private final String token;
    private final ScheduledExecutorService requestPoller;

    public MineCoreApi() {
        this(System.getenv("SOCKET_IO_URL"), System.getenv("TOKEN"));
    }

    public MineCoreApi(@Nullable String socketUrl, @Nullable String token) {
        this(socketUrl, token, true);
    }

    public MineCoreApi(@Nullable String socketUrl, @Nullable String token, boolean connectSocket) {
        instance = this;
        this.token = token;
        ExecutorService executor = Executors.newCachedThreadPool();
        this.asyncEventBus = new AsyncEventBus(executor);
        this.minecoreRequestManager = new RequestManager(this);
        this.socketIOManager = new SocketIOManager(this, socketUrl);
        this.requestPoller = Executors.newSingleThreadScheduledExecutor();
        if (connectSocket) {
            this.socketIOManager.connect(token);
        }
        startRequestPolling();
    }

    public void shutdown() {
        socketIOManager.disconnect();
        requestPoller.shutdownNow();
    }

    private void startRequestPolling() {
        requestPoller.scheduleAtFixedRate(
                this::pollPendingAcceptedRequests, 0, 30, TimeUnit.SECONDS);
    }

    private void pollPendingAcceptedRequests() {
        try {
            GetRequestsOptions options =
                    GetRequestsOptions.builder()
                            .clientStatus(RequestStatusQuery.ACCEPTED)
                            .serverStatus(RequestStatusQuery.PENDING)
                            .build();
            GetRequestsResponse response = minecoreRequestManager.getRequests(options);
            List<MappedRequest> requests = response != null ? response.getAll() : null;
            if (requests == null) {
                System.err.println("No requests found");
                return;
            }

            for (MappedRequest mappedRequest : requests) {
                if (mappedRequest == null || mappedRequest.getStatus() == null) {
                    System.out.println("Skipping request with missing status: " + mappedRequest);
                    continue;
                }

                String requestId = mappedRequest.getId();
                if (requestId == null || requestId.isBlank()) {
                    System.out.println("Skipping request with missing id: " + mappedRequest);
                    continue;
                }

                String serverStatus = mappedRequest.getStatus().getServer();
                if (!"pending".equalsIgnoreCase(serverStatus)) {
                    System.out.println("Skipping request with missing status: " + mappedRequest);
                    continue;
                }

                String type = "accept";

                // Convert mapped request payload to the event model shape.
                StoreCreatedRequest storeCreatedRequest =
                        gson.fromJson(gson.toJsonTree(mappedRequest), StoreCreatedRequest.class);
                asyncEventBus.post(new ReceiveRequestEvent(type, storeCreatedRequest));
            }
        } catch (Exception ignored) {
            // Polling should never crash the scheduler thread.
        }
    }
}

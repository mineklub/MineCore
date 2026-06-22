package dk.mineclub.minecore.api;

import com.google.common.eventbus.AsyncEventBus;
import com.google.gson.Gson;
import dk.mineclub.minecore.api.events.ReceiveRequestEvent;
import dk.mineclub.minecore.api.events.ReceiveVoteEvent;
import dk.mineclub.minecore.api.manager.RequestManager;
import dk.mineclub.minecore.api.manager.SocketIOManager;
import dk.mineclub.minecore.api.model.GetRequestsOptions;
import dk.mineclub.minecore.api.model.GetRequestsResponse;
import dk.mineclub.minecore.api.model.GetVotesOptions;
import dk.mineclub.minecore.api.model.GetVotesResponse;
import dk.mineclub.minecore.api.model.MappedRequest;
import dk.mineclub.minecore.api.model.MappedVote;
import dk.mineclub.minecore.api.model.RequestStatusQuery;
import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import dk.mineclub.minecore.api.model.VoteStatusQuery;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

public final class MineCoreApi {
    private static final Logger LOGGER = Logger.getLogger(MineCoreApi.class.getName());
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
                () -> {
                    pollPendingAcceptedRequests();
                    pollPendingVoteRequests();
                },
                0,
                30,
                TimeUnit.SECONDS);
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
                LOGGER.fine("No requests found");
                return;
            }

            for (MappedRequest mappedRequest : requests) {
                if (mappedRequest == null || mappedRequest.getStatus() == null) {
                    LOGGER.warning("Skipping request with missing status: " + mappedRequest);
                    continue;
                }

                String requestId = mappedRequest.getId();
                if (requestId == null || requestId.trim().isEmpty()) {
                    LOGGER.warning("Skipping request with missing id: " + mappedRequest);
                    continue;
                }

                String serverStatus = mappedRequest.getStatus().getServer();
                if (!"pending".equalsIgnoreCase(serverStatus)) {
                    LOGGER.fine(
                            "Skipping request with non-pending server status: " + mappedRequest);
                    continue;
                }

                String type = "accept";

                LOGGER.fine("Dispatching accepted request poll result: " + mappedRequest);

                // Convert mapped request payload to the event model shape.
                StoreCreatedRequest storeCreatedRequest =
                        gson.fromJson(gson.toJsonTree(mappedRequest), StoreCreatedRequest.class);
                asyncEventBus.post(new ReceiveRequestEvent(type, storeCreatedRequest));
            }
        } catch (Exception ex) {
            // Polling should never crash the scheduler thread.
            LOGGER.log(Level.WARNING, "Failed while polling accepted requests", ex);
        }
    }

    private void pollPendingVoteRequests() {
        try {
            GetVotesOptions options =
                    GetVotesOptions.builder().status(VoteStatusQuery.PENDING).build();
            GetVotesResponse response = minecoreRequestManager.getVotes(options);
            List<MappedVote> votes = response != null ? response.getAll() : null;
            if (votes == null) {
                return;
            }

            for (MappedVote mappedVote : votes) {
                if (mappedVote == null) {
                    continue;
                }

                String voteId = mappedVote.getId();
                if (voteId == null || voteId.trim().isEmpty()) {
                    LOGGER.warning("Skipping vote with missing id: " + mappedVote);
                    continue;
                }

                String status = mappedVote.getStatus();
                if (!"pending".equalsIgnoreCase(status)) {
                    LOGGER.fine("Skipping vote with non-pending status: " + mappedVote);
                    continue;
                }

                asyncEventBus.post(new ReceiveVoteEvent(mappedVote));
            }
        } catch (Exception ex) {
            // Polling should never crash the scheduler thread.
            LOGGER.log(Level.WARNING, "Failed while polling votes", ex);
        }
    }
}

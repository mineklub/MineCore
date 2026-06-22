package dk.mineclub.minecore.internal;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dk.mineclub.minecore.internal.channels.*;
import dk.mineclub.minecore.internal.handler.NewVersionHandler;
import dk.mineclub.minecore.internal.handler.OldVersionHandler;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.RedisClient;

public class InternalPlugin {
    private static final String LIMBO_SERVER_NAME = "Limbo";

    @Getter private static InternalPlugin instance;
    @Getter private RedisClient jedis;
    @Getter private Lang lang;

    @Getter private final ProxyServer server;
    @Getter private final Logger logger;
    private final EnvironmentFile environmentFile;
    private final String baseUrl = "https://api.mineclub.dk/v2/minecore";
    private static final OkHttpClient client =
            new OkHttpClient()
                    .newBuilder()
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .build();
    private static final RequestBody EMPTY_REQUEST_BODY =
            new RequestBody() {
                @Override
                public okhttp3.MediaType contentType() {
                    return null;
                }

                @Override
                public long contentLength() {
                    return 0L;
                }

                @Override
                public void writeTo(BufferedSink sink) {}
            };

    @Inject
    public InternalPlugin(
            @NotNull ProxyServer server,
            @NotNull Logger logger,
            @DataDirectory @NotNull Path dataDirectory) {
        instance = this;
        this.server = server;
        this.logger = logger;
        this.environmentFile =
                new EnvironmentFile(new File(dataDirectory.getParent().toFile(), "../"));
        try {
            this.lang = new Lang();
            this.lang.load();
        } catch (Exception e) {
            logger.error("Error loading language file!", e);
        }
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.jedis =
                RedisClient.builder()
                        .hostAndPort(environmentFile.getHost(), environmentFile.getPort())
                        .clientConfig(
                                DefaultJedisClientConfig.builder()
                                        .user(environmentFile.getUser())
                                        .password(environmentFile.getPassword())
                                        .build())
                        .build();

        new StoreRequestChannel(this);
        new StoreRequestFailedChannel(this);
        new StoreRequestSuccessChannel(this);
        new StoreRequestTimeoutChannel(this);
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        event.getPreviousServer()
                .ifPresent(
                        previousServer -> {
                            String previousName = previousServer.getServerInfo().getName();
                            String currentName = event.getServer().getServerInfo().getName();
                            if (isLimboServer(previousName) && !isLimboServer(currentName)) {
                                cancelPendingOnLimboLeave(
                                        event.getPlayer(), previousName + " -> " + currentName);
                            }
                        });
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        event.getPlayer()
                .getCurrentServer()
                .ifPresent(
                        serverConnection -> {
                            String serverName = serverConnection.getServerInfo().getName();
                            if (isLimboServer(serverName)) {
                                cancelPendingOnLimboLeave(
                                        event.getPlayer(), serverName + " -> disconnected");
                            }
                        });
    }

    private boolean isLimboServer(String serverName) {
        return LIMBO_SERVER_NAME.equalsIgnoreCase(serverName);
    }

    private void cancelPendingOnLimboLeave(
            com.velocitypowered.api.proxy.Player player, String flow) {
        int cancelledNew = NewVersionHandler.cancelPendingRequestsForPlayer(this, player);
        int cancelledOld = OldVersionHandler.cancelPendingRequestsForPlayer(this, player);
        int cancelledTotal = cancelledNew + cancelledOld;
        if (cancelledTotal > 0) {
            logger.info(
                    "Cancelled {} pending request(s) for {} after leaving limbo ({}) [new={}, old={}]",
                    cancelledTotal,
                    player.getUsername(),
                    flow,
                    cancelledNew,
                    cancelledOld);
        }
    }

    public <T> int cancelPendingRequestsForPlayer(
            Map<String, T> pendingRequests,
            com.velocitypowered.api.proxy.Player player,
            Function<T, StoreRequestMessage> messageExtractor,
            String sourceLabel) {
        String prefix = player.getUniqueId() + ":";
        int cancelled = 0;

        for (Map.Entry<String, T> entry : pendingRequests.entrySet()) {
            if (!entry.getKey().startsWith(prefix)) {
                continue;
            }

            T pendingValue = entry.getValue();
            if (!pendingRequests.remove(entry.getKey(), pendingValue)) {
                continue;
            }

            StoreRequestMessage message = messageExtractor.apply(pendingValue);
            if (message == null) {
                continue;
            }

            try (Response response = cancelRequest(message.data().id())) {
                if (response != null && !response.isSuccessful()) {
                    logger.warn(
                            "Cancelling pending {} limbo-leave request returned status {}",
                            sourceLabel,
                            response.code());
                }
            } catch (Exception e) {
                logger.warn("Failed to cancel pending {} limbo-leave request", sourceLabel, e);
            }

            OldVersionHandler.publishReturn(this, message);
            cancelled++;
        }

        return cancelled;
    }

    public Response acceptRequest(String id) {
        String token = environmentFile.getToken();
        Request request =
                new Request.Builder()
                        .url(baseUrl + "/client/request/" + id + "/accept")
                        .post(EMPTY_REQUEST_BODY)
                        .header("Authorization", token)
                        .build();

        try {
            return client.newCall(request).execute();
        } catch (Exception ex) {
            logger.warn("Failed to create request, {}", ex.getMessage(), ex);
        }

        return null;
    }

    public Response cancelRequest(String id) {
        String token = environmentFile.getToken();
        Request request =
                new Request.Builder()
                        .url(baseUrl + "/client/request/" + id + "/cancel")
                        .post(EMPTY_REQUEST_BODY)
                        .header("Authorization", token)
                        .build();

        try {
            return client.newCall(request).execute();
        } catch (Exception ex) {
            logger.warn("Failed to cancel request, {}", ex.getMessage(), ex);
        }

        return null;
    }
}

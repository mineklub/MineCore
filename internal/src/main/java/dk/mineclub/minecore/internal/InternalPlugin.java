package dk.mineclub.minecore.internal;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dk.mineclub.minecore.internal.channels.StoreRequestChannel;
import dk.mineclub.minecore.internal.channels.StoreRequestFailedChannel;
import dk.mineclub.minecore.internal.channels.StoreRequestMessage;
import dk.mineclub.minecore.internal.channels.StoreRequestSuccessChannel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import org.slf4j.Logger;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.RedisClient;

public class InternalPlugin {
    @Getter private static InternalPlugin instance;
    @Getter private RedisClient jedis;
    @Getter private Lang lang;

    @Getter private final ProxyServer server;
    private final Logger logger;
    private final EnvironmentFile environmentFile;
    private String baseUrl = "https://api.mineclub.dk/v2/minecore";
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
                public void writeTo(BufferedSink sink) throws IOException {}
            };

    @Inject
    public InternalPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
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
    }

    public Response acceptRequest(StoreRequestMessage message) {
        String token = environmentFile.getToken();
        Request request =
                new Request.Builder()
                        .url(baseUrl + "/client/request/" + message.data().id() + "/accept")
                        .post(EMPTY_REQUEST_BODY)
                        .header("Authorization", token)
                        .build();

        try {
            return client.newCall(request).execute();
        } catch (Exception ex) {
            System.out.println("Failed to create request, " + ex.getMessage());
        }

        return null;
    }

    public Response cancelRequest(StoreRequestMessage message) {
        String token = environmentFile.getToken();
        Request request =
                new Request.Builder()
                        .url(baseUrl + "/client/request/" + message.data().id() + "/cancel")
                        .post(EMPTY_REQUEST_BODY)
                        .header("Authorization", token)
                        .build();

        try {
            return client.newCall(request).execute();
        } catch (Exception ex) {
            System.out.println("Failed to create request, " + ex.getMessage());
        }

        return null;
    }
}

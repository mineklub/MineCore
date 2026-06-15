package dk.mineclub.minecore.internal;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dk.mineclub.minecore.internal.channels.StoreRequestChannel;
import java.io.File;
import java.nio.file.Path;

import dk.mineclub.minecore.internal.channels.StoreRequestFailedChannel;
import dk.mineclub.minecore.internal.channels.StoreRequestSuccessChannel;
import lombok.Getter;
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
}

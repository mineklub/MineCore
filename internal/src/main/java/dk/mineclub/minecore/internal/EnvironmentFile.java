package dk.mineclub.minecore.internal;

import java.io.File;
import lombok.Getter;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

@Getter
public class EnvironmentFile {
    private final File dataFolder;
    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final String token;

    public EnvironmentFile(File dataFolder) {
        this.dataFolder = dataFolder;

        File file = new File(dataFolder, "environment.yml");
        if (!file.exists()) {
            throw new RuntimeException("environment.yml does not exist");
        }

        YamlConfigurationLoader loader =
                YamlConfigurationLoader.builder()
                        .file(new File(dataFolder, "environment.yml"))
                        .build();
        ConfigurationNode node;
        try {
            node = loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load environment.yml", e);
        }
        this.host = node.node("jedis", "host").getString("");
        this.port = node.node("jedis", "port").getInt(6379);
        this.user = node.node("jedis", "user").getString("");
        this.password = node.node("jedis", "password").getString("");
        this.token = node.node("rest", "authorization").getString("");
    }
}

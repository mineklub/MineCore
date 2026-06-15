package dk.mineclub.minecore.internal;

import com.velocitypowered.api.command.CommandSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public class Lang {
    private ConfigurationNode node;

    public void send(CommandSource player, String key, TagResolver... args) {
        Component message = get(key, args);
        if (message == null) {
            return;
        }
        if (message.equals(Component.empty())) {
            return;
        }
        player.sendMessage(message);
    }

    public List<Component> getList(String key, TagResolver... args) {
        String[] nodes = key.split("\\.");
        ConfigurationNode node = this.node;
        for (String subNode : nodes) {
            node = node.node(subNode);
        }
        List<Component> translations = new ArrayList<>();
        try {
            List<String> strings = node.getList(String.class);
            assert strings != null;
            for (String s : strings) {
                translations.add(MiniMessage.miniMessage().deserialize(s, args));
            }
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
        if (translations.isEmpty()) {
            translations.add(
                    MiniMessage.miniMessage()
                            .deserialize("<red>Missing translation for <yellow>" + key));
        }
        return translations;
    }

    public String getString(String key) {
        return splitNode(key);
    }

    public Component get(String key) {
        if (!getList(key).isEmpty()) {
            return Component.join(JoinConfiguration.newlines(), getList(key));
        }
        String translation = splitNode(key);
        return MiniMessage.miniMessage().deserialize(translation);
    }

    private String splitNode(String key) {
        String[] nodes = key.split("\\.");
        ConfigurationNode node = this.node;
        for (String subNode : nodes) {
            node = node.node(subNode);
        }
        String translation = node.getString();
        if (translation == null) {
            translation = "<red>Missing translation for <yellow>" + key;
        }
        return translation;
    }

    public Component get(String key, TagResolver... args) {
        if (!getList(key, args).isEmpty()) {
            return Component.join(JoinConfiguration.newlines(), getList(key, args));
        }
        String translation = splitNode(key);
        return MiniMessage.miniMessage().deserialize(translation, args);
    }

    public void load() throws IOException {
        final InputStream lang =
                InternalPlugin.getInstance()
                        .getClass()
                        .getClassLoader()
                        .getResourceAsStream("lang.yml");
        if (lang == null) {
            throw new IOException("lang.yml not found");
        }
        Callable<BufferedReader> source = () -> new BufferedReader(new InputStreamReader(lang));
        final YamlConfigurationLoader loader =
                YamlConfigurationLoader.builder().source(source).build();
        this.node = loader.load();
        // Convert the InputStream to a ConfigurationNode
    }
}

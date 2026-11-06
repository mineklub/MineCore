package dk.mineclub.minecore.internal;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import dk.mineclub.minecore.internal.i18n.*;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class Lang {
    private MessageCatalog catalog;

    public void load() throws IOException {
        catalog =
                new MessageCatalog(
                        getClass().getResourceAsStream("/language/en.yml"),
                        getClass().getResourceAsStream("/language/da.yml"));
    }

    public void send(CommandSource source, String key, TagResolver... args) {
        source.sendMessage(
                source instanceof Player player ? get(player, key, args) : get(key, args));
    }

    public Component get(String key) {
        return get(key, new TagResolver[0]);
    }

    public Component get(String key, TagResolver... args) {
        return catalog.get(Language.ENGLISH, key, args);
    }

    public Component get(Player player, String key, TagResolver... args) {
        return get(player.getUniqueId(), key, args);
    }

    public Component get(UUID player, String key, TagResolver... args) {
        return catalog.get(PlayerLanguages.get(player), key, args);
    }

    public List<Component> getList(String key, TagResolver... args) {
        return catalog.list(Language.ENGLISH, key, args);
    }

    public List<Component> getList(UUID player, String key, TagResolver... args) {
        return catalog.list(PlayerLanguages.get(player), key, args);
    }

    public String getString(String key) {
        return String.join("\n", catalog.strings(Language.ENGLISH, key));
    }
}

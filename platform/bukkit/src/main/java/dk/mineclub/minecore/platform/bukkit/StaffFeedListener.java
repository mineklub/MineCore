package dk.mineclub.minecore.platform.bukkit;

import dk.mineclub.minecore.api.MineCoreApi;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class StaffFeedListener implements Listener {

    private static final String[] RANKS = {"Admin", "Udvikler", "Mod", "Supporter"};

    private final MineCoreApi api;

    public StaffFeedListener(MineCoreApi api) {
        this.api = api;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        String text = PlainTextComponentSerializer.plainText().serialize(event.message());
        send("chat", event.getPlayer(), text);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        send("join", event.getPlayer(), null);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        send("quit", event.getPlayer(), null);
    }

    private void send(String type, Player player, String message) {
        api.getMinecoreRequestManager()
                .sendFeedEvent(
                        type,
                        player.getName(),
                        player.getUniqueId().toString(),
                        rankOf(player),
                        message);
    }

    private String rankOf(Player player) {
        for (String rank : RANKS) {
            if (player.hasPermission("group." + rank.toLowerCase())) {
                return rank;
            }
        }
        return null;
    }
}

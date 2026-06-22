package dk.mineclub.minecore.example.listeners;

import com.google.common.eventbus.Subscribe;
import dk.mineclub.minecore.api.MineCoreApi;
import dk.mineclub.minecore.api.events.PostCreateRequestEvent;
import dk.mineclub.minecore.api.events.PreCreateRequestEvent;
import dk.mineclub.minecore.api.events.ReceiveRequestEvent;
import dk.mineclub.minecore.api.events.ReceiveVoteEvent;
import dk.mineclub.minecore.api.model.MappedVote;
import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import dk.mineclub.minecore.example.ExamplePlugin;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class StoreListeners implements Listener {
    private final MineCoreApi mineCoreApi;
    private final ExamplePlugin plugin;

    public StoreListeners(MineCoreApi mineCoreApi, ExamplePlugin plugin) {
        this.mineCoreApi = mineCoreApi;
        this.plugin = plugin;
    }

    @Subscribe
    public void preEvent(PreCreateRequestEvent event) {
        plugin.getLogger().info("PreCreateRequestEvent called for request: " + event);
    }

    @Subscribe
    public void postEvent(PostCreateRequestEvent event) {
        plugin.getLogger().info("PostCreateRequestEvent called for request: " + event);
    }

    @Subscribe
    public void receiveEvent(ReceiveRequestEvent event) {
        plugin.getLogger().info("ReceiveRequestEvent called for request: " + event);

        if (event.getStoreRequest() == null
                || event.getStoreRequest().getMcaccount() == null
                || event.getStoreRequest().getMcaccount().getUuid() == null) {
            return;
        }

        try {
            UUID uuid = UUID.fromString(event.getStoreRequest().getMcaccount().getUuid());
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                processRequest(player, event.getStoreRequest());
            } else {
                plugin.queueRequest(event.getStoreRequest());
            }
            mineCoreApi.getMinecoreRequestManager().acceptRequest(event.getStoreRequest());
        } catch (IllegalArgumentException ex) {
            plugin.getLogger()
                    .warning(
                            "Invalid request UUID: "
                                    + event.getStoreRequest().getMcaccount().getUuid());
        }
    }

    @Subscribe
    public void receiveVoteEvent(ReceiveVoteEvent event) {
        plugin.getLogger().info("ReceiveVoteEvent called for vote: " + event);

        if (event.getVote() == null
                || event.getVote().getMcaccount() == null
                || event.getVote().getMcaccount().getUuid() == null) {
            return;
        }

        try {
            UUID uuid = UUID.fromString(event.getVote().getMcaccount().getUuid());
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                processVote(player, event.getVote());
            } else {
                plugin.queueVote(event.getVote());
            }
            mineCoreApi.getMinecoreRequestManager().acceptVote(event.getVote());
        } catch (IllegalArgumentException ex) {
            plugin.getLogger()
                    .warning("Invalid vote UUID: " + event.getVote().getMcaccount().getUuid());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (var request : plugin.drainRequests(event.getPlayer().getUniqueId())) {
            processRequest(event.getPlayer(), request);
        }

        for (var vote : plugin.drainVotes(event.getPlayer().getUniqueId())) {
            processVote(event.getPlayer(), vote);
        }
    }

    private void processRequest(Player player, StoreCreatedRequest request) {
        player.sendMessage("§aYour MineCore request was received.");
    }

    private void processVote(Player player, MappedVote vote) {
        player.sendMessage("§aThanks for voting! Your vote was received.");
    }
}

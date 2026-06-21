package dk.mineclub.minecore.example;

import dk.mineclub.minecore.api.MineCoreApi;
import dk.mineclub.minecore.api.model.MappedVote;
import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import dk.mineclub.minecore.example.commands.BuyCommand;
import dk.mineclub.minecore.example.listeners.StoreListeners;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.bukkit.plugin.java.JavaPlugin;

public class ExamplePlugin extends JavaPlugin {
    public MineCoreApi api;
    private final ConcurrentHashMap<UUID, Queue<StoreCreatedRequest>> pendingRequests =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Queue<MappedVote>> pendingVotes =
            new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        api = new MineCoreApi();
        this.getLifecycleManager()
                .registerEventHandler(
                        LifecycleEvents.COMMANDS,
                        commands -> {
                            commands.registrar()
                                    .register(new BuyCommand().createCommand("buy", this));
                        });
        StoreListeners listeners = new StoreListeners(api, this);
        api.getAsyncEventBus().register(listeners);
        getServer().getPluginManager().registerEvents(listeners, this);
    }

    public void queueVote(MappedVote vote) {
        if (vote == null || vote.getMcaccount() == null || vote.getMcaccount().getUuid() == null) {
            return;
        }

        try {
            UUID uuid = UUID.fromString(vote.getMcaccount().getUuid());
            pendingVotes.computeIfAbsent(uuid, ignored -> new ConcurrentLinkedQueue<>()).add(vote);
        } catch (IllegalArgumentException ex) {
            getLogger()
                    .warning("Ignoring vote with invalid UUID: " + vote.getMcaccount().getUuid());
        }
    }

    public void queueRequest(StoreCreatedRequest request) {
        if (request == null
                || request.getMcaccount() == null
                || request.getMcaccount().getUuid() == null) {
            return;
        }

        try {
            UUID uuid = UUID.fromString(request.getMcaccount().getUuid());
            pendingRequests
                    .computeIfAbsent(uuid, ignored -> new ConcurrentLinkedQueue<>())
                    .add(request);
        } catch (IllegalArgumentException ex) {
            getLogger()
                    .warning(
                            "Ignoring request with invalid UUID: "
                                    + request.getMcaccount().getUuid());
        }
    }

    public List<MappedVote> drainVotes(UUID uuid) {
        Queue<MappedVote> queue = pendingVotes.remove(uuid);
        if (queue == null || queue.isEmpty()) {
            return List.of();
        }

        return new ArrayList<>(queue);
    }

    public List<StoreCreatedRequest> drainRequests(UUID uuid) {
        Queue<StoreCreatedRequest> queue = pendingRequests.remove(uuid);
        if (queue == null || queue.isEmpty()) {
            return List.of();
        }

        return new ArrayList<>(queue);
    }
}

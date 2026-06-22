package dk.mineclub.minecore.platform.common.bridge;

import com.google.common.eventbus.Subscribe;
import dk.mineclub.minecore.api.events.PostCreateRequestEvent;
import dk.mineclub.minecore.api.events.PreCreateRequestEvent;
import dk.mineclub.minecore.api.events.ReceiveRequestEvent;
import dk.mineclub.minecore.api.events.ReceiveVoteEvent;
import dk.mineclub.minecore.platform.common.event.MineCorePostCreateRequestEvent;
import dk.mineclub.minecore.platform.common.event.MineCorePreCreateRequestEvent;
import dk.mineclub.minecore.platform.common.event.MineCoreReceiveRequestEvent;
import dk.mineclub.minecore.platform.common.event.MineCoreReceiveVoteEvent;
import dk.mineclub.minecore.platform.common.event.SocketRequestType;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/** Bridges async MineCore API events into synchronous Bukkit events. */
public class CommonEventBridge {
    private final JavaPlugin plugin;

    public CommonEventBridge(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onPreCreate(PreCreateRequestEvent event) {
        MineCorePreCreateRequestEvent bukkitEvent =
                callSync(
                        () -> {
                            MineCorePreCreateRequestEvent createdEvent =
                                    new MineCorePreCreateRequestEvent(event.getStoreRequest());
                            Bukkit.getPluginManager().callEvent(createdEvent);
                            return createdEvent;
                        });

        if (bukkitEvent != null && bukkitEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onPostCreate(PostCreateRequestEvent event) {
        callSync(
                () -> {
                    Bukkit.getPluginManager()
                            .callEvent(new MineCorePostCreateRequestEvent(event.getStoreRequest()));
                    return null;
                });
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onReceive(ReceiveRequestEvent event) {
        callSync(
                () -> {
                    plugin.getLogger().fine("ReceiveRequestEvent payload: " + event.getStoreRequest());
                    Bukkit.getPluginManager()
                            .callEvent(
                                    new MineCoreReceiveRequestEvent(
                                            SocketRequestType.from(event.getType()),
                                            event.getStoreRequest()));
                    return null;
                });
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onReceiveVote(ReceiveVoteEvent event) {
        callSync(
                () -> {
                    plugin.getLogger().fine("ReceiveVoteEvent payload: " + event.getVote());
                    Bukkit.getPluginManager()
                            .callEvent(new MineCoreReceiveVoteEvent(event.getVote()));
                    return null;
                });
    }

    private <T> T callSync(Callable<T> callable) {
        try {
            if (Bukkit.isPrimaryThread()) {
                return callable.call();
            }

            Future<T> future = Bukkit.getScheduler().callSyncMethod(this.plugin, callable);
            return future.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            plugin.getLogger()
                    .warning("Interrupted while dispatching Bukkit event: " + ex.getMessage());
        } catch (ExecutionException ex) {
            plugin.getLogger().warning("Failed to dispatch Bukkit event: " + ex.getCause());
        } catch (Exception ex) {
            plugin.getLogger().warning("Failed to dispatch Bukkit event: " + ex.getMessage());
        }

        return null;
    }
}

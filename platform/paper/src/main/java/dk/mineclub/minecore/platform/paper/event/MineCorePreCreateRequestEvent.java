package dk.mineclub.minecore.platform.paper.event;

import dk.mineclub.minecore.api.model.StoreRequest;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MineCorePreCreateRequestEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    @Getter private final StoreRequest storeRequest;
    private boolean cancelled;

    public MineCorePreCreateRequestEvent(StoreRequest storeRequest) {
        this.storeRequest = storeRequest;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(storeRequest.getMcaccount());
    }
}

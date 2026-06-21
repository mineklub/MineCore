package dk.mineclub.minecore.platform.common.event;

import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class MineCorePostCreateRequestEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final StoreCreatedRequest storeRequest;

    public MineCorePostCreateRequestEvent(StoreCreatedRequest storeRequest) {
        this.storeRequest = storeRequest;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(UUID.fromString(storeRequest.getMcaccount().getUuid()));
    }
}

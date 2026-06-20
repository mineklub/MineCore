package dk.mineclub.minecore.platform.paper.event;

import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class MineCoreReceiveRequestEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final SocketRequestType type;
    private final StoreCreatedRequest storeRequest;

    public MineCoreReceiveRequestEvent(SocketRequestType type, StoreCreatedRequest storeRequest) {
        this.type = type == null ? SocketRequestType.UNKNOWN : type;
        this.storeRequest = storeRequest;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(UUID.fromString(storeRequest.getMcaccount().getUuid()));
    }
}

package dk.mineclub.minecore.platform.common.event;

import dk.mineclub.minecore.api.model.MappedVote;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class MineCoreReceiveVoteEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final MappedVote vote;

    public MineCoreReceiveVoteEvent(MappedVote vote) {
        this.vote = vote;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public OfflinePlayer getOfflinePlayer() {
        if (vote == null || vote.getMcaccount() == null || vote.getMcaccount().getUuid() == null) {
            return null;
        }

        return Bukkit.getOfflinePlayer(UUID.fromString(vote.getMcaccount().getUuid()));
    }
}

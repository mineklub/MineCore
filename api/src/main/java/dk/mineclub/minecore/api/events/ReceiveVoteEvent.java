package dk.mineclub.minecore.api.events;

import dk.mineclub.minecore.api.MineCoreApi;
import dk.mineclub.minecore.api.model.MappedVote;
import lombok.Getter;

@Getter
public class ReceiveVoteEvent implements Event {
    private final boolean cancelled = false;
    private final MappedVote vote;

    public ReceiveVoteEvent(MappedVote vote) {
        this.vote = vote;
    }

    @Override
    public boolean callEvent() {
        MineCoreApi api = MineCoreApi.getInstance();
        api.getAsyncEventBus().post(this);
        return cancelled;
    }

    @Override
    public String toString() {
        return "ReceiveVoteEvent{" + "vote=" + vote + '}';
    }
}

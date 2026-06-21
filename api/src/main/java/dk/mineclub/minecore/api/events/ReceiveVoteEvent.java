package dk.mineclub.minecore.api.events;

import dk.mineclub.minecore.api.MineCoreApi;
import lombok.Getter;

@Getter
public class ReceiveVoteEvent implements Event {
    private final boolean cancelled = false;

    @Override
    public boolean callEvent() {
        MineCoreApi api = MineCoreApi.getInstance();
        api.getAsyncEventBus().post(this);
        return cancelled;
    }
}

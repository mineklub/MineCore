package dk.mineclub.minecore.api.events;

import dk.mineclub.minecore.api.MineCoreApi;
import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import lombok.Getter;

@Getter
public class ReceiveRequestEvent implements Event {
    private final boolean cancelled = false;
    private final String type;
    private final StoreCreatedRequest storeRequest;

    public ReceiveRequestEvent(String type, StoreCreatedRequest storeRequest) {
        this.type = type;
        this.storeRequest = storeRequest;
    }

    @Override
    public boolean callEvent() {
        MineCoreApi api = MineCoreApi.getInstance();
        api.getAsyncEventBus().post(this);
        return cancelled;
    }

    @Override
    public String toString() {
        return "ReceiveRequestEvent{"
                + "type='"
                + type
                + '\''
                + ", storeRequest="
                + storeRequest
                + '}';
    }
}

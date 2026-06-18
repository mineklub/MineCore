package dk.mineclub.minecore.api.events;

import dk.mineclub.minecore.api.MineCoreApi;
import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import lombok.Getter;

@Getter
public class PostCreateRequestEvent implements Event {
    private boolean cancelled;
    private final StoreCreatedRequest storeRequest;

    public PostCreateRequestEvent(StoreCreatedRequest storeRequest) {
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
        return "PostCreateRequestEvent{"
                + "cancelled="
                + cancelled
                + ", storeRequest="
                + storeRequest
                + '}';
    }
}

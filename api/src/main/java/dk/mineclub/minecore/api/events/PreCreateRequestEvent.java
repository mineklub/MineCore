package dk.mineclub.minecore.api.events;

import dk.mineclub.minecore.api.MineCoreApi;
import dk.mineclub.minecore.api.model.StoreRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PreCreateRequestEvent implements Event {
    @Setter private boolean cancelled;
    private final StoreRequest storeRequest;

    public PreCreateRequestEvent(StoreRequest storeRequest) {
        this.storeRequest = storeRequest;
    }

    @Override
    public boolean callEvent() {
        MineCoreApi api = MineCoreApi.getInstance();
        api.getAsyncEventBus().post(this);
        return cancelled;
    }
}

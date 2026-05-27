package dk.mineclub.minecore.api.events;

import dk.mineclub.minecore.api.MineCoreApi;
import dk.mineclub.minecore.api.model.StoreRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
public class PreCreateRequestEvent implements Event {
	@Setter
	private boolean cancelled;
	private final StoreRequest storeRequest;
	private final UUID mcaccount;

	public PreCreateRequestEvent(StoreRequest storeRequest, UUID mcaccount) {
		this.storeRequest = storeRequest;
		this.mcaccount = mcaccount;
	}

	@Override
	public boolean callEvent() {
		MineCoreApi api = MineCoreApi.getInstance();
		api.getAsyncEventBus().post(this);
		return cancelled;
	}
}

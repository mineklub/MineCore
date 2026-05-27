package dk.mineclub.minecore.api.events;

import lombok.Getter;

@Getter
public class ReceiveRequestEvent implements Event {
    private final boolean cancelled = false;

    @Override
    public boolean callEvent() {
        return false;
    }
}

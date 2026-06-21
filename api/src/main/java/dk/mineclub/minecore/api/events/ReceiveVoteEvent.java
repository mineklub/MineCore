package dk.mineclub.minecore.api.events;

public class ReceiveVoteEvent implements Event {
    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean callEvent() {
        return false;
    }
}

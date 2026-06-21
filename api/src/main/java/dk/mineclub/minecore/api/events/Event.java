package dk.mineclub.minecore.api.events;

public interface Event {
    boolean isCancelled();

    boolean callEvent();
}

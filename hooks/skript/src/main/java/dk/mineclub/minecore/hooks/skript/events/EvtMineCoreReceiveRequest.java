package dk.mineclub.minecore.hooks.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import dk.mineclub.minecore.platform.paper.event.MineCoreReceiveRequestEvent;
import dk.mineclub.minecore.platform.paper.event.SocketRequestType;
import org.bukkit.event.Event;

public class EvtMineCoreReceiveRequest extends SkriptEvent {
    static {
        Skript.registerEvent(
                "MineCore Receive Request",
                EvtMineCoreReceiveRequest.class,
                MineCoreReceiveRequestEvent.class,
                "minecore receive request");
        EventValues.registerEventValue(
                MineCoreReceiveRequestEvent.class,
                SocketRequestType.class,
                new Getter<SocketRequestType, MineCoreReceiveRequestEvent>() {
                    @Override
                    public SocketRequestType get(MineCoreReceiveRequestEvent event) {
                        return event.getType();
                    }
                },
                0);
        EventValues.registerEventValue(
                MineCoreReceiveRequestEvent.class,
                StoreCreatedRequest.class,
                new Getter<StoreCreatedRequest, MineCoreReceiveRequestEvent>() {
                    @Override
                    public StoreCreatedRequest get(MineCoreReceiveRequestEvent event) {
                        return event.getStoreRequest();
                    }
                },
                0);
    }

    @Override
    public boolean init(
            Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
        return true;
    }

    @Override
    public boolean check(Event event) {
        return event instanceof MineCoreReceiveRequestEvent;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "minecore receive request";
    }
}

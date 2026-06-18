package dk.mineclub.minecore.hooks.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import dk.mineclub.minecore.api.model.StoreRequest;
import dk.mineclub.minecore.platform.paper.event.MineCorePreCreateRequestEvent;
import org.bukkit.event.Event;

public class EvtMineCorePreCreateRequest extends SkriptEvent {
    static {
        Skript.registerEvent(
                "MineCore Pre Create Request",
                EvtMineCorePreCreateRequest.class,
                MineCorePreCreateRequestEvent.class,
                "minecore pre create request");
        EventValues.registerEventValue(
                MineCorePreCreateRequestEvent.class,
                StoreRequest.class,
                new Getter<StoreRequest, MineCorePreCreateRequestEvent>() {
                    @Override
                    public StoreRequest get(MineCorePreCreateRequestEvent event) {
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
        return event instanceof MineCorePreCreateRequestEvent;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "minecore pre create request";
    }
}

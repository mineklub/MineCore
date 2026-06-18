package dk.mineclub.minecore.hooks.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import dk.mineclub.minecore.platform.paper.event.MineCorePostCreateRequestEvent;
import org.bukkit.event.Event;

public class EvtMineCorePostCreateRequest extends SkriptEvent {
    static {
        Skript.registerEvent(
                "MineCore Post Create Request",
                EvtMineCorePostCreateRequest.class,
                MineCorePostCreateRequestEvent.class,
                "minecore post create request");
        EventValues.registerEventValue(
                MineCorePostCreateRequestEvent.class,
                StoreCreatedRequest.class,
                new Getter<StoreCreatedRequest, MineCorePostCreateRequestEvent>() {
                    @Override
                    public StoreCreatedRequest get(MineCorePostCreateRequestEvent event) {
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
        return event instanceof MineCorePostCreateRequestEvent;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "minecore post create request";
    }
}

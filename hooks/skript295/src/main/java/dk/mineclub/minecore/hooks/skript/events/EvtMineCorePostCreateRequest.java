package dk.mineclub.minecore.hooks.skript.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import dk.mineclub.minecore.platform.common.event.MineCorePostCreateRequestEvent;
import org.bukkit.event.Event;

public class EvtMineCorePostCreateRequest extends SkriptEvent {

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

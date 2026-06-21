package dk.mineclub.minecore.hooks.skript.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import dk.mineclub.minecore.platform.paper.event.MineCoreReceiveVoteEvent;
import org.bukkit.event.Event;

public class EvtMineCoreReceiveVote extends SkriptEvent {

    @Override
    public boolean init(
            Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
        return true;
    }

    @Override
    public boolean check(Event event) {
        return event instanceof MineCoreReceiveVoteEvent;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "minecore receive vote";
    }
}

package dk.mineclub.minecore.hooks.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import dk.mineclub.minecore.platform.common.event.MineCoreReceiveRequestEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprMineCoreReceiveRequestType extends SimpleExpression<String> {
    @Override
    public boolean init(
            Expression<?>[] expressions,
            int matchedPattern,
            Kleenean isDelayed,
            SkriptParser.ParseResult parseResult) {
        Class<? extends Event>[] currentEvents = getParser().getCurrentEvents();
        if (currentEvents == null) {
            return false;
        }

        for (Class<? extends Event> eventClass : currentEvents) {
            if (MineCoreReceiveRequestEvent.class.isAssignableFrom(eventClass)) {
                return true;
            }
        }

        Skript.error("'event-type' can only be used in a minecore receive request event.");
        return false;
    }

    @Override
    protected String[] get(Event event) {
        if (!(event instanceof MineCoreReceiveRequestEvent)) {
            return new String[0];
        }
        MineCoreReceiveRequestEvent receiveEvent = (MineCoreReceiveRequestEvent) event;

        return new String[] {receiveEvent.getType().name()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "event-type";
    }
}

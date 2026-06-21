package dk.mineclub.minecore.hooks.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import dk.mineclub.minecore.platform.common.event.MineCoreReceiveRequestEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Returns the request type string inside a {@code minecore receive request} event.
 *
 * <p>Note: Skript dev36 does not expose {@code ParserInstance.getCurrentEvents()}, so parse-time
 * event validation is skipped — using this expression outside a {@code minecore receive request}
 * event simply returns an empty result at runtime.
 */
public class ExprMineCoreReceiveRequestType extends SimpleExpression<String> {
    @Override
    public boolean init(
            Expression<?>[] expressions,
            int matchedPattern,
            Kleenean isDelayed,
            SkriptParser.ParseResult parseResult) {
        // Cannot validate current event context in Skript dev36 (no getParser() on SyntaxElement).
        return true;
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

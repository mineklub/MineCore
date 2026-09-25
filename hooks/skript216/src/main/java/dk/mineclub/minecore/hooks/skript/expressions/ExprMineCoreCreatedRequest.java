package dk.mineclub.minecore.hooks.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import dk.mineclub.minecore.hooks.skript.runtime.RequestExecutionContext;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprMineCoreCreatedRequest extends SimpleExpression<StoreCreatedRequest> {
    @Override
    public boolean init(
            Expression<?>[] expressions,
            int matchedPattern,
            Kleenean isDelayed,
            SkriptParser.ParseResult parseResult) {
        return true;
    }

    @Override
    protected StoreCreatedRequest[] get(Event event) {
        StoreCreatedRequest request = RequestExecutionContext.lastCreatedRequest();
        if (request == null) {
            return new StoreCreatedRequest[0];
        }

        return new StoreCreatedRequest[] {request};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends StoreCreatedRequest> getReturnType() {
        return StoreCreatedRequest.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "last minecore created request";
    }
}

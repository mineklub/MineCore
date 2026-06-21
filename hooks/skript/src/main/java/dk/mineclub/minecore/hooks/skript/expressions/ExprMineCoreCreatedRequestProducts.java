package dk.mineclub.minecore.hooks.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprMineCoreCreatedRequestProducts
        extends SimpleExpression<StoreCreatedRequest.Product> {

    private Expression<StoreCreatedRequest> requestExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(
            Expression<?>[] exprs,
            int matchedPattern,
            Kleenean isDelayed,
            SkriptParser.ParseResult parseResult) {
        this.requestExpr = (Expression<StoreCreatedRequest>) exprs[0];
        return true;
    }

    @Override
    protected StoreCreatedRequest.Product[] get(Event event) {
        StoreCreatedRequest request = requestExpr.getSingle(event);
        if (request == null) return null;
        System.out.println("Getting products for request: " + request.toString());
        if (request.getProducts() == null) return null;
        return request.getProducts().toArray(new StoreCreatedRequest.Product[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<StoreCreatedRequest.Product> getReturnType() {
        return StoreCreatedRequest.Product.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "store request products";
    }
}

package dk.mineclub.minecore.hooks.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import dk.mineclub.minecore.api.model.StoreProduct;
import dk.mineclub.minecore.api.model.StoreRequest;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprMineCoreNewRequestProducts extends SimpleExpression<StoreProduct> {

    private Expression<StoreRequest> requestExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(
            Expression<?>[] exprs,
            int matchedPattern,
            Kleenean isDelayed,
            ParseResult parseResult) {
        this.requestExpr = (Expression<StoreRequest>) exprs[0];
        return true;
    }

    @Override
    protected StoreProduct[] get(Event event) {
        StoreRequest request = requestExpr.getSingle(event);
        if (request == null || request.getStoreProducts() == null) {
            return new StoreProduct[0];
        }
        return request.getStoreProducts();
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<StoreProduct> getReturnType() {
        return StoreProduct.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "store request products";
    }
}

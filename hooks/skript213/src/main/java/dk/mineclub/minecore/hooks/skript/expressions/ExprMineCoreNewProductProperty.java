package dk.mineclub.minecore.hooks.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import dk.mineclub.minecore.api.model.StoreProduct;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprMineCoreNewProductProperty extends SimpleExpression<Object> {

    private Expression<StoreProduct> productExpr;
    private int property; // 0=id, 1=price, 2=quantity, 3=subscriptionDays

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(
            Expression<?>[] exprs,
            int matchedPattern,
            Kleenean isDelayed,
            ParseResult parseResult) {
        if (!StoreProduct.class.isAssignableFrom(exprs[0].getReturnType())) {
            return false;
        }

        this.productExpr = (Expression<StoreProduct>) exprs[0];
        this.property = matchedPattern;
        return true;
    }

    @Override
    protected Object[] get(Event event) {
        StoreProduct product = productExpr.getSingle(event);
        if (product == null) {
            return new Object[0];
        }

        Object value =
                switch (property) {
                    case 0 -> product.getId();
                    case 1 -> product.getName();
                    case 2 -> product.getPrice();
                    case 3 -> product.getQuantity();
                    case 4 -> product.getSubscriptionDays();
                    default -> null;
                };

        return value == null ? new Object[0] : new Object[] {value};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<?> getReturnType() {
        return switch (property) {
            case 2 -> Double.class;
            case 3, 4 -> Integer.class;
            default -> String.class;
        };
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "store product property";
    }
}

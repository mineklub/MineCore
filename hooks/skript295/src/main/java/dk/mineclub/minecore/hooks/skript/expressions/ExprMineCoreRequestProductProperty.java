package dk.mineclub.minecore.hooks.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import dk.mineclub.minecore.api.model.StoreCreatedRequest.Product;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprMineCoreRequestProductProperty extends SimpleExpression<Object> {

    private Expression<Product> productExpr;
    private int property; // 0=id, 1=productId, 2=productName, 3=price, 4=quantity, 5=createdAt,

    // 6=updatedAt

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(
            Expression<?>[] exprs,
            int matchedPattern,
            Kleenean isDelayed,
            ParseResult parseResult) {
        this.productExpr = (Expression<Product>) exprs[0];
        this.property = matchedPattern;
        return true;
    }

    @Override
    protected Object[] get(Event event) {
        Product product = productExpr.getSingle(event);
        if (product == null) {
            return new Object[0];
        }

        Object value;
        switch (property) {
            case 0:
                value = product.getProductId();
                break;
            case 1:
                value = product.getProductName();
                break;
            case 2:
                value = product.getPriceAsDouble();
                break;
            case 3:
                value = product.getQuantity();
                break;
            case 4:
                value = product.getCreatedAt();
                break;
            case 5:
                value = product.getUpdatedAt();
                break;
            default:
                value = null;
                break;
        }

        return value == null ? new Object[0] : new Object[] {value};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<?> getReturnType() {
        switch (property) {
            case 2:
                return Double.class;
            case 3:
                return Integer.class;
            default:
                return String.class;
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "product property";
    }
}

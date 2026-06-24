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
        if (!Product.class.isAssignableFrom(exprs[0].getReturnType())) {
            return false;
        }

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

        Object value =
                switch (property) {
                    case 0 -> product.getProductId();
                    case 1 -> product.getProductName();
                    case 2 -> product.getPrice();
                    case 3 -> product.getQuantity();
                    case 4 -> product.getCreatedAt();
                    case 5 -> product.getUpdatedAt();
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
            case 1 -> Double.class;
            case 2 -> Integer.class;
            default -> String.class;
        };
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "product property";
    }
}

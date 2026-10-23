package dk.mineclub.minecore.hooks.skript.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import dk.mineclub.minecore.api.model.StoreProduct;
import dk.mineclub.minecore.hooks.skript.runtime.RequestExecutionContext;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EffMineCoreAddProduct extends Effect {
    private Expression<String> productId;
    private @Nullable Expression<String> productName;
    private @Nullable Expression<Number> price;
    private @Nullable Expression<Number> quantity;
    private @Nullable Expression<Number> subscriptionDays;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(
            Expression<?>[] expressions,
            int matchedPattern,
            Kleenean isDelayed,
            SkriptParser.ParseResult parseResult) {
        productId = (Expression<String>) expressions[0];
        productName = (Expression<String>) expressions[1];
        price = (Expression<Number>) expressions[2];
        quantity = (Expression<Number>) expressions[3];
        subscriptionDays = (Expression<Number>) expressions[4];
        return true;
    }

    @Override
    protected void execute(Event event) {
        String idValue = productId.getSingle(event);
        if (idValue == null || idValue.isBlank()) {
            return;
        }

        StoreProduct product =
                StoreProduct.builder()
                        .id(idValue)
                        .name(valueOrDefault(productName, event, idValue))
                        .price(integerValue(price, event, null))
                        .quantity(integerValue(quantity, event, 1))
                        .subscriptionDays(integerValue(subscriptionDays, event, null))
                        .build();

        // No-op outside a "create minecore request" section.
        RequestExecutionContext.addPendingProduct(product);
    }

    private static Integer integerValue(
            @Nullable Expression<Number> expression, Event event, @Nullable Integer defaultValue) {
        if (expression == null) {
            return defaultValue;
        }
        Number value = expression.getSingle(event);
        return value == null ? defaultValue : value.intValue();
    }

    private static String valueOrDefault(
            @Nullable Expression<String> expression, Event event, String defaultValue) {
        if (expression == null) {
            return defaultValue;
        }
        String value = expression.getSingle(event);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "add product " + productId.toString(event, debug) + " to minecore request";
    }
}

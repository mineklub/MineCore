package dk.mineclub.minecore.hooks.skript.sections;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import dk.mineclub.minecore.api.manager.RequestManager;
import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import dk.mineclub.minecore.api.model.StoreProduct;
import dk.mineclub.minecore.hooks.skript.runtime.MineCoreSkriptApi;
import dk.mineclub.minecore.hooks.skript.runtime.RequestExecutionContext;
import dk.mineclub.minecore.hooks.skript.util.EffectSection;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Implements "create [a] minecore request for %offlineplayer%" as a real section-aware effect.
 *
 * <p>Usage:
 *
 * <pre>
 * create a minecore request for player:
 *     add product "my-product" named "My Product" price 10
 * </pre>
 */
public class SecMineCoreCreateRequest extends EffectSection {
    private Expression<OfflinePlayer> playerExpr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(
            Expression<?>[] expressions,
            int matchedPattern,
            Kleenean isDelayed,
            SkriptParser.ParseResult parseResult) {
        playerExpr = (Expression<OfflinePlayer>) expressions[0];
        loadSection("minecore create request section", true, ScriptLoader.getCurrentEvents());
        return true;
    }

    @Override
    protected void execute(Event event) {
        OfflinePlayer player = playerExpr.getSingle(event);
        if (player == null) {
            return;
        }

        RequestManager requestManager = MineCoreSkriptApi.requestManager();
        if (requestManager == null) {
            return;
        }

        List<StoreProduct> products;
        try (RequestExecutionContext.ProductScope scope =
                RequestExecutionContext.withPendingProducts()) {
            runSection(event);
            products = scope.products();
        }

        if (products.isEmpty()) {
            return;
        }

        StoreCreatedRequest createdRequest =
                requestManager.createRequest(
                        dk.mineclub.minecore.api.model.StoreRequest.builder()
                                .mcaccount(player.getUniqueId())
                                .storeProducts(products.toArray(new StoreProduct[0]))
                                .build());
        if (createdRequest != null) {
            RequestExecutionContext.setLastCreatedRequest(createdRequest);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "create minecore request for "
                + (playerExpr != null ? playerExpr.toString(event, debug) : "?");
    }
}

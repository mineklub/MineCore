package dk.mineclub.minecore.hooks.skript.sections;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import dk.mineclub.minecore.api.manager.RequestManager;
import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import dk.mineclub.minecore.api.model.StoreProduct;
import dk.mineclub.minecore.api.model.StoreRequest;
import dk.mineclub.minecore.hooks.skript.runtime.MineCoreSkriptApi;
import dk.mineclub.minecore.hooks.skript.runtime.RequestExecutionContext;
import java.util.List;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class SecMineCoreCreateRequest extends Section {
    private Expression<OfflinePlayer> account;
    private Trigger sectionTrigger;

    @Override
    public boolean init(
            Expression<?>[] expressions,
            int matchedPattern,
            Kleenean isDelayed,
            SkriptParser.ParseResult parseResult,
            SectionNode sectionNode,
            List<TriggerItem> triggerItems) {
        account = (Expression<OfflinePlayer>) expressions[0];
        String eventName = getParser().getCurrentEventName();
        Class<? extends Event>[] eventTypes = getParser().getCurrentEvents();
        sectionTrigger =
                loadCode(
                        sectionNode,
                        eventName == null ? "minecore create request section" : eventName,
                        eventTypes == null ? new Class[] {Event.class} : eventTypes);
        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(Event event) {
        RequestExecutionContext.clearLastCreatedRequest();

        RequestManager requestManager = MineCoreSkriptApi.requestManager();
        if (requestManager == null) {
            debug(event, false);
            return getNext();
        }

        OfflinePlayer offlinePlayer = account.getSingle(event);
        if (offlinePlayer == null) {
            debug(event, false);
            return getNext();
        }

        UUID accountUuid = offlinePlayer.getUniqueId();

        List<StoreProduct> products;
        try (RequestExecutionContext.ProductScope scope =
                RequestExecutionContext.withPendingProducts()) {
            sectionTrigger.execute(event);
            products = scope.products();
        }

        if (products.isEmpty()) {
            debug(event, false);
            return getNext();
        }

        StoreRequest request =
                StoreRequest.builder()
                        .mcaccount(accountUuid)
                        .storeProducts(products.toArray(new StoreProduct[0]))
                        .build();

        StoreCreatedRequest createdRequest = requestManager.createRequest(request);
        if (createdRequest != null) {
            RequestExecutionContext.setLastCreatedRequest(createdRequest);
            debug(event, true);
        } else {
            debug(event, false);
        }

        return getNext();
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "create minecore request for " + account.toString(event, debug);
    }
}

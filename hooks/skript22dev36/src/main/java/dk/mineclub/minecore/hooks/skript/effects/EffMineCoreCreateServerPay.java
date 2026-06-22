package dk.mineclub.minecore.hooks.skript.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import dk.mineclub.minecore.api.manager.RequestManager;
import dk.mineclub.minecore.api.model.ServerPayRequest;
import dk.mineclub.minecore.hooks.skript.runtime.MineCoreSkriptApi;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EffMineCoreCreateServerPay extends Effect {
    private Expression<String> mcaccount;
    private Expression<Number> amount;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(
            Expression<?>[] expressions,
            int matchedPattern,
            Kleenean isDelayed,
            SkriptParser.ParseResult parseResult) {
        mcaccount = (Expression<String>) expressions[0];
        amount = (Expression<Number>) expressions[1];
        return true;
    }

    @Override
    protected void execute(Event event) {
        RequestManager requestManager = MineCoreSkriptApi.requestManager();
        if (requestManager == null) {
            return;
        }

        String mcaccountValue = mcaccount.getSingle(event);
        Number amountValue = amount.getSingle(event);
        if (mcaccountValue == null || mcaccountValue.trim().isEmpty() || amountValue == null) {
            return;
        }

        double payAmount = amountValue.doubleValue();
        if (payAmount < 1D) {
            return;
        }

        requestManager.createServerPay(
                ServerPayRequest.builder().mcaccount(mcaccountValue).amount(payAmount).build());
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "create minecore server pay for "
                + mcaccount.toString(event, debug)
                + " with amount "
                + amount.toString(event, debug);
    }
}

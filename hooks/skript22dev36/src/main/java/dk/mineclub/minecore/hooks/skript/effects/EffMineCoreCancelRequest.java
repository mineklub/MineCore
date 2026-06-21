package dk.mineclub.minecore.hooks.skript.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import dk.mineclub.minecore.api.manager.RequestManager;
import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import dk.mineclub.minecore.hooks.skript.runtime.MineCoreSkriptApi;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EffMineCoreCancelRequest extends Effect {
    private Expression<Object> requestInput;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(
            Expression<?>[] expressions,
            int matchedPattern,
            Kleenean isDelayed,
            SkriptParser.ParseResult parseResult) {
        requestInput = (Expression<Object>) expressions[0];
        return true;
    }

    @Override
    protected void execute(Event event) {
        RequestManager requestManager = MineCoreSkriptApi.requestManager();
        if (requestManager == null) {
            return;
        }

        Object input = requestInput.getSingle(event);
        StoreCreatedRequest request = MineCoreSkriptApi.toRequest(input);
        if (request == null) {
            return;
        }

        requestManager.cancelRequest(request);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "cancel minecore request " + requestInput.toString(event, debug);
    }
}

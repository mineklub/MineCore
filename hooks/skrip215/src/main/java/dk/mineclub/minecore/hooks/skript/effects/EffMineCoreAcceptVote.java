package dk.mineclub.minecore.hooks.skript.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import dk.mineclub.minecore.api.manager.RequestManager;
import dk.mineclub.minecore.api.model.MappedVote;
import dk.mineclub.minecore.hooks.skript.runtime.MineCoreSkriptApi;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EffMineCoreAcceptVote extends Effect {
    private Expression<Object> voteInput;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(
            Expression<?>[] expressions,
            int matchedPattern,
            Kleenean isDelayed,
            SkriptParser.ParseResult parseResult) {
        voteInput = (Expression<Object>) expressions[0];
        return true;
    }

    @Override
    protected void execute(Event event) {
        RequestManager requestManager = MineCoreSkriptApi.requestManager();
        if (requestManager == null) {
            return;
        }

        Object input = voteInput.getSingle(event);
        MappedVote vote = MineCoreSkriptApi.toVote(input);
        if (vote == null) {
            return;
        }

        requestManager.acceptVote(vote);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "accept minecore vote " + voteInput.toString(event, debug);
    }
}

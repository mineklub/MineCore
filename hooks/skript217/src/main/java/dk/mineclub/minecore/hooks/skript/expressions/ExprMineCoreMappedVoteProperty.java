package dk.mineclub.minecore.hooks.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import dk.mineclub.minecore.api.model.MappedVote;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprMineCoreMappedVoteProperty extends SimpleExpression<Object> {

    private Expression<MappedVote> mappedVoteExpr;
    private int property;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(
            Expression<?>[] exprs,
            int matchedPattern,
            Kleenean isDelayed,
            ParseResult parseResult) {
        this.mappedVoteExpr = (Expression<MappedVote>) exprs[0];
        this.property = matchedPattern;
        return true;
    }

    @Override
    protected Object[] get(Event event) {
        MappedVote mappedVote = mappedVoteExpr.getSingle(event);
        if (mappedVote == null) {
            return new Object[0];
        }

        Object value =
                switch (property) {
                    case 0 -> mappedVote.getId();
                    case 1 -> mappedVote.getStatus();
                    case 2 -> mappedVote.getCreatedAt();
                    case 3 -> mappedVote.getUpdatedAt();
                    case 4 -> resolveOfflinePlayer(mappedVote);
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
        return property == 4 ? OfflinePlayer.class : String.class;
    }

    private OfflinePlayer resolveOfflinePlayer(MappedVote mappedVote) {
        if (mappedVote.getMcaccount() == null || mappedVote.getMcaccount().getUuid() == null) {
            return null;
        }

        try {
            UUID uuid = UUID.fromString(mappedVote.getMcaccount().getUuid());
            return Bukkit.getOfflinePlayer(uuid);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "mapped vote property";
    }
}

package dk.mineclub.minecore.hooks.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprMineCoreCreatedRequestProperty extends SimpleExpression<Object> {

    private Expression<StoreCreatedRequest> requestExpr;
    private int property;

    // 0=id, 1=service, 2=status server, 3=status client,
    // 4=offlineplayer (from mcaccount), 5=createdAt, 6=updatedAt

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(
            Expression<?>[] exprs,
            int matchedPattern,
            Kleenean isDelayed,
            ParseResult parseResult) {
        this.requestExpr = (Expression<StoreCreatedRequest>) exprs[0];
        this.property = matchedPattern;
        return true;
    }

    @Override
    protected Object[] get(Event event) {
        StoreCreatedRequest request = requestExpr.getSingle(event);
        if (request == null) {
            return new Object[0];
        }

        Object value;
        switch (property) {
            case 0:
                value = request.getId();
                break;
            case 1:
                value = request.getService();
                break;
            case 2:
                value = request.getStatus() == null ? null : request.getStatus().getServer();
                break;
            case 3:
                value = request.getStatus() == null ? null : request.getStatus().getClient();
                break;
            case 4:
                value = request.getCreatedAt();
                break;
            case 5:
                value = request.getUpdatedAt();
                break;
            default:
                value = null;
                break;
        }

        return value == null ? new Object[0] : new Object[] {value};
    }

    private OfflinePlayer resolveOfflinePlayer(StoreCreatedRequest request) {
        if (request.getMcaccount() == null || request.getMcaccount().getUuid() == null) {
            return null;
        }

        try {
            UUID uuid = UUID.fromString(request.getMcaccount().getUuid());
            return Bukkit.getOfflinePlayer(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<?> getReturnType() {
        return property == 4 ? OfflinePlayer.class : String.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "created request property";
    }
}

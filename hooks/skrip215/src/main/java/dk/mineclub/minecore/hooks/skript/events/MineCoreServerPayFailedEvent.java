package dk.mineclub.minecore.hooks.skript.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

public class MineCoreServerPayFailedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final OfflinePlayer offlinePlayer;
    private final double amount;
    @Nullable private final String message;

    public MineCoreServerPayFailedEvent(
            OfflinePlayer offlinePlayer, double amount, @Nullable String message) {
        this.offlinePlayer = offlinePlayer;
        this.amount = amount;
        this.message = message;
    }

    public OfflinePlayer getOfflinePlayer() {
        return offlinePlayer;
    }

    public double getAmount() {
        return amount;
    }

    public @Nullable String getMessage() {
        return message;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

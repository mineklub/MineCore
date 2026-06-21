package dk.mineclub.minecore.internal.handler;

import com.velocitypowered.api.proxy.Player;
import dk.mineclub.minecore.internal.InternalPlugin;
import dk.mineclub.minecore.internal.channels.StoreRequestMessage;

public class NewVersionHandler {
    // TODO: Waiting on velocity supporting dialog
    public static void handle(InternalPlugin plugin, Player player, StoreRequestMessage message) {
        OldVersionHandler.handle(plugin, player, message);
    }
}

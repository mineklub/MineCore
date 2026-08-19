package dk.mineclub.minecore.platform.common.staff;

import dk.mineclub.minecore.api.MineCoreApi;
import io.socket.client.Socket;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class StaffCommandBridge {

    private static final Logger LOGGER = Logger.getLogger(StaffCommandBridge.class.getName());
    private static final String EVENT = "staffcommand";

    private static final int MAX_ATTEMPTS = 10;
    private static final long RETRY_TICKS = 40L;

    private StaffCommandBridge() {}

    public static void register(JavaPlugin plugin, MineCoreApi api) {
        attach(plugin, api, 1);
    }

    private static void attach(JavaPlugin plugin, MineCoreApi api, int attempt) {
        Socket socket = api.getSocketIOManager().getSocket();

        if (socket == null) {
            if (attempt >= MAX_ATTEMPTS) {
                LOGGER.warning("Staff-kommandoer er ikke aktive: ingen socket-forbindelse");
                return;
            }
            Bukkit.getScheduler()
                    .runTaskLater(plugin, () -> attach(plugin, api, attempt + 1), RETRY_TICKS);
            return;
        }

        socket.on(EVENT, args -> handle(plugin, api, args));
        LOGGER.info("Staff-kommandoer fra panelet er aktive");
    }

    private static void handle(JavaPlugin plugin, MineCoreApi api, Object... args) {
        if (args.length == 0 || args[0] == null) {
            return;
        }

        final Payload payload;
        try {
            payload = api.getGson().fromJson(String.valueOf(args[0]), Payload.class);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Kunne ikke laese staffcommand: " + ex.getMessage(), ex);
            return;
        }

        if (payload == null
                || payload.uuid == null
                || payload.command == null
                || payload.command.isEmpty()) {
            return;
        }

        final UUID uuid;
        try {
            uuid = UUID.fromString(payload.uuid);
        } catch (IllegalArgumentException ex) {
            return;
        }

        Bukkit.getScheduler()
                .runTask(
                        plugin,
                        () -> {
                            Player player = Bukkit.getPlayer(uuid);
                            if (player == null) {
                                LOGGER.info(
                                        "staffcommand ignoreret: "
                                                + payload.username
                                                + " er ikke online paa denne server");
                                return;
                            }

                            LOGGER.info(
                                    "Panel-kommando fra "
                                            + player.getName()
                                            + ": /"
                                            + payload.command);
                            Bukkit.dispatchCommand(player, payload.command);
                        });
    }

    private static final class Payload {
        String uuid;
        String username;
        String command;
    }
}

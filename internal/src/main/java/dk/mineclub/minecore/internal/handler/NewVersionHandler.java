package dk.mineclub.minecore.internal.handler;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.chat.clickevent.CustomClickEvent;
import com.github.retrooper.packetevents.protocol.dialog.CommonDialogData;
import com.github.retrooper.packetevents.protocol.dialog.ConfirmationDialog;
import com.github.retrooper.packetevents.protocol.dialog.DialogAction;
import com.github.retrooper.packetevents.protocol.dialog.action.StaticAction;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessage;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessageDialogBody;
import com.github.retrooper.packetevents.protocol.dialog.button.ActionButton;
import com.github.retrooper.packetevents.protocol.dialog.button.CommonButtonData;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.configuration.client.WrapperConfigClientCustomClickAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCustomClickAction;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerShowDialog;
import com.velocitypowered.api.proxy.Player;
import dk.mineclub.minecore.internal.InternalPlugin;
import dk.mineclub.minecore.internal.channels.StoreRequestMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.NotNull;

public class NewVersionHandler {
    private static final ResourceLocation REQUEST_ACTION_ID =
            new ResourceLocation("minecore", "request_action");
    private static final Map<String, PendingRequest> PENDING_REQUESTS = new ConcurrentHashMap<>();
    private static volatile boolean packetListenerRegistered;
    private static volatile boolean packetEventsWarningLogged;

    public static void handle(InternalPlugin plugin, Player player, StoreRequestMessage message) {
        if (!isPacketEventsReady(plugin)) {
            OldVersionHandler.handle(plugin, player, message);
            return;
        }

        registerPacketListener(plugin);
        PendingRequest pendingRequest = cachePendingRequest(player, message);
        PacketEvents.getAPI()
                .getPlayerManager()
                .sendPacket(
                        player,
                        new WrapperPlayServerShowDialog(createDialog(plugin, pendingRequest)));
    }

    private static boolean isPacketEventsReady(InternalPlugin plugin) {
        try {
            PacketEvents.getAPI().getPlayerManager();
            return true;
        } catch (Exception ignored) {
            // PacketEvents is not initialized yet, fall back below.
        }

        if (!packetEventsWarningLogged) {
            packetEventsWarningLogged = true;
            plugin.getLogger()
                    .warn(
                            "PacketEvents API is not initialized; falling back to the legacy request UI.");
        }

        return false;
    }

    private static ConfirmationDialog createDialog(
            InternalPlugin plugin, PendingRequest pendingRequest) {
        StoreRequestMessage message = pendingRequest.message();
        Component title = createDialogTitle(plugin, message);
        Component body = createDialogBody(plugin, message);

        CommonDialogData common =
                new CommonDialogData(
                        title,
                        Component.empty(),
                        true,
                        false,
                        DialogAction.CLOSE,
                        List.of(new PlainMessageDialogBody(new PlainMessage(body, 240))),
                        List.of());

        ActionButton acceptButton =
                new ActionButton(
                        new CommonButtonData(
                                plugin.getLang().get("request.accept-button"),
                                Component.empty(),
                                130),
                        new StaticAction(
                                new CustomClickEvent(
                                        REQUEST_ACTION_ID,
                                        createPayload(
                                                "accept",
                                                message.data().id(),
                                                pendingRequest.token()))));
        ActionButton denyButton =
                new ActionButton(
                        new CommonButtonData(
                                plugin.getLang().get("request.deny-button"),
                                Component.empty(),
                                130),
                        new StaticAction(
                                new CustomClickEvent(
                                        REQUEST_ACTION_ID,
                                        createPayload(
                                                "deny",
                                                message.data().id(),
                                                pendingRequest.token()))));

        return new ConfirmationDialog(common, acceptButton, denyButton);
    }

    private static Component createDialogTitle(InternalPlugin plugin, StoreRequestMessage message) {
        List<Component> headerLines =
                plugin.getLang()
                        .getList(
                                "request.header",
                                Placeholder.unparsed(
                                        "server", message.data().service().nicename()));
        return headerLines.isEmpty() ? Component.text("MINECORE") : headerLines.getFirst();
    }

    private static Component createDialogBody(InternalPlugin plugin, StoreRequestMessage message) {
        List<Component> lines = new ArrayList<>();
        List<Component> headerLines =
                plugin.getLang()
                        .getList(
                                "request.header",
                                Placeholder.unparsed(
                                        "server", message.data().service().nicename()));

        if (headerLines.size() > 1) {
            lines.addAll(headerLines.subList(1, headerLines.size()));
        }

        for (StoreRequestMessage.Product product : message.data().products()) {
            lines.add(
                    plugin.getLang()
                            .get(
                                    product.quantity() > 1
                                            ? "request.product-quantity"
                                            : "request.product",
                                    Placeholder.parsed("name", product.productName()),
                                    Formatter.number(
                                            "price",
                                            Double.parseDouble(product.price().numberDecimal())),
                                    Formatter.number("quantity", product.quantity())));
        }

        lines.add(
                plugin.getLang()
                        .get(
                                "request.dialog-footer",
                                Formatter.number("total", calculateTotal(message)),
                                Placeholder.component(
                                        "accept", plugin.getLang().get("request.accept-button")),
                                Placeholder.component(
                                        "deny", plugin.getLang().get("request.deny-button")),
                                Formatter.number("balance", message.data().mcaccount().balance())));

        return Component.join(JoinConfiguration.newlines(), lines);
    }

    private static double calculateTotal(StoreRequestMessage message) {
        return message.data().products().stream()
                .map(StoreRequestMessage.Product::price)
                .map(StoreRequestMessage.DecimalValue::numberDecimal)
                .map(Double::parseDouble)
                .reduce(0.0, Double::sum);
    }

    private static PendingRequest cachePendingRequest(Player player, StoreRequestMessage message) {
        PendingRequest pendingRequest = new PendingRequest(message, UUID.randomUUID().toString());
        PENDING_REQUESTS.put(player.getUniqueId() + ":" + message.data().id(), pendingRequest);
        return pendingRequest;
    }

    private static NBTCompound createPayload(String action, String requestId, String token) {
        NBTCompound payload = new NBTCompound();
        payload.setTag("action", new NBTString(action));
        payload.setTag("request_id", new NBTString(requestId));
        payload.setTag("token", new NBTString(token));
        return payload;
    }

    private static void registerPacketListener(InternalPlugin plugin) {
        if (packetListenerRegistered) {
            return;
        }

        synchronized (NewVersionHandler.class) {
            if (packetListenerRegistered) {
                return;
            }

            PacketEvents.getAPI()
                    .getEventManager()
                    .registerListener(
                            new PacketListenerAbstract() {
                                @Override
                                public void onPacketReceive(@NotNull PacketReceiveEvent event) {
                                    if (event.getPacketType()
                                                    == PacketType.Play.Client.CUSTOM_CLICK_ACTION
                                            || event.getPacketType()
                                                    == PacketType.Configuration.Client
                                                            .CUSTOM_CLICK_ACTION) {
                                        Player player =
                                                plugin.getServer()
                                                        .getPlayer(event.getUser().getUUID())
                                                        .orElse(null);
                                        if (player == null) {
                                            return;
                                        }

                                        if (event.getPacketType()
                                                == PacketType.Play.Client.CUSTOM_CLICK_ACTION) {
                                            WrapperPlayClientCustomClickAction wrapper =
                                                    new WrapperPlayClientCustomClickAction(event);
                                            handleCustomClickAction(
                                                    plugin,
                                                    player,
                                                    wrapper.getId(),
                                                    wrapper.getPayload());
                                        } else {
                                            WrapperConfigClientCustomClickAction wrapper =
                                                    new WrapperConfigClientCustomClickAction(event);
                                            handleCustomClickAction(
                                                    plugin,
                                                    player,
                                                    wrapper.getId(),
                                                    wrapper.getPayload());
                                        }
                                    }
                                }
                            });

            packetListenerRegistered = true;
        }
    }

    private static void handleCustomClickAction(
            InternalPlugin plugin,
            Player player,
            ResourceLocation id,
            com.github.retrooper.packetevents.protocol.nbt.NBT payload) {
        if (!REQUEST_ACTION_ID.equals(id) || !(payload instanceof NBTCompound compound)) {
            return;
        }

        String action = compound.getStringTagValueOrNull("action");
        String requestId = compound.getStringTagValueOrNull("request_id");
        String token = compound.getStringTagValueOrNull("token");
        if (action == null || requestId == null || token == null) {
            return;
        }

        StoreRequestMessage message = popPendingRequest(player, requestId, token);
        if (message == null) {
            return;
        }

        if ("accept".equals(action)) {
            OldVersionHandler.handleRequestResponse(
                    plugin,
                    player,
                    () -> plugin.acceptRequest(message.data().id()),
                    OldVersionHandler::getAcceptErrorKey,
                    "request.accept-errors.network",
                    "request.accept-errors.unexpected",
                    "request.accept-errors.unknown");
            OldVersionHandler.publishReturn(plugin, message);
            return;
        }

        if ("deny".equals(action)) {
            OldVersionHandler.handleRequestResponse(
                    plugin,
                    player,
                    () -> plugin.cancelRequest(message.data().id()),
                    OldVersionHandler::getCancelErrorKey,
                    "request.cancel-errors.network",
                    "request.cancel-errors.unexpected",
                    "request.cancel-errors.unknown");
            OldVersionHandler.publishReturn(plugin, message);
        }
    }

    private static StoreRequestMessage popPendingRequest(
            Player player, String requestId, String token) {
        String key = player.getUniqueId() + ":" + requestId;
        PendingRequest pendingRequest = PENDING_REQUESTS.get(key);
        if (pendingRequest == null || !pendingRequest.token().equals(token)) {
            return null;
        }

        PENDING_REQUESTS.remove(key, pendingRequest);
        return pendingRequest.message();
    }

    public static int cancelPendingRequestsForPlayer(InternalPlugin plugin, Player player) {
        return plugin.cancelPendingRequestsForPlayer(
                PENDING_REQUESTS, player, PendingRequest::message, "new-version");
    }

    private record PendingRequest(StoreRequestMessage message, String token) {}
}

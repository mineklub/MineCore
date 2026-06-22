package dk.mineclub.minecore.internal.handler;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.Player;
import dk.mineclub.minecore.internal.InternalPlugin;
import dk.mineclub.minecore.internal.channels.StoreRequestMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import okhttp3.Response;

public class OldVersionHandler {
    private static final Gson GSON = new Gson();

    static String getAcceptErrorKey(String apiMessage) {
        if (apiMessage == null || apiMessage.isBlank()) {
            return "request.accept-errors.unknown";
        }

        return switch (apiMessage.trim().toLowerCase()) {
            case "request not found" -> "request.accept-errors.request-not-found";
            case "mcaccount not found" -> "request.accept-errors.mcaccount-not-found";
            case "request is already accepted" -> "request.accept-errors.request-already-accepted";
            case "cannot accept a cancelled request" ->
                    "request.accept-errors.cannot-accept-cancelled-request";
            case "player is not online" -> "request.accept-errors.player-not-online";
            case "distribution not found" -> "request.accept-errors.distribution-not-found";
            case "insufficient balance" -> "request.accept-errors.insufficient-balance";
            default -> "request.accept-errors.unknown";
        };
    }

    static String getCancelErrorKey(String apiMessage) {
        if (apiMessage == null || apiMessage.isBlank()) {
            return "request.cancel-errors.unknown";
        }

        return switch (apiMessage.trim().toLowerCase()) {
            case "request not found" -> "request.cancel-errors.request-not-found";
            case "mcaccount not found" -> "request.cancel-errors.mcaccount-not-found";
            case "cannot cancel an accepted request" ->
                    "request.cancel-errors.cannot-cancel-accepted-request";
            case "request is already cancelled" ->
                    "request.cancel-errors.request-already-cancelled";
            default -> "request.cancel-errors.unknown";
        };
    }

    private static String parseErrorMessage(Response response) {
        try {
            JsonObject jsonObject = GSON.fromJson(response.body().string(), JsonObject.class);
            if (jsonObject == null) {
                return null;
            }

            JsonElement messageElement = jsonObject.get("message");
            if (messageElement == null || !messageElement.isJsonPrimitive()) {
                return null;
            }

            return messageElement.getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static void sendUnknownError(
            InternalPlugin plugin, Player player, String unknownKey, String apiMessage) {
        player.sendMessage(
                plugin.getLang()
                        .get(
                                unknownKey,
                                Placeholder.unparsed(
                                        "error", apiMessage == null ? "Unknown" : apiMessage)));
    }

    static void handleRequestResponse(
            InternalPlugin plugin,
            Player player,
            Supplier<Response> action,
            Function<String, String> errorKeyResolver,
            String networkKey,
            String unexpectedKey,
            String unknownKey) {
        try (Response response = action.get()) {
            if (response == null) {
                player.sendMessage(plugin.getLang().get(networkKey));
                return;
            }

            if (!response.isSuccessful()) {
                String apiMessage = parseErrorMessage(response);
                String errorKey = errorKeyResolver.apply(apiMessage);

                if (unknownKey.equals(errorKey)) {
                    sendUnknownError(plugin, player, unknownKey, apiMessage);
                } else {
                    player.sendMessage(plugin.getLang().get(errorKey));
                }
            }
        } catch (Exception ignored) {
            player.sendMessage(plugin.getLang().get(unexpectedKey));
        }
    }

    static void publishReturn(InternalPlugin plugin, StoreRequestMessage message) {
        JsonObject object = new JsonObject();
        object.addProperty("service", message.data().service().id());
        object.addProperty("mcaccount", message.data().mcaccount().uuid());
        plugin.getJedis().publish("MINECORE:RETURN", object.toString());
    }

    public static void handle(InternalPlugin plugin, Player player, StoreRequestMessage message) {
        Component header =
                plugin.getLang()
                        .get(
                                "request.header",
                                Placeholder.unparsed(
                                        "server", message.data().service().nicename()));
        List<Component> products = new ArrayList<>();
        for (StoreRequestMessage.Product product : message.data().products()) {
            Component productComp =
                    plugin.getLang()
                            .get(
                                    product.quantity() > 1
                                            ? "request.product-quantity"
                                            : "request.product",
                                    Placeholder.unparsed("name", product.productName()),
                                    Formatter.number(
                                            "price",
                                            Double.parseDouble(product.price().numberDecimal())),
                                    Formatter.number("quantity", product.quantity()));
            products.add(productComp);
        }
        Component footer =
                plugin.getLang()
                        .get(
                                "request.footer",
                                Formatter.number(
                                        "total",
                                        message.data().products().stream()
                                                .map(StoreRequestMessage.Product::price)
                                                .map(
                                                        StoreRequestMessage.DecimalValue
                                                                ::numberDecimal)
                                                .map(Double::parseDouble)
                                                .reduce(0.0, Double::sum)),
                                Placeholder.component(
                                        "accept",
                                        plugin.getLang()
                                                .get("request.accept-button")
                                                .clickEvent(
                                                        ClickEvent.callback(
                                                                ignored -> {
                                                                    handleRequestResponse(
                                                                            plugin,
                                                                            player,
                                                                            () ->
                                                                                    plugin
                                                                                            .acceptRequest(
                                                                                                    message),
                                                                            OldVersionHandler
                                                                                    ::getAcceptErrorKey,
                                                                            "request.accept-errors.network",
                                                                            "request.accept-errors.unexpected",
                                                                            "request.accept-errors.unknown");
                                                                    publishReturn(plugin, message);
                                                                }))),
                                Placeholder.component(
                                        "deny",
                                        plugin.getLang()
                                                .get("request.deny-button")
                                                .clickEvent(
                                                        ClickEvent.callback(
                                                                ignored -> {
                                                                    handleRequestResponse(
                                                                            plugin,
                                                                            player,
                                                                            () ->
                                                                                    plugin
                                                                                            .cancelRequest(
                                                                                                    message),
                                                                            OldVersionHandler
                                                                                    ::getCancelErrorKey,
                                                                            "request.cancel-errors.network",
                                                                            "request.cancel-errors.unexpected",
                                                                            "request.cancel-errors.unknown");
                                                                    publishReturn(plugin, message);
                                                                }))),
                                Formatter.number("balance", message.data().mcaccount().balance()));

        ComponentBuilder<TextComponent, TextComponent.Builder> builder = Component.text();

        builder.append(header);

        for (Component product : products) {
            builder.append(Component.newline());
            builder.append(product);
        }

        builder.append(Component.newline());
        builder.append(footer);

        player.sendMessage(builder.build());
    }
}

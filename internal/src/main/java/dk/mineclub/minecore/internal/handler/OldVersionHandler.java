package dk.mineclub.minecore.internal.handler;

import com.velocitypowered.api.proxy.Player;
import dk.mineclub.minecore.internal.InternalPlugin;
import dk.mineclub.minecore.internal.channels.StoreRequestMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.ArrayList;
import java.util.List;

public class OldVersionHandler {
	public static void handle(InternalPlugin plugin, Player player, StoreRequestMessage message) {
		Component header = plugin.getLang().get("request.header", Placeholder.unparsed("server", message.data().service().nicename()));
		List<Component> products = new ArrayList<>();
		for (StoreRequestMessage.Product product : message.data().products()) {
			Component productComp = plugin.getLang().get(product.quantity() > 1 ? "request.product-quantity" : "request.product",
				Placeholder.unparsed("name", product.productName()),
				Formatter.number("price", Double.parseDouble(product.price().numberDecimal())),
				Formatter.number("quantity", product.quantity()));
			products.add(productComp);
		}
		Component footer = plugin.getLang().get("request.footer",
			Formatter.number("total", message.data().products().stream()
				.map(StoreRequestMessage.Product::price)
				.map(StoreRequestMessage.DecimalValue::numberDecimal)
				.map(Double::parseDouble)
				.reduce(0.0, Double::sum)),
			Placeholder.component("accept", plugin.getLang().get("request.accept-button").clickEvent(ClickEvent.callback(_ -> {
				player.sendMessage(Component.text("JAJA"));
			}))),
			Placeholder.component("deny", plugin.getLang().get("request.deny-button").clickEvent(ClickEvent.callback(_ -> {
				player.sendMessage(Component.text("NEJNEJ"));
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

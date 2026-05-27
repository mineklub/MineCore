package dk.mineclub.minecore.example.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dk.mineclub.minecore.api.model.StoreProduct;
import dk.mineclub.minecore.api.model.StoreProductQuantity;
import dk.mineclub.minecore.api.model.StoreRequest;
import dk.mineclub.minecore.example.ExamplePlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

public class BuyCommand {
    public LiteralCommandNode<CommandSourceStack> createCommand(String name, ExamplePlugin plugin) {
        return Commands.literal(name)
                .executes(
                        context -> {
                            if (!(context.getSource().getSender() instanceof Player player)) {
                                return Command.SINGLE_SUCCESS;
                            }

                            StoreProduct product =
                                    StoreProduct.builder()
                                            .name("Example Product")
                                            .id("example_product")
                                            .price(1)
                                            .quantity(
                                                    StoreProductQuantity.builder()
                                                            .min(1)
                                                            .value(1)
                                                            .max(1)
                                                            .build())
                                            .build();
                            StoreProduct[] storeProducts = new StoreProduct[] {product};
                            StoreRequest storeRequest =
                                    StoreRequest.builder()
                                            .storeProducts(storeProducts)
                                            .mcaccount(player.getUniqueId())
                                            .build();
                            plugin.api.getMinecoreRequestManager().createRequest(storeRequest);
                            return Command.SINGLE_SUCCESS;
                        })
                .build();
    }
}

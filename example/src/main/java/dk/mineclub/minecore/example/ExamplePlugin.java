package dk.mineclub.minecore.example;

import dk.mineclub.minecore.api.MineCoreApi;
import dk.mineclub.minecore.example.commands.BuyCommand;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

public class ExamplePlugin extends JavaPlugin {
    public MineCoreApi api;

    @Override
    public void onEnable() {
        api = new MineCoreApi();
        this.getLifecycleManager()
                .registerEventHandler(
                        LifecycleEvents.COMMANDS,
                        commands -> {
                            commands.registrar()
                                    .register(new BuyCommand().createCommand("buy", this));
                        });
    }
}

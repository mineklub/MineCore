package dk.mineclub.minecore.example.listeners;

import com.google.common.eventbus.Subscribe;
import dk.mineclub.minecore.api.MineCoreApi;
import dk.mineclub.minecore.api.events.PostCreateRequestEvent;
import dk.mineclub.minecore.api.events.PreCreateRequestEvent;
import dk.mineclub.minecore.api.events.ReceiveRequestEvent;
import dk.mineclub.minecore.example.ExamplePlugin;

public class StoreListeners {
    private MineCoreApi mineCoreApi;

    public StoreListeners(MineCoreApi mineCoreApi) {
        this.mineCoreApi = mineCoreApi;
    }

    @Subscribe
    public void preEvent(PreCreateRequestEvent event) {
        System.out.println(
                "PreCreateRequestEvent called for request: "
                        + event.toString());
    }

    @Subscribe
    public void postEvent(PostCreateRequestEvent event) {
        System.out.println(
                "PostCreateRequestEvent called for request: " + event.toString());
    }

    @Subscribe
    public void receiveEvent(ReceiveRequestEvent event) {
        System.out.println("ReceiveRequestEvent called for request: " + event.toString());
        mineCoreApi.getMinecoreRequestManager().acceptRequest(event.getStoreRequest());
    }
}

package dk.mineclub.minecore.example.listeners;

import com.google.common.eventbus.Subscribe;
import dk.mineclub.minecore.api.events.PostCreateRequestEvent;
import dk.mineclub.minecore.api.events.PreCreateRequestEvent;
import dk.mineclub.minecore.api.events.ReceiveRequestEvent;

public class StoreListeners {
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
    }
}

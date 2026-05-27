package dk.mineclub.minecore.api;

import com.google.common.eventbus.AsyncEventBus;
import com.google.gson.Gson;
import dk.mineclub.minecore.api.manager.RequestManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.Getter;

public final class MineCoreApi {
    @Getter private static MineCoreApi instance;
    @Getter private AsyncEventBus asyncEventBus;
    @Getter private RequestManager minecoreRequestManager = new RequestManager(this);
    @Getter private final Gson gson = new Gson();

    public MineCoreApi() {
        instance = this;
        ExecutorService executor = Executors.newCachedThreadPool();
        this.asyncEventBus = new AsyncEventBus(executor);
    }
}

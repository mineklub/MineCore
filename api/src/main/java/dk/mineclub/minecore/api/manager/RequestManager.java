package dk.mineclub.minecore.api.manager;

import dk.mineclub.minecore.api.MineCoreApi;
import dk.mineclub.minecore.api.events.PostCreateRequestEvent;
import dk.mineclub.minecore.api.events.PreCreateRequestEvent;
import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import dk.mineclub.minecore.api.model.StoreRequest;
import java.util.concurrent.TimeUnit;
import okhttp3.*;
import org.jspecify.annotations.Nullable;

public class RequestManager {
    private final MineCoreApi mineCoreApi;
    private static final OkHttpClient client =
            new OkHttpClient()
                    .newBuilder()
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .build();
    private final String baseUrl = "https://api.mineclub.dk/v2/minecore";

    public RequestManager(MineCoreApi mineCoreApi) {
        this.mineCoreApi = mineCoreApi;
    }

    public @Nullable StoreCreatedRequest createRequest(StoreRequest storeRequest) {
        PreCreateRequestEvent event = new PreCreateRequestEvent(storeRequest);
        boolean cancelled = event.callEvent();
        if (cancelled) {
            return null;
        }

        String token = System.getenv("TOKEN");
        RequestBody requestBody =
                RequestBody.create(
                        storeRequest.toJson().toString(), MediaType.parse("application/json"));
        Request request =
                new Request.Builder()
                        .url(baseUrl + "/server/request")
                        .post(requestBody)
                        .header("Authorization", "Bearer " + token)
                        .build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            String responseBody = body.string();
            if (response.isSuccessful()) {
                StoreCreatedRequest createdRequest =
                        mineCoreApi.getGson().fromJson(responseBody, StoreCreatedRequest.class);
                PostCreateRequestEvent PostEvent = new PostCreateRequestEvent(createdRequest);
                PostEvent.callEvent();
                return createdRequest;
            }

            System.out.println(responseBody);
        } catch (Exception ex) {
            System.out.println("Failed to create request, " + ex.getMessage());
        }

        return null;
    }
}

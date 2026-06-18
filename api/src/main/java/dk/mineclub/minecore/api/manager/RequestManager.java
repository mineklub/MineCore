package dk.mineclub.minecore.api.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import dk.mineclub.minecore.api.MineCoreApi;
import dk.mineclub.minecore.api.events.PostCreateRequestEvent;
import dk.mineclub.minecore.api.events.PreCreateRequestEvent;
import dk.mineclub.minecore.api.model.GetRequestsOptions;
import dk.mineclub.minecore.api.model.GetRequestsResponse;
import dk.mineclub.minecore.api.model.MappedRequest;
import dk.mineclub.minecore.api.model.RequestActionResponse;
import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import dk.mineclub.minecore.api.model.StoreRequest;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.List;
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

        String token = mineCoreApi.getToken();
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

    public @Nullable RequestActionResponse acceptRequest(StoreCreatedRequest storeCreatedRequest) {
        String token = mineCoreApi.getToken();
        Request request =
                new Request.Builder()
                        .url(baseUrl + "/server/request/" + storeCreatedRequest.getId() + "/accept")
                        .post(RequestBody.create(new byte[0]))
                        .header("Authorization", "Bearer " + token)
                        .build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            return mineCoreApi.getGson().fromJson(body.string(), RequestActionResponse.class);
        } catch (Exception ex) {
            System.out.println("Failed to accept request, " + ex.getMessage());
        }

        return null;
    }

    public @Nullable RequestActionResponse cancelRequest(StoreCreatedRequest storeCreatedRequest) {
        String token = mineCoreApi.getToken();
        Request request =
                new Request.Builder()
                        .url(baseUrl + "/server/request/" + storeCreatedRequest.getId() + "/cancel")
                        .post(RequestBody.create(new byte[0]))
                        .header("Authorization", "Bearer " + token)
                        .build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            return mineCoreApi.getGson().fromJson(body.string(), RequestActionResponse.class);
        } catch (Exception ex) {
            System.out.println("Failed to cancel request, " + ex.getMessage());
        }

        return null;
    }

    public @Nullable GetRequestsResponse getRequests(@Nullable GetRequestsOptions options) {
        String token = mineCoreApi.getToken();

        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "/server/request").newBuilder();

        if (options != null) {
            if (options.getPage() != null)
                urlBuilder.addQueryParameter("page", String.valueOf(options.getPage()));
            if (options.getLimit() != null)
                urlBuilder.addQueryParameter("limit", String.valueOf(options.getLimit()));
            if (options.getClientStatus() != null)
                urlBuilder.addQueryParameter("clientStatus", options.getClientStatus().toString());
            if (options.getServerStatus() != null)
                urlBuilder.addQueryParameter("serverStatus", options.getServerStatus().toString());
            if (options.getSortBy() != null)
                urlBuilder.addQueryParameter("sortBy", options.getSortBy().toString());
            if (options.getOrder() != null)
                urlBuilder.addQueryParameter("order", options.getOrder().toString());
            if (options.getMcaccount() != null)
                urlBuilder.addQueryParameter("mcaccount", options.getMcaccount());
            if (options.getFrom() != null) {
                urlBuilder.addQueryParameter(
                        "from",
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                .format(options.getFrom()));
            }
            if (options.getTo() != null) {
                urlBuilder.addQueryParameter(
                        "to",
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                .format(options.getTo()));
            }
            if (options.getWithMeta() != null)
                urlBuilder.addQueryParameter("withMeta", String.valueOf(options.getWithMeta()));
            if (options.getIncludeProducts() != null)
                urlBuilder.addQueryParameter(
                        "includeProducts", String.valueOf(options.getIncludeProducts()));
        }

        Request request =
                new Request.Builder()
                        .url(urlBuilder.build())
                        .get()
                        .header("Authorization", "Bearer " + token)
                        .build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (body == null) return null;
            String responseBody = body.string();

            JsonElement element = mineCoreApi.getGson().fromJson(responseBody, JsonElement.class);

            // Plain array response (withMeta=false or not set)
            if (element instanceof JsonArray) {
                Type listType = new TypeToken<List<MappedRequest>>() {}.getType();
                List<MappedRequest> list = mineCoreApi.getGson().fromJson(element, listType);
                GetRequestsResponse wrapper = new GetRequestsResponse();
                wrapper.setRequests(list);
                return wrapper;
            }

            // Object response (withMeta=true)
            return mineCoreApi.getGson().fromJson(element, GetRequestsResponse.class);
        } catch (Exception ex) {
            System.out.println("Failed to get requests, " + ex.getMessage());
        }

        return null;
    }
}

package dk.mineclub.minecore.api.manager;

import com.google.gson.JsonObject;
import dk.mineclub.minecore.api.MineCoreApi;
import dk.mineclub.minecore.api.events.PreCreateRequestEvent;
import dk.mineclub.minecore.api.model.StoreRequest;
import okhttp3.*;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RequestManager {
	private MineCoreApi mineCoreApi;
	private static final OkHttpClient client =
		new OkHttpClient()
			.newBuilder()
			.readTimeout(1, TimeUnit.MINUTES)
			.writeTimeout(1, TimeUnit.MINUTES)
			.build();
	private String baseUrl = "https://api.mineclub.dk/v2/minecore";

	public RequestManager(MineCoreApi mineCoreApi) {
		this.mineCoreApi = mineCoreApi;
	}

	public @Nullable JsonObject createRequest(StoreRequest storeRequest, UUID mcaccount) {
		PreCreateRequestEvent event = new PreCreateRequestEvent(storeRequest, mcaccount);
		boolean cancelled = event.callEvent();
		if (cancelled) {
			return null;
		}

		String token = System.getenv("TOKEN");
		RequestBody requestBody = RequestBody.create(storeRequest.toJson().toString(), MediaType.parse("application/json"));
		Request request =
			new Request.Builder()
				.url(baseUrl + "/server/request")
				.post(requestBody)
				.header("Authorization", token)
				.build();

		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful()) {
				return mineCoreApi.getGson().fromJson(response.body().string(), JsonObject.class);
			}

			return null;
		} catch (Exception _) {

		}

		return null;
	}
}


package dk.mineclub.minecore.common.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dk.mineclub.minecore.common.dto.CreateMinecoreRequestDto;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.extern.java.Log;
import okhttp3.*;

/** Manager for handling Minecore server requests via the API. */
@Log
public class MinecoreRequestManager {

    private static final String BASE_URL = "https://api.mineclub.dk";
    private static final String ENDPOINT = "/v2/minecore/server/request";
    private static final String CONTENT_TYPE_JSON = "application/json";

    private final OkHttpClient httpClient;
    private final Gson gson;

    public MinecoreRequestManager() {
        this.httpClient =
                new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build();
        this.gson = new GsonBuilder().serializeNulls().create();
    }

    /**
     * Create a Minecore request with the given token and request data.
     *
     * @param token The MineCoreToken for authentication
     * @param requestDto The request DTO containing products and metadata
     * @return The server response as a string
     * @throws IOException if the HTTP request fails
     */
    public String createRequest(String token, CreateMinecoreRequestDto requestDto)
            throws IOException {
        // Validate the request DTO
        if (requestDto == null
                || requestDto.getMcaccount() == null
                || requestDto.getProducts() == null
                || requestDto.getProducts().isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid request: mcaccount and products are required");
        }

        // Create JSON request body
        String jsonBody = gson.toJson(requestDto);
        RequestBody body = RequestBody.create(jsonBody, MediaType.parse(CONTENT_TYPE_JSON));

        // Build the request with authorization header
        Request request =
                new Request.Builder()
                        .url(BASE_URL + ENDPOINT)
                        .post(body)
                        .addHeader("Authorization", "Bearer " + token)
                        .addHeader("Content-Type", CONTENT_TYPE_JSON)
                        .build();

        // Execute the request
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body().string();
                throw new IOException(
                        "Failed to create Minecore request. Status: "
                                + response.code()
                                + ", Body: "
                                + errorBody);
            }

            String responseBody = response.body().string();
            log.info(
                    "Minecore request created successfully for account: "
                            + requestDto.getMcaccount());
            return responseBody;
        }
    }

    /** Response object wrapper for Minecore API responses. */
    @Data
    public static class MinecoreResponse {
        private String id;
        private String status;
        private Object data;
    }
}

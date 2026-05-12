package dk.mineclub.minecore.api.service;

import dk.mineclub.minecore.common.dto.CreateMinecoreRequestDto;
import dk.mineclub.minecore.common.manager.MinecoreRequestManager;
import java.io.IOException;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.java.Log;

/** Service for handling Minecore server requests through the API. */
@Log
public class MinecoreServerService {

    private final MinecoreRequestManager requestManager;

    /** Constructs a new MinecoreServerService with a default MinecoreRequestManager. */
    public MinecoreServerService() {
        this.requestManager = new MinecoreRequestManager();
    }

    /**
     * Create a Minecore server request.
     *
     * @param token The authentication token for the Minecore API
     * @param requestDto The request data containing products and metadata
     * @return The response from the Minecore API
     */
    public MinecoreServerResponse createMinecoreRequest(
            @NonNull String token, @NonNull CreateMinecoreRequestDto requestDto) {
        try {
            String responseBody = requestManager.createRequest(token, requestDto);
            return MinecoreServerResponse.success(responseBody);
        } catch (IllegalArgumentException e) {
            log.warning("Invalid Minecore request: " + e.getMessage());
            return MinecoreServerResponse.validationError(e.getMessage());
        } catch (IOException e) {
            log.severe("Failed to create Minecore request: " + e.getMessage());
            return MinecoreServerResponse.error(e.getMessage());
        }
    }

    /** Response wrapper for Minecore server requests. */
    @Data
    public static class MinecoreServerResponse {
        private final String status;
        private final String message;
        private final String data;

        /**
         * Constructs a MinecoreServerResponse.
         *
         * @param status The response status
         * @param message The response message
         * @param data The response data
         */
        public MinecoreServerResponse(String status, String message, String data) {
            this.status = status;
            this.message = message;
            this.data = data;
        }

        /**
         * Creates a successful response.
         *
         * @param data The response data
         * @return A successful MinecoreServerResponse
         */
        public static MinecoreServerResponse success(String data) {
            return new MinecoreServerResponse("SUCCESS", "Request created successfully", data);
        }

        /**
         * Creates a validation error response.
         *
         * @param message The error message
         * @return A validation error MinecoreServerResponse
         */
        public static MinecoreServerResponse validationError(String message) {
            return new MinecoreServerResponse("VALIDATION_ERROR", message, null);
        }

        /**
         * Creates an error response.
         *
         * @param message The error message
         * @return An error MinecoreServerResponse
         */
        public static MinecoreServerResponse error(String message) {
            return new MinecoreServerResponse("ERROR", message, null);
        }

        /**
         * Checks if the response indicates success.
         *
         * @return true if the response status is SUCCESS, false otherwise
         */
        public boolean isSuccess() {
            return "SUCCESS".equals(status);
        }
    }
}

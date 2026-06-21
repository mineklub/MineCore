package dk.mineclub.minecore.api.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@SuppressWarnings("unused")
public class StoreCreatedRequest {
    private String service;
    private Mcaccount mcaccount;
    private List<Product> products;
    private Status status;

    @SerializedName("_id")
    private String id;

    private String createdAt;
    private String updatedAt;

    @SerializedName("__v")
    private int version;

    @Getter
    @ToString
    @SuppressWarnings("unused")
    public static class Product {
        @SerializedName("name")
        private String productName;

        @SerializedName("id")
        private String productId;

        private double price;
        private int quantity;
        private String createdAt;
        private String updatedAt;
        private JsonObject metadata;

        @SerializedName("__v")
        private int version;
    }

    @Getter
    @ToString
    @SuppressWarnings("unused")
    public static class Status {
        private String server;
        private String client;
    }

    @Getter
    @ToString
    @SuppressWarnings("unused")
    public static class Mcaccount {
        private String username;
        private String uuid;
    }
}

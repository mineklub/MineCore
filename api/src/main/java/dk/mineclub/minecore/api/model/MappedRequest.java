package dk.mineclub.minecore.api.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@SuppressWarnings("unused")
public class MappedRequest {

    @SerializedName("_id")
    private String id;

    /** UUID of the Minecraft account. */
    private Mcaccount mcaccount;

    private List<Product> products;
    private double price;
    private String service;
    private Status status;
    private String createdAt;
    private String updatedAt;

    @Getter
    @ToString
    @SuppressWarnings("unused")
    public static class Product {
        private String name;
        private String id;
        private double price;
        private int quantity;
        private JsonObject subscription;
        private JsonObject metadata;
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

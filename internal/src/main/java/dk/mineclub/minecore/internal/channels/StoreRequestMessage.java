package dk.mineclub.minecore.internal.channels;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public record StoreRequestMessage(String type, Data data) {
    public record Data(
            @SerializedName("_id") String id,
            Mcaccount mcaccount,
            List<Product> products,
            Service service) {}

    public record Product(
            String productName,
            String productId,
            DecimalValue price,
            Integer quantity,
            Subscription subscription,
            @SerializedName("_id") String id,
            String createdAt,
            String updatedAt,
            @SerializedName("__v") int version) {}

    public record Mcaccount(String uuid, String username, Float balance) {}

    public record DecimalValue(@SerializedName("$numberDecimal") String numberDecimal) {}

    public record Subscription(boolean enabled) {}

    public record Service(
            @SerializedName("_id") String id,
            String name,
            String type,
            String status,
            ServiceInfo info,
            String nicename) {}

    public record ServiceInfo(Owner owner) {}

    public record Owner(
            @SerializedName("_id") String id,
            String username,
            String uuid) {}
}


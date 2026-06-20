package dk.mineclub.minecore.api.model;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import java.lang.reflect.Type;
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

        private DecimalValue price;
        private int quantity;
        private String createdAt;
        private String updatedAt;
        private JsonObject metadata;

        @SerializedName("__v")
        private int version;

        public Double getPriceAsDouble() {
            if (price == null || price.numberDecimal == null) {
                return null;
            }

            return Double.parseDouble(price.numberDecimal);
        }
    }

    @Getter
    @ToString
    @SuppressWarnings("unused")
    @JsonAdapter(DecimalValueAdapter.class)
    public static class DecimalValue {
        @SerializedName("$numberDecimal")
        private String numberDecimal;
    }

    private static final class DecimalValueAdapter
            implements JsonDeserializer<DecimalValue>, JsonSerializer<DecimalValue> {
        @Override
        public DecimalValue deserialize(
                JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json == null || json.isJsonNull()) {
                return null;
            }

            DecimalValue decimalValue = new DecimalValue();
            if (json.isJsonPrimitive()) {
                decimalValue.numberDecimal = json.getAsString();
                return decimalValue;
            }

            JsonObject jsonObject = json.getAsJsonObject();
            if (jsonObject.has("$numberDecimal")) {
                decimalValue.numberDecimal = jsonObject.get("$numberDecimal").getAsString();
            } else if (jsonObject.has("numberDecimal")) {
                decimalValue.numberDecimal = jsonObject.get("numberDecimal").getAsString();
            } else {
                decimalValue.numberDecimal = jsonObject.toString();
            }
            return decimalValue;
        }

        @Override
        public JsonElement serialize(
                DecimalValue src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            if (src != null && src.numberDecimal != null) {
                jsonObject.addProperty("$numberDecimal", src.numberDecimal);
            }
            return jsonObject;
        }
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

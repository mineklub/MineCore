package dk.mineclub.minecore.api.model;

import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StoreProductQuantity {
    private Integer min;
    private Integer max;
    private Integer value;

    public JsonObject toJson() {
        if (min != null && max != null && min > max) {
            throw new IllegalArgumentException("min cannot be greater than max");
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("min", min);
        jsonObject.addProperty("max", max);
        jsonObject.addProperty("value", value);
        return jsonObject;
    }
}

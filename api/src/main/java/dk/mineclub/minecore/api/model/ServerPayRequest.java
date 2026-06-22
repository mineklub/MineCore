package dk.mineclub.minecore.api.model;

import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ServerPayRequest {
    private String mcaccount;
    private double amount;

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        if (mcaccount != null) {
            jsonObject.addProperty("mcaccount", mcaccount);
        }
        jsonObject.addProperty("amount", amount);
        return jsonObject;
    }
}

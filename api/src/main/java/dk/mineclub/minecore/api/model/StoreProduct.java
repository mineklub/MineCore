package dk.mineclub.minecore.api.model;

import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public class StoreProduct {
	private String name;
	private String id;
	private Float price;
	private StoreProductQuantity quantity;
	private Integer subscriptionDays;
	private JsonObject metadata;

	public JsonObject toJson() {
		JsonObject jsonObject = new JsonObject();
		if (name != null) {
			jsonObject.addProperty("name", name);
		}
		if (id != null) {
			jsonObject.addProperty("id", id);
		}
		if (price != null) {
			jsonObject.addProperty("price", price);
		}
		if (quantity != null) {
			jsonObject.add("quantity", quantity.toJson());
		}
		if (subscriptionDays != null) {
			jsonObject.addProperty("subscriptionDays", subscriptionDays);
		}
		if (metadata != null) {
			jsonObject.add("metadata", metadata);
		}
		return jsonObject;
	}
}


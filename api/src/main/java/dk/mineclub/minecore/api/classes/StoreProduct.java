package dk.mineclub.minecore.api.classes;

import com.google.gson.JsonObject;
import lombok.Builder;

@Builder
public class StoreProduct {
	private String name;
	private String id;
	private Float price;
	private StoreProductQuantity quantity;
	private Integer subscriptionDays;
	private JsonObject metadata;

	public JsonObject toJson()
	{
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("name", name);
		jsonObject.addProperty("id", id);
		jsonObject.addProperty("price", price);
		jsonObject.add("quantity", quantity != null ? quantity.toJson() : null);
		if (subscriptionDays != null) {
			jsonObject.addProperty("subscriptionDays", subscriptionDays);
		}
		jsonObject.add("metadata", metadata);
		return jsonObject;
	}
}

package dk.mineclub.minecore.api.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Getter;

import java.util.Arrays;

@Builder @Getter
public class StoreRequest {
	private StoreProduct[] storeProducts;
	private JsonObject metadata;

	public JsonObject toJson() {
		JsonObject jsonObject = new JsonObject();
		StoreProduct[] products = storeProducts == null ? new StoreProduct[0] : storeProducts;
		jsonObject.add(
				"products",
				Arrays.stream(products)
						.map(StoreProduct::toJson)
						.collect(JsonArray::new, JsonArray::add, JsonArray::addAll));
		if (metadata != null) {
			jsonObject.add("metadata", metadata);
		}
		return jsonObject;
	}
}


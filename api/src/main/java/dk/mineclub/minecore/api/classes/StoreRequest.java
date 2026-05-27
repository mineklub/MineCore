package dk.mineclub.minecore.api.classes;

import com.google.gson.JsonObject;
import lombok.Builder;

import java.util.Arrays;

@Builder
public class StoreRequest {
	private StoreProduct[] storeProducts;
	private JsonObject metadata;

	public JsonObject toJson() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("products", Arrays.stream(storeProducts).map(StoreProduct::toJson).collect(com.google.gson.JsonArray::new, com.google.gson.JsonArray::add, com.google.gson.JsonArray::addAll));
		jsonObject.add("metadata", metadata);
		return jsonObject;
	}
}

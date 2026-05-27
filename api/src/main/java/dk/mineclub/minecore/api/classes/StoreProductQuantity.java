package dk.mineclub.minecore.api.classes;

import com.google.gson.JsonObject;
import lombok.Builder;

@Builder
public class StoreProductQuantity {
	private Integer min;
	private Integer max;
	private Integer value;

	public JsonObject toJson()
	{
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("min", min);
		jsonObject.addProperty("max", max);
		jsonObject.addProperty("value", value);
		return jsonObject;
	}
}

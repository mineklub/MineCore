package dk.mineclub.minecore.common.dto;

import com.google.gson.annotations.SerializedName;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for a product in a Minecore request. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MinecoreRequestProductDto {

    @NotNull private String name;

    @NotNull private String id;

    @NotNull
    @Min(1)
    private Integer price;

    @Valid private MinecoreRequestQuantityDto quantity;

    @SerializedName("subscriptionDays")
    private Integer subscriptionDays;

    private Map<String, Object> metadata;
}

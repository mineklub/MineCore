package dk.mineclub.minecore.common.dto;

import dk.mineclub.minecore.common.validator.AtMostOneSubscription;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for creating a Minecore request with multiple products. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMinecoreRequestDto {

    @NotNull private String mcaccount;

    @NotEmpty(message = "At least 1 product is required")
    @Size(max = 10, message = "No more than 10 products are allowed")
    @Valid
    @AtMostOneSubscription
    private List<MinecoreRequestProductDto> products;

    private Map<String, Object> metadata;
}

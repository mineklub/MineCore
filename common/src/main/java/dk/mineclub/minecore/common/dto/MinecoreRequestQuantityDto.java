package dk.mineclub.minecore.common.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for specifying quantity constraints in a Minecore request. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MinecoreRequestQuantityDto {

    @NotNull
    @Min(1)
    private Integer min;

    @NotNull
    @Min(1)
    private Integer max;

    @NotNull
    @Min(1)
    private Integer value;
}

package dk.mineclub.minecore.api.model;

import lombok.Getter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

@Getter
@ToString
@SuppressWarnings("unused")
public class RequestActionResponse {
    private boolean success;
    private String message;

    @Nullable private MappedRequest request;
}

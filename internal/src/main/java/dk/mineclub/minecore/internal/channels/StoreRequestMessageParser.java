package dk.mineclub.minecore.internal.channels;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.Optional;

public final class StoreRequestMessageParser {
    private static final Gson GSON = new Gson();

    public Optional<StoreRequestMessage> parse(String messageJson) {
        try {
            return Optional.ofNullable(GSON.fromJson(messageJson, StoreRequestMessage.class));
        } catch (JsonSyntaxException ignored) {
            return Optional.empty();
        }
    }
}


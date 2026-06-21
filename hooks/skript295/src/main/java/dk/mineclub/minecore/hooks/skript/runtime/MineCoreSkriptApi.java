package dk.mineclub.minecore.hooks.skript.runtime;

import ch.njol.skript.Skript;
import dk.mineclub.minecore.api.MineCoreApi;
import dk.mineclub.minecore.api.manager.RequestManager;
import dk.mineclub.minecore.api.model.MappedVote;
import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import org.jetbrains.annotations.Nullable;

/** Shared runtime helpers for MineCore Skript syntaxes. */
public final class MineCoreSkriptApi {
    private MineCoreSkriptApi() {}

    public static @Nullable RequestManager requestManager() {
        MineCoreApi api = MineCoreApi.getInstance();
        if (api == null) {
            return null;
        }
        return api.getMinecoreRequestManager();
    }

    public static @Nullable StoreCreatedRequest toRequest(@Nullable Object input) {
        if (input instanceof StoreCreatedRequest) {
            return (StoreCreatedRequest) input;
        }

        if (!(input instanceof String)) {
            return null;
        }
        String requestId = ((String) input).trim();
        if (requestId.isEmpty()) {
            return null;
        }

        MineCoreApi api = MineCoreApi.getInstance();
        if (api == null) {
            return null;
        }

        try {
            return api.getGson()
                    .fromJson("{\"_id\":\"" + requestId + "\"}", StoreCreatedRequest.class);
        } catch (Exception ex) {
            Skript.warning(
                    "MineCore Skript: invalid request id '" + requestId + "': " + ex.getMessage());
            return null;
        }
    }

    public static @Nullable MappedVote toVote(@Nullable Object input) {
        if (input instanceof MappedVote) {
            return (MappedVote) input;
        }

        if (!(input instanceof String)) {
            return null;
        }
        String voteId = ((String) input).trim();
        if (voteId.isEmpty()) {
            return null;
        }

        MineCoreApi api = MineCoreApi.getInstance();
        if (api == null) {
            return null;
        }

        try {
            return api.getGson().fromJson("{\"_id\":\"" + voteId + "\"}", MappedVote.class);
        } catch (Exception ex) {
            Skript.warning("MineCore Skript: invalid vote id '" + voteId + "': " + ex.getMessage());
            return null;
        }
    }
}

package dk.mineclub.minecore.api.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@SuppressWarnings("unused")
public class MappedVote {

    @SerializedName("_id")
    private String id;

    private Mcaccount mcaccount;
    private String status;
    private String createdAt;
    private String updatedAt;

    @Getter
    @ToString
    @SuppressWarnings("unused")
    public static class Mcaccount {
        private String username;
        private String uuid;
    }
}

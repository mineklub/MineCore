package dk.mineclub.minecore.api.model;

import com.google.gson.annotations.SerializedName;

public enum VoteStatusQuery {
    @SerializedName("accepted")
    ACCEPTED("accepted"),

    @SerializedName("pending")
    PENDING("pending");

    private final String value;

    VoteStatusQuery(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}

package dk.mineclub.minecore.api.model;

import com.google.gson.annotations.SerializedName;

public enum VoteSortByQuery {
    @SerializedName("createdAt")
    CREATED_AT("createdAt"),

    @SerializedName("updatedAt")
    UPDATED_AT("updatedAt");

    private final String value;

    VoteSortByQuery(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}

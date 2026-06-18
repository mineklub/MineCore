package dk.mineclub.minecore.api.model;

import com.google.gson.annotations.SerializedName;

public enum RequestSortByQuery {
    @SerializedName("createdAt")
    CREATED_AT("createdAt"),

    @SerializedName("updatedAt")
    UPDATED_AT("updatedAt");

    private final String value;

    RequestSortByQuery(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}

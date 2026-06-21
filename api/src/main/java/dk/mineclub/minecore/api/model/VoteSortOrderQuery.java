package dk.mineclub.minecore.api.model;

import com.google.gson.annotations.SerializedName;

public enum VoteSortOrderQuery {
    @SerializedName("asc")
    ASC("asc"),

    @SerializedName("desc")
    DESC("desc");

    private final String value;

    VoteSortOrderQuery(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}

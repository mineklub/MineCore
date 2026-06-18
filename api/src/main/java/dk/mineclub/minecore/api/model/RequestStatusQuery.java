package dk.mineclub.minecore.api.model;

import com.google.gson.annotations.SerializedName;

public enum RequestStatusQuery {
    @SerializedName("accepted")
    ACCEPTED("accepted"),

    @SerializedName("cancelled")
    CANCELLED("cancelled"),

    @SerializedName("pending")
    PENDING("pending");

    private final String value;

    RequestStatusQuery(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}


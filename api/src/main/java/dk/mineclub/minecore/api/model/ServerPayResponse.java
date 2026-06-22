package dk.mineclub.minecore.api.model;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@SuppressWarnings("unused")
public class ServerPayResponse {
    private boolean success;
    private String message;
    private Payment payment;

    @Getter
    @ToString
    @SuppressWarnings("unused")
    public static class Payment {
        private double amount;
        private double serviceBalance;
    }
}

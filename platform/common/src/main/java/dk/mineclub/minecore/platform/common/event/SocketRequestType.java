package dk.mineclub.minecore.platform.common.event;

@SuppressWarnings("unused")
public enum SocketRequestType {
    ACCEPT,
    CANCEL,
    UNKNOWN;

    public static SocketRequestType from(String value) {
        if (value == null) {
            return UNKNOWN;
        }

        if ("accept".equalsIgnoreCase(value)) {
            return ACCEPT;
        }

        if ("cancel".equalsIgnoreCase(value)) {
            return CANCEL;
        }

        return UNKNOWN;
    }
}

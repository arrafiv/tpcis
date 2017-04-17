package util;

public enum Type {
	ENCRYPT ("Encrypt"),
    DECRYPT ("Decrypt");

    private final String type;

    Type(String type) {
        this.type = type;
    }

    public String toString() {
        return this.type;
    }
}

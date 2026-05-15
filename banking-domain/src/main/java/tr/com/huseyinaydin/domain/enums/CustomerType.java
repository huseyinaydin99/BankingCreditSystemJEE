package tr.com.huseyinaydin.domain.enums;

public enum CustomerType {

    INDIVIDUAL(1),
    CORPORATE(2);

    private final int code;

    CustomerType(int code) {
        this.code = code;
    }

    public int getCode() { return code; }

    public static CustomerType fromCode(int code) {
        for (CustomerType type : values()) {
            if (type.code == code) return type;
        }
        throw new IllegalArgumentException("Unknown CustomerType code: " + code);
    }
}

package tr.com.huseyinaydin.domain.enums;

public enum CreditApplicationStatus {

    PENDING(1),
    APPROVED(2),
    REJECTED(3);

    private final int code;

    CreditApplicationStatus(int code) {
        this.code = code;
    }

    public int getCode() { return code; }

    public static CreditApplicationStatus fromCode(int code) {
        for (CreditApplicationStatus status : values()) {
            if (status.code == code) return status;
        }
        throw new IllegalArgumentException("Unknown CreditApplicationStatus code: " + code);
    }
}

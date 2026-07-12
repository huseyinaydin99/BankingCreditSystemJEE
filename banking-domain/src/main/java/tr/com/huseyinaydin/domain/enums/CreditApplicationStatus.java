package tr.com.huseyinaydin.domain.enums;

public enum CreditApplicationStatus {

    // NOT: STATUS_CODE ORDINAL olarak saklanır. Yeni değerler mevcut ordinal'leri
    // (PENDING=0, APPROVED=1, REJECTED=2) bozmamak için SONA eklenmiştir.
    // Parantez içindeki değer ayrı 'code' alanıdır; ordinal ile karıştırılmamalıdır.
    PENDING(1),
    APPROVED(2),
    REJECTED(3),
    UNDER_REVIEW(4),
    CANCELLED(5);

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

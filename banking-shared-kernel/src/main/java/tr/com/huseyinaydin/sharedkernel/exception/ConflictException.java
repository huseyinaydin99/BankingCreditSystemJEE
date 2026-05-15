package tr.com.huseyinaydin.sharedkernel.exception;

public class ConflictException extends ApplicationException {

    private static final String ERROR_CODE = "CONFLICT";

    private final String conflictField;
    private final Object conflictValue;

    public ConflictException(String conflictField, Object conflictValue) {
        super(conflictField + " zaten mevcut: " + conflictValue, ERROR_CODE);
        this.conflictField = conflictField;
        this.conflictValue = conflictValue;
    }

    public ConflictException(String conflictField, Object conflictValue, String message) {
        super(message, ERROR_CODE);
        this.conflictField = conflictField;
        this.conflictValue = conflictValue;
    }

    public ConflictException(String message) {
        super(message, ERROR_CODE);
        this.conflictField = null;
        this.conflictValue = null;
    }

    public String getConflictField() { return conflictField; }
    public Object getConflictValue() { return conflictValue; }
}

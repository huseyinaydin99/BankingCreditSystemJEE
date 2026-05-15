package tr.com.huseyinaydin.sharedkernel.exception;

public class NotFoundException extends ApplicationException {

    private static final String ERROR_CODE = "NOT_FOUND";

    private final String resourceType;
    private final String resourceId;

    public NotFoundException(String resourceType, String resourceId) {
        super(resourceType + " bulunamadı. Id: " + resourceId, ERROR_CODE);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public NotFoundException(String resourceType, String resourceId, String message) {
        super(message, ERROR_CODE);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public NotFoundException(String message) {
        super(message, ERROR_CODE);
        this.resourceType = null;
        this.resourceId = null;
    }

    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
}

package tr.com.huseyinaydin.sharedkernel.security;

public final class GeneralOperationClaims {
    public static final String INDIVIDUAL_CUSTOMERS_CREATE = "individual-customers:create";
    public static final String INDIVIDUAL_CUSTOMERS_UPDATE = "individual-customers:update";
    public static final String INDIVIDUAL_CUSTOMERS_READ = "individual-customers:read";
    public static final String CORPORATE_CUSTOMERS_CREATE = "corporate-customers:create";
    public static final String CREDIT_APPLICATIONS_CREATE = "credit-applications:create";
    public static final String CREDIT_APPLICATIONS_APPROVE = "credit-applications:approve";
    public static final String CREDIT_APPLICATIONS_READ = "credit-applications:read";
    
    private GeneralOperationClaims() {}
}

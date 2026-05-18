package tr.com.huseyinaydin.application.common;

public interface BankingErrorCodes {

    String NATIONAL_ID_CONFLICT    = "NATIONAL_ID_CONFLICT";
    String TAX_NUMBER_CONFLICT     = "TAX_NUMBER_CONFLICT";

    String CUSTOMER_NOT_FOUND      = "CUSTOMER_NOT_FOUND";
    String CUSTOMER_INACTIVE       = "CUSTOMER_INACTIVE";

    String CREDIT_TYPE_NOT_FOUND   = "CREDIT_TYPE_NOT_FOUND";
    String AMOUNT_OUT_OF_RANGE     = "AMOUNT_OUT_OF_RANGE";
    String TERM_OUT_OF_RANGE       = "TERM_OUT_OF_RANGE";
}

package tr.com.huseyinaydin.sharedkernel.exception;


import java.net.URI;


public class BusinessProblemDetail extends ProblemDetail {

    private String errorCode;

    protected BusinessProblemDetail() {
    }

    public BusinessProblemDetail(URI type, String title, int status, String detail,
                                 URI instance, String errorCode) {
        super(type, title, status, detail, instance);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}

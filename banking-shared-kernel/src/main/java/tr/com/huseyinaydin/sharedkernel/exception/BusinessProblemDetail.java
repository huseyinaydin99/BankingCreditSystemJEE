package tr.com.huseyinaydin.sharedkernel.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.net.URI;


@JsonInclude(JsonInclude.Include.NON_NULL)
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

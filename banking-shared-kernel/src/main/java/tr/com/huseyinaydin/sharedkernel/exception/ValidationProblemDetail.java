package tr.com.huseyinaydin.sharedkernel.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.net.URI;
import java.util.List;
import java.util.Map;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationProblemDetail extends ProblemDetail {

    private Map<String, List<String>> errors;

    protected ValidationProblemDetail() {
    }

    public ValidationProblemDetail(URI type, String title, int status, String detail,
                                   URI instance, Map<String, List<String>> errors) {
        super(type, title, status, detail, instance);
        this.errors = errors;
    }

    public Map<String, List<String>> getErrors() { return errors; }
}

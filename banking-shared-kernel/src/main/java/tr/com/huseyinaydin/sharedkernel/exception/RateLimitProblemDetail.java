package tr.com.huseyinaydin.sharedkernel.exception;

import java.net.URI;

public class RateLimitProblemDetail extends ProblemDetail {

    public RateLimitProblemDetail(String detail) {
        super(URI.create("https://bank.com/probs/rate-limit"),
              "Too Many Requests",
              429,
              detail,
              URI.create("/api/v1/creditapplications"));
    }
}

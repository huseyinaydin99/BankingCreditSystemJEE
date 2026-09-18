package tr.com.huseyinaydin.sharedkernel.exception;


import java.net.URI;


public class ProblemDetail {

    private URI type;
    private String title;
    private int status;
    private String detail;
    private URI instance;

    protected ProblemDetail() {
    }

    public ProblemDetail(URI type, String title, int status, String detail, URI instance) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
    }

    public URI getType() { return type; }

    public String getTitle() { return title; }

    public int getStatus() { return status; }

    public String getDetail() { return detail; }

    public URI getInstance() { return instance; }
}

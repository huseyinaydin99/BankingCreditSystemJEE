package tr.com.huseyinaydin.application.pipeline;

public interface ISecuredRequest {
    default String[] getRequiredRoles() {
        return new String[0];
    }
    
    default String[] claims() {
        return new String[0];
    }
}

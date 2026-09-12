package tr.com.huseyinaydin.domain.user;

import tr.com.huseyinaydin.domain.common.Entity;

import java.util.UUID;

public class UserOperationClaim extends Entity<UUID> {
    private UUID userId;
    private UUID operationClaimId;

    protected UserOperationClaim() {
        super();
    }

    public UserOperationClaim(UUID userId, UUID operationClaimId) {
        super();
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.operationClaimId = operationClaimId;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getOperationClaimId() { return operationClaimId; }
    public void setOperationClaimId(UUID operationClaimId) { this.operationClaimId = operationClaimId; }
}

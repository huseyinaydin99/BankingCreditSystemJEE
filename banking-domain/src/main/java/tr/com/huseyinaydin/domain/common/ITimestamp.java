package tr.com.huseyinaydin.domain.common;

import java.time.LocalDateTime;

public interface ITimestamp {
    LocalDateTime createdDate = null;
    LocalDateTime updatedDate = null;
    LocalDateTime deletedDate = null;

    LocalDateTime getCreatedDate();
    LocalDateTime getUpdatedDate();
    LocalDateTime getDeletedDate();
}

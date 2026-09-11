package tr.com.huseyinaydin.domain.common;







import java.time.LocalDateTime;
import java.util.Objects;


public abstract class BaseEntity<TId> implements ITimestamp {

    protected TId id;

    protected LocalDateTime createdDate;

    protected LocalDateTime updatedDate;

    protected LocalDateTime deletedDate;

    protected BaseEntity() {
    }

    protected void onPrePersist() {
        this.createdDate = LocalDateTime.now();
    }

    protected void onPreUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    public TId getId() { return id; }

    @Override
    public LocalDateTime getCreatedDate() { return createdDate; }

    @Override
    public LocalDateTime getUpdatedDate() { return updatedDate; }

    @Override
    public LocalDateTime getDeletedDate() { return deletedDate; }

    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
    public void setDeletedDate(LocalDateTime deletedDate) { this.deletedDate = deletedDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity<?> that = (BaseEntity<?>) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : 0;
    }
}

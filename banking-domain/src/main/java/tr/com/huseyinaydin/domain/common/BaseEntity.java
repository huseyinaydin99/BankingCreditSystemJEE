package tr.com.huseyinaydin.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.time.LocalDateTime;
import java.util.Objects;

@MappedSuperclass
public abstract class BaseEntity<TId> implements ITimestamp {

    @Id
    protected TId id;

    @Column(name = "created_date", nullable = false, updatable = false)
    protected LocalDateTime createdDate;

    @Column(name = "updated_date")
    protected LocalDateTime updatedDate;

    @Column(name = "deleted_date")
    protected LocalDateTime deletedDate;

    protected BaseEntity() {
        this.createdDate = LocalDateTime.now();
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

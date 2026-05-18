package tr.com.huseyinaydin.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;
import java.util.Objects;

@MappedSuperclass
public abstract class BaseEntity<TId> implements ITimestamp {

    @Id
    protected TId id;

    @Column(name = "CREATED_DATE", nullable = false, updatable = false)
    protected LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    protected LocalDateTime updatedDate;

    @Column(name = "DELETED_DATE")
    protected LocalDateTime deletedDate;

    protected BaseEntity() {
    }

    @PrePersist
    protected void onPrePersist() {
        this.createdDate = LocalDateTime.now();
    }

    @PreUpdate
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

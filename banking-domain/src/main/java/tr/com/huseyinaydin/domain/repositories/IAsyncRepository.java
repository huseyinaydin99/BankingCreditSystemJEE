package tr.com.huseyinaydin.domain.repositories;

import tr.com.huseyinaydin.domain.common.BaseEntity;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;
import tr.com.huseyinaydin.sharedkernel.pagination.PaginationRequest;

import java.util.List;
import java.util.Optional;

public interface IAsyncRepository<TEntity extends BaseEntity<TId>, TId> {

    Optional<TEntity> findById(TId id);

    Paginate<TEntity> findAll(Specification<TEntity> spec, PaginationRequest pagination);

    Paginate<TEntity> findAll(Specification<TEntity> spec, PaginationRequest pagination, boolean withDeleted);

    TEntity save(TEntity entity);

    List<TEntity> saveAll(List<TEntity> entities);

    TEntity update(TEntity entity);

    void delete(TEntity entity, boolean permanent);

    boolean existsBy(Specification<TEntity> spec);

    long count(Specification<TEntity> spec);
}

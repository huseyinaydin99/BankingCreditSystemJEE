package tr.com.huseyinaydin.application.stubs;

import tr.com.huseyinaydin.domain.common.BaseEntity;
import tr.com.huseyinaydin.domain.repositories.IAsyncRepository;
import tr.com.huseyinaydin.domain.repositories.Specification;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;
import tr.com.huseyinaydin.sharedkernel.pagination.PaginationRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

public abstract class InMemoryRepositoryBase<TEntity extends BaseEntity<TId>, TId> implements IAsyncRepository<TEntity, TId> {
    protected final Map<TId, TEntity> store = new HashMap<>();

    @Override
    public Optional<TEntity> findById(TId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Paginate<TEntity> findAll(Specification<TEntity> spec, PaginationRequest pagination) {
        return new Paginate<>(new ArrayList<>(), 0, 10, 0);
    }

    @Override
    public Paginate<TEntity> findAll(Specification<TEntity> spec, PaginationRequest pagination, boolean withDeleted) {
        return new Paginate<>(new ArrayList<>(), 0, 10, 0);
    }

    @Override
    public TEntity save(TEntity entity) {
        store.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public List<TEntity> saveAll(List<TEntity> entities) {
        for (TEntity e : entities) save(e);
        return entities;
    }

    @Override
    public TEntity update(TEntity entity) {
        store.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void delete(TEntity entity, boolean permanent) {
        if (permanent) {
            store.remove(entity.getId());
        }
    }

    @Override
    public boolean existsBy(Specification<TEntity> spec) {
        return false;
    }

    @Override
    public long count(Specification<TEntity> spec) {
        return store.size();
    }
}

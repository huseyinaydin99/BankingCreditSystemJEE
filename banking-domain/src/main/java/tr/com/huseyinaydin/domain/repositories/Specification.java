package tr.com.huseyinaydin.domain.repositories;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@FunctionalInterface
public interface Specification<TEntity> {

    Predicate toPredicate(Root<TEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb);

    default Specification<TEntity> and(Specification<TEntity> other) {
        return (root, query, cb) -> cb.and(
                this.toPredicate(root, query, cb),
                other.toPredicate(root, query, cb));
    }

    default Specification<TEntity> or(Specification<TEntity> other) {
        return (root, query, cb) -> cb.or(
                this.toPredicate(root, query, cb),
                other.toPredicate(root, query, cb));
    }

    static <T> Specification<T> not(Specification<T> spec) {
        return (root, query, cb) -> cb.not(spec.toPredicate(root, query, cb));
    }

    static <T> Specification<T> all() {
        return (root, query, cb) -> cb.conjunction();
    }
}

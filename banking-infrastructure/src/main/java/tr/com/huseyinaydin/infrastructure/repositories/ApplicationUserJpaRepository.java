package tr.com.huseyinaydin.infrastructure.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import tr.com.huseyinaydin.domain.repositories.IApplicationUserRepository;
import tr.com.huseyinaydin.domain.user.ApplicationUser;

import java.util.Optional;
import java.util.UUID;

public class ApplicationUserJpaRepository
        extends JpaRepositoryBase<ApplicationUser, UUID>
        implements IApplicationUserRepository {

    public ApplicationUserJpaRepository(EntityManager entityManager) {
        super(entityManager, ApplicationUser.class);
    }

    @Override
    public Optional<ApplicationUser> findByEmail(String email) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ApplicationUser> query = cb.createQuery(ApplicationUser.class);
        Root<ApplicationUser> root = query.from(ApplicationUser.class);
        query.where(cb.and(
                cb.equal(root.get("email"), email),
                root.get("deletedDate").isNull()
        ));
        return entityManager.createQuery(query).getResultStream().findFirst();
    }
}

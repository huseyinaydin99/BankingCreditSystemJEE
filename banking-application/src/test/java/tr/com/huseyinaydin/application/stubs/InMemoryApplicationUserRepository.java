package tr.com.huseyinaydin.application.stubs;

import tr.com.huseyinaydin.domain.user.ApplicationUser;
import tr.com.huseyinaydin.domain.repositories.IApplicationUserRepository;
import java.util.Optional;
import java.util.UUID;

public class InMemoryApplicationUserRepository extends InMemoryRepositoryBase<ApplicationUser, UUID> implements IApplicationUserRepository {

    @Override
    public Optional<ApplicationUser> findByEmail(String email) {
        return store.values().stream()
                .filter(u -> email.equals(u.getEmail()))
                .findFirst();
    }
}

package tr.com.huseyinaydin.domain.repositories;

import tr.com.huseyinaydin.domain.user.ApplicationUser;

import java.util.Optional;
import java.util.UUID;

public interface IApplicationUserRepository {
    Optional<ApplicationUser> findById(UUID id);
    ApplicationUser save(ApplicationUser user);
    void delete(UUID id);
    Optional<ApplicationUser> findByEmail(String email);
    boolean existsByEmail(String email);
}

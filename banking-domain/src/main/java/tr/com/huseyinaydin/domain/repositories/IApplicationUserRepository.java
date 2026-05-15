package tr.com.huseyinaydin.domain.repositories;

import tr.com.huseyinaydin.domain.user.ApplicationUser;

import java.util.Optional;
import java.util.UUID;

public interface IApplicationUserRepository extends IAsyncRepository<ApplicationUser, UUID> {

    Optional<ApplicationUser> findByEmail(String email);
}

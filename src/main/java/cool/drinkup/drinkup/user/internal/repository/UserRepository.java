package cool.drinkup.drinkup.user.internal.repository;

import cool.drinkup.drinkup.user.internal.model.OAuthTypeEnum;
import cool.drinkup.drinkup.user.internal.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmail(String email);

    Optional<User> findByOauthIdAndOauthType(String oauthId, OAuthTypeEnum oauthType);
}

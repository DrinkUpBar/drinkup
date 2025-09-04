package cool.drinkup.drinkup.user.spi;

import java.util.Optional;

public interface AuthenticationServiceFacade {
    Optional<AuthenticatedUserDTO> getCurrentAuthenticatedUser();
}

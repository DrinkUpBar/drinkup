package cool.drinkup.drinkup.user.internal.mapper;

import cool.drinkup.drinkup.user.internal.controller.req.LoginRequest;
import cool.drinkup.drinkup.user.internal.controller.resp.UserProfileResp;
import cool.drinkup.drinkup.user.internal.model.DrinkupUserDetails;
import cool.drinkup.drinkup.user.internal.model.User;
import cool.drinkup.drinkup.user.spi.AuthenticatedUserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "nickname", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "oauthId", ignore = true)
    @Mapping(target = "oauthType", ignore = true)
    @Mapping(target = "oauthBindings", ignore = true)
    User toUser(LoginRequest loginRequest);

    UserProfileResp toUserProfileResp(User user);

    @Mapping(target = "userId", source = "id")
    AuthenticatedUserDTO toAuthenticatedUserDTO(DrinkupUserDetails userDetails);
}

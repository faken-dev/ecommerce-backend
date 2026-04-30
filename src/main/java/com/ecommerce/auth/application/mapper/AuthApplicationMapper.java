package com.ecommerce.auth.application.mapper;

import com.ecommerce.auth.application.dto.UserResponse;
import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", imports = { Role.class, Collectors.class })
public interface AuthApplicationMapper {

    @Mapping(target = "email", expression = "java(user.getEmail().value())")
    @Mapping(target = "phoneNumber",
            expression = "java(user.getPhoneNumber() != null ? user.getPhoneNumber().value() : null)")
    @Mapping(target = "roles",
            expression = "java(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))")
    @Mapping(target = "permissions",
            expression = "java(user.getRoles().stream().flatMap(r -> r.getPermissions().stream()).map(p -> p.getName()).collect(Collectors.toSet()))")
    @Mapping(target = "profilePictureUrl", source = "profilePictureUrl")
    UserResponse toUserResponse(User user);
}

package com.ecommerce.auth.application.mapper;

import com.ecommerce.auth.application.dto.UserResponse;
import com.ecommerce.auth.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthApplicationMapper {

    @Mapping(target = "email", expression = "java(user.getEmail().value())")
    @Mapping(target = "phoneNumber",
            expression = "java(user.getPhoneNumber() != null ? user.getPhoneNumber().value() : null)")
    @Mapping(target = "roles",
            expression = "java(user.getRoles().stream().map(com.ecommerce.auth.domain.entity.Role::getName).collect(java.util.stream.Collectors.toSet()))")
    UserResponse toUserResponse(User user);
}
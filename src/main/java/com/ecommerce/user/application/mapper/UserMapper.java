package com.ecommerce.user.application.mapper;

import com.ecommerce.user.infrastructure.persistence.entity.UserRoleJpaEntity;
import com.ecommerce.user.infrastructure.persistence.entity.UserAccountJpaEntity;
import com.ecommerce.user.application.dto.AddressResponse;
import com.ecommerce.user.application.dto.UserAdminResponse;
import com.ecommerce.user.application.dto.UserDetailAdminResponse;
import com.ecommerce.user.domain.entity.Address;
import com.ecommerce.user.domain.entity.User;
import com.ecommerce.user.domain.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(mapRoles(entity.getRoles()))")
    User toDomain(UserAccountJpaEntity entity);

    UserAdminResponse toAdminResponse(User user);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "phoneNumber", source = "user.phoneNumber")
    @Mapping(target = "active", source = "user.active")
    @Mapping(target = "emailVerified", source = "user.emailVerified")
    @Mapping(target = "roles", source = "user.roles")
    @Mapping(target = "createdAt", source = "user.createdAt")
    @Mapping(target = "profilePictureUrl", source = "profile.profilePictureUrl")
    @Mapping(target = "bio", source = "profile.bio")
    @Mapping(target = "dateOfBirth", source = "profile.dateOfBirth")
    @Mapping(target = "gender", expression = "java(profile.getGender() != null ? profile.getGender().name() : null)")
    @Mapping(target = "addresses", source = "addresses")
    UserDetailAdminResponse toDetailAdminResponse(User user, UserProfile profile, List<Address> addresses);

    AddressResponse toAddressResponse(Address address);

    default Set<String> mapRoles(Set<UserRoleJpaEntity> roles) {
        if (roles == null) return null;
        return roles.stream().map(UserRoleJpaEntity::getName).collect(Collectors.toSet());
    }
}

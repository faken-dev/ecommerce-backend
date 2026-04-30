package com.ecommerce.auth.infrastructure.persistence.mapper;

import com.ecommerce.auth.domain.entity.*;
import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.domain.valueobject.HashedPassword;
import com.ecommerce.auth.domain.valueobject.PhoneNumber;
import com.ecommerce.auth.infrastructure.persistence.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthDomainMapper {

    // - User -----
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "passwordHash", source = "passwordHash")
    User toUser(UserJpaEntity jpa);

    @Mapping(target = "email", source = "email")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "roles", ignore = true)
    UserJpaEntity toJpa(User user);

    // - Role & Permission-
    Role toRole(RoleJpaEntity jpa);

    RoleJpaEntity toJpa(Role role);

    Permission toPermission(PermissionJpaEntity jpa);

    PermissionJpaEntity toJpa(Permission permission);

    // - OtpToken ---
    OtpToken toOtpToken(OtpTokenJpaEntity jpa);

    OtpTokenJpaEntity toJpa(OtpToken token);

    // - RefreshToken -
    RefreshToken toRefreshToken(RefreshTokenJpaEntity jpa);

    RefreshTokenJpaEntity toJpa(RefreshToken token);

    // - Value Object Helpers ---------------
    default Email mapEmail(String email) {
        return email != null ? new Email(email) : null;
    }

    default String mapEmailToString(Email email) {
        return email != null ? email.value() : null;
    }

    default PhoneNumber mapPhoneNumber(String phone) {
        return phone != null ? new PhoneNumber(phone) : null;
    }

    default String mapPhoneNumberToString(PhoneNumber phone) {
        return phone != null ? phone.value() : null;
    }

    default HashedPassword mapPassword(String hash) {
        return hash != null ? HashedPassword.of(hash) : null;
    }

    default String mapPasswordToString(HashedPassword hash) {
        return hash != null ? hash.value() : null;
    }
}

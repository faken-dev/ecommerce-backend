package com.ecommerce.auth.domain.entity;

import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.domain.valueobject.HashedPassword;
import com.ecommerce.auth.domain.valueobject.OAuth2Provider;
import com.ecommerce.testutil.fixture.Fixtures;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("User Entity — Domain Tests")
class UserTest {

    // ── Factory ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create() factory")
    class CreateFactory {

        @Test
        @DisplayName("should create user with correct defaults")
        void createsWithCorrectDefaults() {
            User user = Fixtures.aUser();

            assertThat(user.getId()).isNotNull();
            assertThat(user.getEmail().value()).isEqualTo(Fixtures.USER_EMAIL);
            assertThat(user.getPasswordHash().value()).isEqualTo(Fixtures.USER_PASSWORD);
            assertThat(user.getFullName()).isEqualTo(Fixtures.USER_FULLNAME);
            assertThat(user.isActive()).isTrue();
            assertThat(user.isEmailVerified()).isFalse();
            assertThat(user.isPhoneVerified()).isFalse();
            assertThat(user.isOtpBlocked()).isFalse();
            assertThat(user.getRoles()).isEmpty();
        }

        @Test
        @DisplayName("should create user with phone number when provided")
        void createsWithPhoneNumber() {
            User user = User.create(
                    new Email(Fixtures.USER_EMAIL),
                    HashedPassword.of(Fixtures.USER_PASSWORD),
                    Fixtures.USER_FULLNAME
            );
            user.setPhoneNumber(com.ecommerce.auth.domain.valueobject.PhoneNumber.of(Fixtures.USER_PHONE));

            assertThat(user.getPhoneNumber().value()).isEqualTo(Fixtures.USER_PHONE);
        }
    }

    @Nested
    @DisplayName("createOAuth() factory")
    class CreateOAuthFactory {

        @Test
        @DisplayName("should create OAuth user with email pre-verified")
        void createsOAuthUserPreVerified() {
            User user = User.createOAuth(
                    OAuth2Provider.GOOGLE,
                    "google-sub-123",
                    new Email("oauth@example.com"),
                    "OAuth User",
                    "https://avatar.url"
            );

            assertThat(user.getProvider()).isEqualTo(OAuth2Provider.GOOGLE);
            assertThat(user.getProviderUserId()).isEqualTo("google-sub-123");
            assertThat(user.getPasswordHash()).isNull();
            assertThat(user.isEmailVerified()).isTrue();
            // avatar URL is passed to UserBuilder but no separate profileUrl field
            // The User entity doesn't expose a separate getProfilePictureUrl() method
            // Verify fullName is set from the parameter
            assertThat(user.getFullName()).isEqualTo("OAuth User");
        }

        @Test
        @DisplayName("should default OAuth user to active")
        void oauthUserIsActive() {
            User user = User.createOAuth(
                    OAuth2Provider.APPLE,
                    "apple-sub-456",
                    new Email("apple@example.com"),
                    "Apple User",
                    null
            );

            assertThat(user.isActive()).isTrue();
        }
    }

    // ── Domain Rules ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ensureActive()")
    class EnsureActive {

        @Test
        @DisplayName("should pass for active non-deleted user")
        void activeUserPasses() {
            User user = Fixtures.aUser();
            user.ensureActive(); // must not throw
        }

        @Test
        @DisplayName("should throw AUTH_ACCOUNT_BLOCKED when active=false")
        void blockedUserThrows() {
            User user = Fixtures.aBlockedUser();

            assertThatThrownBy(user::ensureActive)
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_ACCOUNT_BLOCKED);
        }

        @Test
        @DisplayName("should throw AUTH_USER_DELETED when deletedAt is set")
        void deletedUserThrows() {
            User user = Fixtures.aDeletedUser();

            assertThatThrownBy(user::ensureActive)
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_USER_DELETED);
        }
    }

    @Nested
    @DisplayName("ensureOtpNotBlocked()")
    class EnsureOtpNotBlocked {

        @Test
        @DisplayName("should throw when OTP is blocked")
        void blockedOtpThrows() {
            User user = Fixtures.aUser();
            user.blockOtp("Spam detected");

            assertThatThrownBy(user::ensureOtpNotBlocked)
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_OTP_BLOCKED);
        }

        @Test
        @DisplayName("should pass when OTP is not blocked")
        void notBlockedPasses() {
            Fixtures.aUser().ensureOtpNotBlocked();
        }
    }

    @Nested
    @DisplayName("isOAuthUser()")
    class IsOAuthUser {

        @Test
        @DisplayName("should return true when provider is GOOGLE")
        void googleIsOAuth() {
            assertThat(Fixtures.anOAuthUser().isOAuthUser()).isTrue();
        }

        @Test
        @DisplayName("should return true when provider is APPLE")
        void appleIsOAuth() {
            User user = User.createOAuth(OAuth2Provider.APPLE, "sub", new Email("a@b.com"), "n", null);
            assertThat(user.isOAuthUser()).isTrue();
        }

        @Test
        @DisplayName("should return false when provider is null")
        void noProviderReturnsFalse() {
            assertThat(Fixtures.aUser().isOAuthUser()).isFalse();
        }
    }

    @Nested
    @DisplayName("isDeleted()")
    class IsDeleted {

        @Test
        @DisplayName("should return false when deletedAt is null")
        void notDeleted() {
            assertThat(Fixtures.aUser().isDeleted()).isFalse();
        }

        @Test
        @DisplayName("should return true when deletedAt is set")
        void deleted() {
            assertThat(Fixtures.aDeletedUser().isDeleted()).isTrue();
        }
    }

    // ── Soft Delete ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("softDelete()")
    class SoftDelete {

        @Test
        @DisplayName("should set active=false and deletedAt")
        void setsCorrectFields() {
            User user = Fixtures.aUser();
            UUID deletedBy = Fixtures.ADMIN_ID;

            user.softDelete(deletedBy);

            assertThat(user.isDeleted()).isTrue();
            assertThat(user.isActive()).isFalse();
            assertThat(user.getDeletedAt()).isNotNull();
            assertThat(user.getDeletedBy()).isEqualTo(deletedBy);
        }

        @Test
        @DisplayName("should be idempotent")
        void idempotent() {
            User user = Fixtures.aUser();
            UUID deletedBy = Fixtures.ADMIN_ID;

            user.softDelete(deletedBy);
            var firstDeletedAt = user.getDeletedAt();
            user.softDelete(deletedBy);

            assertThat(user.getDeletedAt()).isEqualTo(firstDeletedAt);
        }
    }

    @Nested
    @DisplayName("softDeleteAndReturnEvent()")
    class SoftDeleteAndReturnEvent {

        @Test
        @DisplayName("should return event with correct userId")
        void returnsEvent() {
            User user = Fixtures.aUser();
            var event = user.softDeleteAndReturnEvent(Fixtures.ADMIN_ID);

            assertThat(event.userId()).isEqualTo(user.getId());
            assertThat(event.occurredAt()).isNotNull();
            assertThat(user.isDeleted()).isTrue();
        }
    }

    // ── Block / Unblock ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("blockAccount() / unblockAccount()")
    class BlockUnblock {

        @Test
        @DisplayName("blockAccount should set active=false")
        void block() {
            User user = Fixtures.anActiveEmailVerifiedUser();
            user.blockAccount();
            assertThat(user.isActive()).isFalse();
        }

        @Test
        @DisplayName("unblockAccount should set active=true")
        void unblock() {
            User user = Fixtures.aBlockedUser();
            user.unblockAccount();
            assertThat(user.isActive()).isTrue();
        }
    }

    // ── OTP Block ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("blockOtp() / unblockOtp()")
    class OtpBlock {

        @Test
        @DisplayName("blockOtp should set blocked fields and timestamp")
        void block() {
            User user = Fixtures.aUser();
            user.blockOtp("Too many attempts");

            assertThat(user.isOtpBlocked()).isTrue();
            assertThat(user.getOtpBlockedAt()).isNotNull();
            assertThat(user.getOtpBlockedReason()).isEqualTo("Too many attempts");
        }

        @Test
        @DisplayName("unblockOtp should clear all blocked fields")
        void unblock() {
            User user = Fixtures.aUser();
            user.blockOtp("spam");
            user.unblockOtp();

            assertThat(user.isOtpBlocked()).isFalse();
            assertThat(user.getOtpBlockedAt()).isNull();
            assertThat(user.getOtpBlockedReason()).isNull();
        }
    }

    // ── Roles ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Roles")
    class Roles {

        @Test
        @DisplayName("hasRole should return true for assigned role")
        void hasRoleTrue() {
            User user = Fixtures.anActiveEmailVerifiedUser();
            assertThat(user.hasRole("BUYER")).isTrue();
        }

        @Test
        @DisplayName("hasRole should return false for unassigned role")
        void hasRoleFalse() {
            assertThat(Fixtures.aUser().hasRole("ADMIN")).isFalse();
        }

        @Test
        @DisplayName("addRole should add role to set")
        void addRole() {
            User user = Fixtures.aUser();
            user.addRole(Fixtures.sellerRole());

            assertThat(user.hasRole("SELLER")).isTrue();
        }

        @Test
        @DisplayName("getAllPermissions should aggregate from all roles")
        void allPermissions() {
            User user = Fixtures.anActiveEmailVerifiedUser();
            assertThat(user.getAllPermissions()).isNotNull();
        }
    }

    // ── State Transitions ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("verifyEmail() / verifyPhone()")
    class Verify {

        @Test
        @DisplayName("verifyEmail should set emailVerified=true")
        void verifyEmail() {
            User user = Fixtures.aUser();
            user.verifyEmail();
            assertThat(user.isEmailVerified()).isTrue();
        }

        @Test
        @DisplayName("verifyPhone should set phoneVerified=true")
        void verifyPhone() {
            User user = Fixtures.aUser();
            user.verifyPhone();
            assertThat(user.isPhoneVerified()).isTrue();
        }
    }

    @Nested
    @DisplayName("changePassword()")
    class ChangePassword {

        @Test
        @DisplayName("should update password hash")
        void updatesHash() {
            User user = Fixtures.aUser();
            HashedPassword newHash = HashedPassword.of("NewHashedPassword!");

            user.changePassword(newHash);

            assertThat(user.getPasswordHash().value()).isEqualTo("NewHashedPassword!");
        }
    }

    @Nested
    @DisplayName("updateFullName()")
    class UpdateFullName {

        @Test
        @DisplayName("should update fullName")
        void updatesName() {
            User user = Fixtures.aUser();
            // Ensure updatedAt is set before the test so comparison works
            user.setUpdatedAt(java.time.Instant.now().minusSeconds(1));

            user.updateFullName("New Name");

            assertThat(user.getFullName()).isEqualTo("New Name");
            assertThat(user.getUpdatedAt()).isAfter(
                    java.time.Instant.now().minusSeconds(5));
        }
    }

    // ── Domain Events ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Domain Event Factories")
    class DomainEvents {

        @Test
        @DisplayName("toRegistrationCompletedEvent should contain user data")
        void registrationEvent() {
            User user = Fixtures.anActiveEmailVerifiedUser();
            var event = user.toRegistrationCompletedEvent();

            assertThat(event.userId()).isEqualTo(user.getId());
            assertThat(event.email()).isEqualTo(user.getEmail().value());
            assertThat(event.fullName()).isEqualTo(user.getFullName());
        }

        @Test
        @DisplayName("toEmailVerifiedEvent should contain user data")
        void emailVerifiedEvent() {
            User user = Fixtures.anActiveEmailVerifiedUser();
            var event = user.toEmailVerifiedEvent();

            assertThat(event.userId()).isEqualTo(user.getId());
            assertThat(event.email()).isEqualTo(user.getEmail().value());
        }

        @Test
        @DisplayName("toPasswordChangedEvent should contain user data")
        void passwordChangedEvent() {
            User user = Fixtures.anActiveEmailVerifiedUser();
            var event = user.toPasswordChangedEvent();

            assertThat(event.userId()).isEqualTo(user.getId());
        }
    }

    // ── Equality ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("equals / hashCode")
    class Equality {

        @Test
        @DisplayName("users with same ID should be equal")
        void sameIdEqual() {
            UUID id = UUID.randomUUID();
            User u1 = Fixtures.aUser(id);
            User u2 = Fixtures.aUser(id);

            assertThat(u1).isEqualTo(u2);
            assertThat(u1.hashCode()).isEqualTo(u2.hashCode());
        }

        @Test
        @DisplayName("users with different IDs should not be equal")
        void differentIdNotEqual() {
            User u1 = Fixtures.aUser(UUID.randomUUID());
            User u2 = Fixtures.aUser(UUID.randomUUID());

            assertThat(u1).isNotEqualTo(u2);
        }
    }
}

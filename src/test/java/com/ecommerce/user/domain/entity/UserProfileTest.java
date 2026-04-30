package com.ecommerce.user.domain.entity;

import com.ecommerce.testutil.fixture.Fixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserProfile Entity — Domain Tests")
class UserProfileTest {

    // ── Factory ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create profile with all fields")
        void createsCorrectly() {
            UserProfile profile = Fixtures.aCompleteUserProfile();

            assertThat(profile.getUserId()).isEqualTo(Fixtures.USER_ID);
            assertThat(profile.getFullName()).isEqualTo("Full Name");
            assertThat(profile.getProfilePictureUrl()).isEqualTo("https://cdn.example.com/avatar.jpg");
            assertThat(profile.getBio()).isEqualTo("Bio text");
            assertThat(profile.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 15));
            assertThat(profile.getGender()).isEqualTo(UserProfile.Gender.MALE);
            assertThat(profile.getDefaultAddressId()).isEqualTo(Fixtures.ADDRESS_ID);
        }
    }

    @Nested
    @DisplayName("createFromRegistration()")
    class CreateFromRegistration {

        @Test
        @DisplayName("should create minimal profile with just userId and fullName")
        void createsMinimal() {
            UserProfile profile = Fixtures.aUserProfile();

            assertThat(profile.getUserId()).isEqualTo(Fixtures.USER_ID);
            assertThat(profile.getFullName()).isEqualTo(Fixtures.USER_FULLNAME);
            assertThat(profile.getBio()).isNull();
            assertThat(profile.getDateOfBirth()).isNull();
        }
    }

    // ── Domain Methods ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateProfile()")
    class UpdateProfile {

        @Test
        @DisplayName("should update all fields")
        void updates() {
            UserProfile profile = Fixtures.aUserProfile();

            profile.updateProfile(
                    "New Name",
                    "https://cdn.example.com/new.jpg",
                    "New bio",
                    LocalDate.of(1995, 5, 20),
                    UserProfile.Gender.FEMALE,
                    Fixtures.ADDRESS_ID
            );

            assertThat(profile.getFullName()).isEqualTo("New Name");
            assertThat(profile.getProfilePictureUrl()).isEqualTo("https://cdn.example.com/new.jpg");
            assertThat(profile.getBio()).isEqualTo("New bio");
            assertThat(profile.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 5, 20));
            assertThat(profile.getGender()).isEqualTo(UserProfile.Gender.FEMALE);
            assertThat(profile.getDefaultAddressId()).isEqualTo(Fixtures.ADDRESS_ID);
        }
    }

    @Nested
    @DisplayName("calculateAge() / isAdult()")
    class AgeCalculation {

        @Test
        @DisplayName("calculateAge should return correct age")
        void calculatesAge() {
            UserProfile profile = Fixtures.aCompleteUserProfile();
            int age = profile.calculateAge();
            int expected = LocalDate.now().getYear() - 1990;
            assertThat(age).isEqualTo(expected);
        }

        @Test
        @DisplayName("isAdult returns true when age >= 18")
        void isAdult() {
            UserProfile profile = Fixtures.aCompleteUserProfile();
            assertThat(profile.isAdult()).isTrue();
        }
    }

    @Nested
    @DisplayName("hasProfilePicture()")
    class HasProfilePicture {

        @Test
        @DisplayName("returns true when profilePictureUrl is set")
        void hasPicture() {
            assertThat(Fixtures.aCompleteUserProfile().hasProfilePicture()).isTrue();
        }

        @Test
        @DisplayName("returns false when profilePictureUrl is null")
        void noPicture() {
            assertThat(Fixtures.aUserProfile().hasProfilePicture()).isFalse();
        }

        @Test
        @DisplayName("returns false when profilePictureUrl is blank")
        void blankPicture() {
            UserProfile profile = Fixtures.aCompleteUserProfile();
            profile.updateProfile("n", "", null, null, null, null);
            assertThat(profile.hasProfilePicture()).isFalse();
        }
    }

    @Nested
    @DisplayName("isProfileCompleted()")
    class ProfileCompletion {

        @Test
        @DisplayName("returns true when all required fields are set")
        void complete() {
            assertThat(Fixtures.aCompleteUserProfile().isProfileCompleted()).isTrue();
        }

        @Test
        @DisplayName("returns false when any required field is missing")
        void incomplete() {
            UserProfile profile = Fixtures.aCompleteUserProfile();
            profile.updateProfile("n", null, null, null, null, null);
            assertThat(profile.isProfileCompleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("updateAvatar()")
    class UpdateAvatar {

        @Test
        @DisplayName("should update profile picture URL")
        void updatesAvatar() {
            UserProfile profile = Fixtures.aUserProfile();
            profile.updateAvatar("https://cdn.example.com/new-avatar.png");
            assertThat(profile.getProfilePictureUrl()).isEqualTo("https://cdn.example.com/new-avatar.png");
        }
    }

    // ── Domain Events ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("toCreatedEvent()")
    class DomainEvents {

        @Test
        @DisplayName("should contain profile id and user id")
        void createdEvent() {
            UserProfile profile = Fixtures.aUserProfile();
            var event = profile.toCreatedEvent();
            assertThat(event.profileId()).isEqualTo(profile.getId());
            assertThat(event.userId()).isEqualTo(Fixtures.USER_ID);
        }
    }
}
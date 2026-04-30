package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.command.RegisterCommand;
import com.ecommerce.auth.application.dto.RegisterResponse;
import com.ecommerce.auth.application.dto.UserResponse;
import com.ecommerce.auth.application.mapper.AuthApplicationMapper;
import com.ecommerce.auth.application.port.PasswordEncoder;
import com.ecommerce.auth.domain.entity.Role;
import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.repository.RoleRepository;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.domain.valueobject.HashedPassword;
import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterUseCase")
class RegisterUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EventPublisher eventPublisher;
    @Mock private AuthApplicationMapper mapper;

    private RegisterUseCase sut;

    @BeforeEach
    void setUp() {
        sut = new RegisterUseCase(userRepository, roleRepository, passwordEncoder,
                eventPublisher, mapper);
    }

    private RegisterCommand aCommand(String email, String phone) {
        return new RegisterCommand(email, "StrongPassword123!", "Test User", phone);
    }

    private User savedUser(String email) {
        return User.create(new Email(email), HashedPassword.of("h"), "Test User");
    }

    // ── Success Path ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("execute() — success")
    class Success {

        @Test
        @DisplayName("should encode password and save user")
        void encodesPasswordAndSaves() {
            Role buyerRole = Role.create("BUYER", "Standard buyer");
            when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
            when(roleRepository.findByName("BUYER")).thenReturn(Optional.of(buyerRole));
            User saved = savedUser("new@example.com");
            when(userRepository.save(any(User.class))).thenReturn(saved);
            UserResponse userResp = new UserResponse(UUID.randomUUID(), "new@example.com",
                    "Test User", null, false, false, Set.of("BUYER"), Set.of(), null);
            when(mapper.toUserResponse(any())).thenReturn(userResp);

            sut.execute(aCommand("new@example.com", null));

            verify(passwordEncoder).encode("StrongPassword123!");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("should assign BUYER role by default")
        void assignsBuyerRole() {
            Role buyerRole = Role.create("BUYER", "Standard buyer");
            when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
            when(roleRepository.findByName("BUYER")).thenReturn(Optional.of(buyerRole));
            User saved = savedUser("new@example.com");
            when(userRepository.save(any(User.class))).thenReturn(saved);
            UserResponse userResp = new UserResponse(UUID.randomUUID(), "new@example.com",
                    "Test User", null, false, false, Set.of("BUYER"), Set.of(), null);
            when(mapper.toUserResponse(any())).thenReturn(userResp);

            sut.execute(aCommand("new@example.com", null));

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().hasRole("BUYER")).isTrue();
        }

        @Test
        @DisplayName("should publish RegistrationCompletedEvent")
        void publishesEvent() {
            Role buyerRole = Role.create("BUYER", "Standard buyer");
            when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
            when(roleRepository.findByName("BUYER")).thenReturn(Optional.of(buyerRole));
            User saved = savedUser("new@example.com");
            when(userRepository.save(any(User.class))).thenReturn(saved);
            UserResponse userResp = new UserResponse(UUID.randomUUID(), "new@example.com",
                    "Test User", null, false, false, Set.of("BUYER"), Set.of(), null);
            when(mapper.toUserResponse(any())).thenReturn(userResp);

            sut.execute(aCommand("new@example.com", null));

            verify(eventPublisher).publish(any(
                    com.ecommerce.auth.domain.event.RegistrationCompletedEvent.class));
        }

        @Test
        @DisplayName("should return RegisterResponse")
        void returnsResponse() {
            Role buyerRole = Role.create("BUYER", "Standard buyer");
            when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
            when(roleRepository.findByName("BUYER")).thenReturn(Optional.of(buyerRole));
            User saved = savedUser("new@example.com");
            when(userRepository.save(any(User.class))).thenReturn(saved);
            UserResponse userResp = new UserResponse(UUID.randomUUID(), "new@example.com",
                    "Test User", null, false, false, Set.of("BUYER"), Set.of(), null);
            when(mapper.toUserResponse(any())).thenReturn(userResp);

            RegisterResponse response = sut.execute(aCommand("new@example.com", null));

            assertThat(response).isNotNull();
            assertThat(response.user()).isNotNull();
            assertThat(response.message()).contains("Registration successful");
        }
    }

    // ── Error Paths ─────────────────────────────────────────────────────────---

    @Nested
    @DisplayName("execute() — error cases")
    class ErrorCases {

        @Test
        @DisplayName("should throw AUTH_EMAIL_ALREADY_EXISTS when email is taken")
        void emailExists() {
            when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

            assertThatThrownBy(() -> sut.execute(aCommand("existing@example.com", null)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }

        @Test
        @DisplayName("should throw AUTH_PHONE_ALREADY_EXISTS when phone is taken")
        void phoneExists() {
            when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
            when(userRepository.existsByPhoneNumber(any())).thenReturn(true);

            assertThatThrownBy(() -> sut.execute(aCommand("new@example.com", "+84909123456")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_PHONE_ALREADY_EXISTS);
        }

        @Test
        @DisplayName("should throw VALIDATION_FAILED when BUYER role not found")
        void buyerRoleMissing() {
            when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
            lenient().when(roleRepository.findByName("BUYER")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.execute(aCommand("new@example.com", null)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.VALIDATION_FAILED);
        }
    }
}
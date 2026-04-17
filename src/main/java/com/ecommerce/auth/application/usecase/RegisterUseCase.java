package com.ecommerce.auth.application.usecase;

import com.ecommerce.auth.application.command.RegisterCommand;
import com.ecommerce.auth.application.dto.RegisterResponse;
import com.ecommerce.auth.application.mapper.AuthApplicationMapper;
import com.ecommerce.auth.application.port.PasswordEncoder;
import com.ecommerce.auth.domain.entity.Role;
import com.ecommerce.auth.domain.entity.User;
import com.ecommerce.auth.domain.repository.RoleRepository;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.auth.domain.valueobject.Email;
import com.ecommerce.auth.domain.valueobject.HashedPassword;
import com.ecommerce.auth.domain.valueobject.PhoneNumber;
import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;
    private final AuthApplicationMapper mapper;

    @Transactional
    public RegisterResponse execute(RegisterCommand command) {
        Email email = new Email(command.email());

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }
        if (command.phoneNumber() != null
                && userRepository.existsByPhoneNumber(command.phoneNumber())) {
            throw new BusinessException(ErrorCode.AUTH_PHONE_ALREADY_EXISTS);
        }

        HashedPassword hashedPassword = HashedPassword.of(passwordEncoder.encode(command.password()));

        Role buyerRole = roleRepository.findByName("BUYER")
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));

        User user = User.create(email, hashedPassword, command.fullName())
                .phoneNumber(command.phoneNumber() != null ? PhoneNumber.of(command.phoneNumber()) : null)
                .build();
        user.addRole(buyerRole);

        User saved = userRepository.save(user);

        eventPublisher.publish(saved.toRegistrationCompletedEvent());

        return new RegisterResponse(
                mapper.toUserResponse(saved),
                "Registration successful. A verification code has been sent to your email.");
    }
}

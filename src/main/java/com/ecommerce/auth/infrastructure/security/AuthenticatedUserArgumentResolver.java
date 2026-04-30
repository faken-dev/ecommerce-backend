package com.ecommerce.auth.infrastructure.security;

import com.ecommerce.shared.infrastructure.security.AuthenticatedUser;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.UUID;

/**
 * Resolver to allow injecting UUID directly from AuthenticatedUser principal.
 * This supports @AuthenticationPrincipal UUID userId in controllers.
 */
@Component
public class AuthenticatedUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class) 
            && (parameter.getParameterType().equals(UUID.class) || parameter.getParameterType().equals(AuthenticatedUser.class));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        
        if (parameter.getParameterType().equals(AuthenticatedUser.class)) {
            return principal instanceof AuthenticatedUser ? principal : null;
        }
        
        if (parameter.getParameterType().equals(UUID.class)) {
            if (principal instanceof AuthenticatedUser) {
                return ((AuthenticatedUser) principal).getId();
            }
            if (principal instanceof UUID) {
                return principal;
            }
            if (principal instanceof String) {
                try {
                    return UUID.fromString((String) principal);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        
        return null;
    }
}

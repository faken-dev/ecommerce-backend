package com.ecommerce.shared.domain.exception;

public class EntityNotFoundException extends DomainException {
    public EntityNotFoundException(String message) {
        super(message, 404);
    }
    
    public EntityNotFoundException(String entity, Object id) {
        super(String.format("%s with id %s not found", entity, id), 404);
    }
}

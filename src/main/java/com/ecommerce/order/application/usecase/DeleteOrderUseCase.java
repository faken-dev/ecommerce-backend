package com.ecommerce.order.application.usecase;

import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteOrderUseCase {

    private final OrderRepository orderRepository;

    @Transactional
    public void execute(UUID orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        orderRepository.deleteById(orderId);
    }
}

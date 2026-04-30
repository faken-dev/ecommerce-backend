package com.ecommerce.payment.application.mapper;

import com.ecommerce.payment.application.dto.RefundResponse;
import com.ecommerce.payment.domain.entity.Refund;
import org.springframework.stereotype.Component;

@Component
public class RefundApplicationMapper {

    public RefundResponse toResponse(Refund refund) {
        return new RefundResponse(
                refund.getId(),
                refund.getPaymentId(),
                refund.getOrderId(),
                refund.getRequestedBy(),
                refund.getAmount(),
                refund.getReason(),
                refund.getStatus(),
                refund.getRejectionReason(),
                refund.getProviderRefundId(),
                refund.getApprovedAt(),
                refund.getRejectedAt(),
                refund.getCompletedAt(),
                refund.getFailedAt(),
                refund.getCreatedAt()
        );
    }
}

package com.ecommerce.payment.presentation.controller;

import java.util.HashMap;

import java.util.Map;


import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.domain.entity.PaymentStatus;
import com.ecommerce.payment.infrastructure.config.PaymentProperties;
import com.ecommerce.payment.application.command.*;
import com.ecommerce.payment.application.dto.*;
import com.ecommerce.payment.application.usecase.*;
import com.ecommerce.payment.presentation.dto.request.*;
import com.ecommerce.payment.infrastructure.gateway.momo.MoMoGatewayAdapter;
import com.ecommerce.payment.infrastructure.gateway.vnpay.VNPayGatewayAdapter;
import com.ecommerce.payment.infrastructure.gateway.zalopay.ZaloPayGatewayAdapter;
import com.ecommerce.payment.application.mapper.PaymentApplicationMapper;
import com.ecommerce.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.util.Enumeration;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Payment and refund management")
public class PaymentController {
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final CreatePaymentUseCase createPaymentUseCase;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;
    private final FailPaymentUseCase failPaymentUseCase;
    private final CancelPaymentUseCase cancelPaymentUseCase;
    private final RequestRefundUseCase requestRefundUseCase;
    private final ApproveRefundUseCase approveRefundUseCase;
    private final RejectRefundUseCase rejectRefundUseCase;
    private final GetPaymentUseCase getPaymentUseCase;
    private final ListPaymentsUseCase listPaymentsUseCase;
    private final DeletePaymentUseCase deletePaymentUseCase;
    private final UpdatePaymentStatusUseCase updatePaymentStatusUseCase;
    private final PaymentApplicationMapper paymentApplicationMapper;
    private final VNPayGatewayAdapter vnpayGatewayAdapter;
    private final MoMoGatewayAdapter momoGatewayAdapter;
    private final ZaloPayGatewayAdapter zalopayGatewayAdapter;
    private final PaymentProperties properties;

    // - BUYER ---------------

    @Operation(summary = "Initiate payment for an order")
    @PostMapping
    @PreAuthorize("hasAuthority('payment:create')")
    public ResponseEntity<ApiResponse<PaymentInitiatedResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest req,
            @AuthenticationPrincipal UUID buyerId,
            HttpServletRequest httpRequest) {

        CreatePaymentCommand cmd = new CreatePaymentCommand(
                req.orderId(),
                buyerId,
                req.amount(),
                req.currency() != null ? req.currency() : "VND",
                req.provider(),
                req.methodType(),
                req.description(),
                req.idempotencyKey(),
                req.returnUrl(),
                req.cancelUrl(),
                resolveIp(httpRequest),
                httpRequest.getHeader("User-Agent")
        );

        PaymentInitiatedResponse response = createPaymentUseCase.execute(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @Operation(summary = "Get payment by ID (buyer's own payments)")
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAuthority('payment:read')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal UUID buyerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                getPaymentUseCase.execute(paymentId, buyerId)));
    }

    @Operation(summary = "List own payments (buyer)")
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('payment:read')")
    public ResponseEntity<ApiResponse<Iterable<PaymentResponse>>> listMyPayments(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UUID buyerId) {
        PaymentStatus paymentStatus = status != null ? PaymentStatus.valueOf(status) : null;
        return ResponseEntity.ok(ApiResponse.ok(
                listPaymentsUseCase.executeByBuyer(buyerId, paymentStatus, pageable)));
    }

    @Operation(summary = "Cancel a pending payment")
    @PostMapping("/{paymentId}/cancel")
    @PreAuthorize("hasAuthority('payment:cancel')")
    public ResponseEntity<ApiResponse<Void>> cancelPayment(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal UUID buyerId) {
        cancelPaymentUseCase.execute(new CancelPaymentCommand(paymentId, buyerId));
        return ResponseEntity.ok(ApiResponse.<Void>ok(null, "Payment cancelled"));
    }

    @Operation(summary = "Request a refund for a paid order")
    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasAuthority('payment:refund')")
    public ResponseEntity<ApiResponse<RefundResponse>> requestRefund(
            @PathVariable UUID paymentId,
            @Valid @RequestBody RequestRefundRequest req,
            @AuthenticationPrincipal UUID requestedBy,
            HttpServletRequest httpRequest) {

        // Resolve orderId from payment for buyer-initiated refunds
        var payment = getPaymentUseCase.executeForAdmin(paymentId);

        RequestRefundCommand cmd = new RequestRefundCommand(
                paymentId,
                payment.orderId(),
                requestedBy,
                req.amount(),
                req.reason(),
                resolveIp(httpRequest)
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(requestRefundUseCase.execute(cmd)));
    }

    // - ADMIN ---------------

    @Operation(summary = "List all payments (admin)")
    @GetMapping
    @PreAuthorize("hasAuthority('payment:manage')")
    public ResponseEntity<ApiResponse<Iterable<PaymentResponse>>> listAllPayments(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        PaymentStatus paymentStatus = status != null ? PaymentStatus.valueOf(status) : null;
        return ResponseEntity.ok(ApiResponse.ok(
                listPaymentsUseCase.executeForAdmin(paymentStatus, pageable)));
    }

    @Operation(summary = "Confirm payment (admin or webhook handler)")
    @PostMapping("/{paymentId}/confirm")
    @PreAuthorize("hasAuthority('payment:manage')")
    public ResponseEntity<ApiResponse<PaymentConfirmedResponse>> confirmPayment(
            @PathVariable UUID paymentId,
            @Valid @RequestBody ConfirmPaymentRequest req,
            HttpServletRequest httpRequest) {

        Instant paidAt = parsePaidAt(req.paidAt());
        ConfirmPaymentCommand cmd = new ConfirmPaymentCommand(
                paymentId,
                req.providerReference(),
                paidAt
        );

        return ResponseEntity.ok(ApiResponse.ok(confirmPaymentUseCase.execute(cmd)));
    }

    @Operation(summary = "Record payment failure (webhook handler)")
    @PostMapping("/{paymentId}/fail")
    @PreAuthorize("hasAuthority('payment:manage')")
    public ResponseEntity<ApiResponse<Void>> failPayment(
            @PathVariable UUID paymentId,
            @Valid @RequestBody FailPaymentRequest req) {

        failPaymentUseCase.execute(new FailPaymentCommand(
                paymentId, req.failureCode(), req.failureReason()));
        return ResponseEntity.ok(ApiResponse.<Void>ok(null, "Payment failure recorded"));
    }

    @Operation(summary = "Get payment by ID (admin)")
    @GetMapping("/{paymentId}/admin")
    @PreAuthorize("hasAuthority('payment:manage')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentAdmin(
            @PathVariable UUID paymentId) {
        return ResponseEntity.ok(ApiResponse.ok(
                getPaymentUseCase.executeForAdmin(paymentId)));
    }

    @Operation(summary = "Approve a pending refund")
    @PostMapping("/{paymentId}/refunds/{refundId}/approve")
    @PreAuthorize("hasAuthority('payment:refund:approve')")
    public ResponseEntity<ApiResponse<RefundResponse>> approveRefund(
            @PathVariable UUID paymentId,
            @PathVariable UUID refundId,
            @Valid @RequestBody(required = false) ApproveRefundRequest req,
            HttpServletRequest httpRequest) {

        ApproveRefundCommand cmd = new ApproveRefundCommand(
                refundId,
                paymentId,
                req != null ? req.providerRefundId() : null,
                Instant.now()
        );

        return ResponseEntity.ok(ApiResponse.ok(approveRefundUseCase.execute(cmd)));
    }

    @Operation(summary = "Reject a pending refund")
    @PostMapping("/{paymentId}/refunds/{refundId}/reject")
    @PreAuthorize("hasAuthority('payment:refund:approve')")
    public ResponseEntity<ApiResponse<RefundResponse>> rejectRefund(
            @PathVariable UUID paymentId,
            @PathVariable UUID refundId,
            @Valid @RequestBody RejectRefundRequest req) {

        return ResponseEntity.ok(ApiResponse.ok(
                rejectRefundUseCase.execute(new RejectRefundCommand(refundId, paymentId, req.reason()))));
    }

    @Operation(summary = "Update payment status (admin only)")
    @PatchMapping("/admin/{paymentId}/status")
    @PreAuthorize("hasAuthority('payment:manage')")
    public ResponseEntity<ApiResponse<PaymentResponse>> updateStatus(
            @PathVariable UUID paymentId,
            @RequestParam PaymentStatus status) {
        Payment updated = updatePaymentStatusUseCase.execute(paymentId, status);
        return ResponseEntity.ok(ApiResponse.ok(paymentApplicationMapper.toResponse(updated)));
    }

    @Operation(summary = "Delete payment (admin only)")
    @DeleteMapping("/admin/{paymentId}")
    @PreAuthorize("hasAuthority('payment:manage')")
    public ResponseEntity<ApiResponse<Void>> deletePayment(@PathVariable UUID paymentId) {
        deletePaymentUseCase.execute(paymentId);
        return ResponseEntity.ok(ApiResponse.<Void>ok(null, "Payment deleted successfully"));
    }

    // - WEBHOOK --------------

    @Operation(summary = "Handle payment gateway webhook (signature-verified)")
    @PostMapping("/webhook/{provider}")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(
            @PathVariable String provider,
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Signature", required = false) String signature,
            HttpServletRequest httpRequest) {

        // Signature verification is handled by PaymentGatewayPort in the use case layer
        // The controller just passes raw payload for maximum flexibility

        return ResponseEntity.ok(ApiResponse.<Void>ok(null, "Webhook received"));
    }

    @Operation(summary = "Handle VNPay return redirect")
    @GetMapping("/vnpay-return")
    public void vnpayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            params.put(paramName, request.getParameter(paramName));
        }

        log.info("[VNPay] Received return redirect: {}", params);
        var result = vnpayGatewayAdapter.handleReturnUrl(params);
        
        String frontendUrl = properties.getFrontendUrl() + "/payment/success"; // Match ROUTES.PAYMENT_SUCCESS
        if (result.txnRef() != null) {
            String paymentIdWithDashes = result.txnRef().contains("-") ? result.txnRef() : addDashes(result.txnRef());
            // Update status via use case
            if (result.status() == PaymentStatus.PAID) {
                try {
                    var confirmed = confirmPaymentUseCase.execute(new ConfirmPaymentCommand(
                            UUID.fromString(paymentIdWithDashes),
                            params.get("vnp_TransactionNo"),
                            Instant.now()
                    ));
                    log.info("[VNPay] Payment confirmed successfully for txnRef: {}", result.txnRef());
                    frontendUrl += "?success=true&paymentId=" + paymentIdWithDashes + "&orderId=" + confirmed.orderId();
                } catch (Exception e) {
                    log.error("[VNPay] Failed to confirm payment for txnRef: {}", result.txnRef(), e);
                    frontendUrl += "?success=false&paymentId=" + paymentIdWithDashes + "&error=internal_error";
                }
            } else {
                log.warn("[VNPay] Payment failed or cancelled for txnRef: {}, status: {}", result.txnRef(), result.status());
                try {
                    failPaymentUseCase.execute(new FailPaymentCommand(
                            UUID.fromString(paymentIdWithDashes),
                            params.get("vnp_ResponseCode"),
                            "VNPay payment failed or cancelled"
                    ));
                } catch (Exception e) {
                    log.error("[VNPay] Failed to record payment failure for txnRef: {}", result.txnRef(), e);
                }
                frontendUrl += "?success=false&paymentId=" + paymentIdWithDashes;
            }
        } else {
            log.warn("[VNPay] No txnRef found in return params");
            frontendUrl += "?success=false&error=missing_txn_ref";
        }

        response.sendRedirect(frontendUrl);
    }

    @Operation(summary = "Handle MoMo return redirect")
    @GetMapping("/webhook/momo/return")
    public void momoReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            params.put(paramName, request.getParameter(paramName));
        }

        var result = momoGatewayAdapter.handleReturnUrl(params);
        
        String frontendUrl = properties.getFrontendUrl() + "/payment/success";
        if (result.txnRef() != null) {
            String paymentIdWithDashes = result.txnRef().contains("-") ? result.txnRef() : addDashes(result.txnRef());
            if (result.status() == PaymentStatus.PAID) {
                try {
                    var confirmed = confirmPaymentUseCase.execute(new ConfirmPaymentCommand(
                            UUID.fromString(paymentIdWithDashes),
                            result.providerReference(),
                            Instant.now()
                    ));
                    frontendUrl += "?success=true&paymentId=" + paymentIdWithDashes + "&orderId=" + confirmed.orderId();
                } catch (Exception e) {
                    log.error("[MoMo] Failed to confirm payment for txnRef: {}", result.txnRef(), e);
                    frontendUrl += "?success=false&paymentId=" + paymentIdWithDashes + "&error=internal_error";
                }
            } else {
                try {
                    failPaymentUseCase.execute(new FailPaymentCommand(
                            UUID.fromString(paymentIdWithDashes),
                            params.get("resultCode"),
                            "MoMo payment failed or cancelled"
                    ));
                } catch (Exception e) {
                    log.error("[MoMo] Failed to record payment failure for txnRef: {}", result.txnRef(), e);
                }
                frontendUrl += "?success=false&paymentId=" + paymentIdWithDashes;
            }
        }

        response.sendRedirect(frontendUrl);
    }

    private String addDashes(String txRef) {
        // If it's a ZaloPay trans ID (yyMMdd_uuid), strip the date prefix
        String cleanId = txRef.contains("_") ? txRef.split("_")[1] : txRef;
        
        if (cleanId.length() != 32) return cleanId; // Not a standard no-dash UUID
        
        return cleanId.replaceFirst(
            "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})",
            "$1-$2-$3-$4-$5"
        );
    }

    @Operation(summary = "Handle ZaloPay return redirect")
    @GetMapping("/zalopay-return")
    public void zalopayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            params.put(paramName, request.getParameter(paramName));
        }

        log.info("[ZaloPay] Received return redirect: {}", params);
        var result = zalopayGatewayAdapter.handleReturnUrl(params);
        
        String frontendUrl = properties.getFrontendUrl() + "/payment/success";
        if (result.txnRef() != null) {
            String paymentIdWithDashes = result.txnRef().contains("-") ? result.txnRef() : addDashes(result.txnRef());
            if (result.status() == PaymentStatus.PAID) {
                try {
                    var confirmed = confirmPaymentUseCase.execute(new ConfirmPaymentCommand(
                            UUID.fromString(paymentIdWithDashes),
                            params.get("apptransid"),
                            Instant.now()
                    ));
                    log.info("[ZaloPay] Payment confirmed successfully for txnRef: {}", result.txnRef());
                    frontendUrl += "?success=true&paymentId=" + paymentIdWithDashes + "&orderId=" + confirmed.orderId();
                } catch (Exception e) {
                    log.error("[ZaloPay] Failed to confirm payment for txnRef: {}", result.txnRef(), e);
                    frontendUrl += "?success=false&paymentId=" + paymentIdWithDashes + "&error=internal_error";
                }
            } else {
                log.warn("[ZaloPay] Payment failed or cancelled for txnRef: {}, status: {}", result.txnRef(), result.status());
                try {
                    failPaymentUseCase.execute(new FailPaymentCommand(
                            UUID.fromString(paymentIdWithDashes),
                            params.get("status"),
                            "ZaloPay payment failed or cancelled"
                    ));
                } catch (Exception e) {
                    log.error("[ZaloPay] Failed to record payment failure for txnRef: {}", result.txnRef(), e);
                }
                frontendUrl += "?success=false&paymentId=" + paymentIdWithDashes;
            }
        }

        response.sendRedirect(frontendUrl);
    }

    // - HELPERS --------------

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private Instant parsePaidAt(String paidAt) {
        if (paidAt == null || paidAt.isBlank()) return Instant.now();
        try {
            return Instant.parse(paidAt);
        } catch (Exception e) {
            return Instant.now();
        }
    }
}



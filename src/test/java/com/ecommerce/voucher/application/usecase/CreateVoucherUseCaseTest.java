package com.ecommerce.voucher.application.usecase;

import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.ecommerce.voucher.application.command.CreateVoucherCommand;
import com.ecommerce.voucher.domain.entity.Voucher;
import com.ecommerce.voucher.domain.repository.VoucherRepository;
import com.ecommerce.voucher.domain.enums.VoucherType;
import com.ecommerce.voucher.domain.enums.VoucherScope;
import com.ecommerce.voucher.domain.enums.VoucherStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateVoucherUseCaseTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private CreateVoucherUseCase createVoucherUseCase;

    private UUID sellerId;

    @BeforeEach
    void setUp() {
        sellerId = UUID.randomUUID();
    }

    @Test
    void execute_ShouldThrowException_WhenNormalizedCodeAlreadyExists() {
        // Given
        String rawCode = " summer ";
        String normalizedCode = "SUMMER";
        CreateVoucherCommand cmd = createCommand(rawCode);

        when(voucherRepository.existsByCode(normalizedCode)).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> 
            createVoucherUseCase.execute(cmd, sellerId)
        );

        assertEquals(ErrorCode.VOUCHER_CODE_ALREADY_EXISTS, exception.getErrorCode());
        verify(voucherRepository).existsByCode(normalizedCode);
        verify(voucherRepository, never()).save(any());
    }

    @Test
    void execute_WithUserVoucherData_ShouldBeApplicable() {
        // Given
        UUID voucherId = UUID.fromString("019dbe6f-c434-7af5-93b3-27b0e132d05b");
        String code = "SUMMER1";
        
        // Construct voucher similar to user's DB row
        Voucher voucher = Voucher.builder()
                .id(voucherId)
                .code(code)
                .status(VoucherStatus.ACTIVE)
                .type(VoucherType.PERCENTAGE)
                .scope(VoucherScope.GLOBAL)
                .discountValue(new BigDecimal("20"))
                .maxDiscountAmount(new BigDecimal("100000"))
                .minOrderAmount(BigDecimal.ZERO)
                .maxUsageTotal(100)
                .maxUsagePerUser(1)
                .validFrom(Instant.parse("2026-04-22T07:40:00Z"))
                .validTo(Instant.parse("2030-04-30T07:40:00Z"))
                .currentUsageCount(0)
                .build();

        // Check if calculateDiscount returns applicable=true
        Voucher.DiscountResult result = voucher.calculateDiscount(
                new BigDecimal("1300000"),
                BigDecimal.ZERO,
                new HashSet<>(),
                new HashSet<>(),
                0
        );

        assertEquals(true, result.isApplicable(), "Reason: " + result.getReason());
        assertEquals(new BigDecimal("100000"), result.getDiscountAmount());
    }

    private CreateVoucherCommand createCommand(String code) {
        return new CreateVoucherCommand(
            code,
            "Summer Sale",
            "Description",
            VoucherType.PERCENTAGE,
            VoucherScope.GLOBAL,
            new BigDecimal("20"),
            new BigDecimal("100000"),
            BigDecimal.ZERO,
            100,
            1,
            Instant.now(),
            Instant.now().plusSeconds(86400),
            null,
            null,
            sellerId,
            false
        );
    }
}

package com.ecommerce.order.application.usecase;

import com.ecommerce.catalog.domain.entity.Product;
import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.ecommerce.order.application.command.CreateOrderCommand;
import com.ecommerce.order.application.command.CreateOrderCommand.OrderItemCommand;
import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.mapper.OrderApplicationMapper;
import com.ecommerce.order.application.port.InventoryQueryPort;
import com.ecommerce.order.application.port.VoucherQueryPort;
import com.ecommerce.order.domain.entity.Cart;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.repository.CartRepository;
import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.shared.event.EventPublisher;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.ecommerce.testutil.fixture.Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateOrderUseCase — Application Tests")
class CreateOrderUseCaseTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private ProductRepository productRepository;
    @Mock private InventoryQueryPort inventoryQueryPort;
    @Mock private VoucherQueryPort voucherQueryPort;
    @Mock private OrderApplicationMapper mapper;
    @Mock private EventPublisher eventPublisher;

    private CreateOrderUseCase sut;

    @BeforeEach
    void setUp() {
        sut = new CreateOrderUseCase(orderRepository, cartRepository, productRepository, inventoryQueryPort, voucherQueryPort, mapper, eventPublisher);
    }

    private CreateOrderCommand aCommand(UUID sellerId) {
        return new CreateOrderCommand(
                sellerId,
                Fixtures.SHIPPING_ADDRESS_ID,
                List.of(new OrderItemCommand(
                        Fixtures.PRODUCT_ID, Fixtures.VARIANT_ID, 2, new BigDecimal("50000"), "P", "S", null, null
                )),
                new BigDecimal("100000"),
                new BigDecimal("20000"),
                new BigDecimal("10000"),
                BigDecimal.ZERO,
                "VND",
                "Please deliver fast",
                null
        );
    }

    private OrderResponse aMockResponse() {
        return OrderResponse.builder()
                .id(Fixtures.ORDER_ID)
                .buyerId(Fixtures.USER_ID)
                .sellerId(Fixtures.SELLER_ID)
                .status("PENDING")
                .totalAmount(new BigDecimal("130000"))
                .currency("VND")
                .createdAt(java.time.Instant.now())
                .build();
    }

    @Nested
    @DisplayName("execute() — success")
    class Success {

        @Test
        @DisplayName("should create and save order correctly")
        void createsAndSaves() {
            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            Order saved = Fixtures.anOrder();
            Product product = Fixtures.aProduct();

            when(productRepository.findById(any())).thenReturn(Optional.of(product));
            when(inventoryQueryPort.getAvailableStock(any(), any())).thenReturn(100);
            when(orderRepository.save(any())).thenReturn(saved);
            when(mapper.toOrderResponse(any())).thenReturn(aMockResponse());

            sut.execute(
                    aCommand(Fixtures.SELLER_ID), Fixtures.USER_ID,
                    "192.168.1.1", "Mozilla/5.0");

            verify(orderRepository).save(captor.capture());
            Order captured = captor.getValue();
            assertThat(captured.getBuyerId()).isEqualTo(Fixtures.USER_ID);
            assertThat(captured.getSellerId()).isEqualTo(Fixtures.SELLER_ID);
            assertThat(captured.getBuyerNote()).isEqualTo("Please deliver fast");
        }

        @Test
        @DisplayName("should publish OrderCreatedEvent")
        void publishesEvent() {
            Product product = Fixtures.aProduct();
            when(productRepository.findById(any())).thenReturn(Optional.of(product));
            when(inventoryQueryPort.getAvailableStock(any(), any())).thenReturn(100);
            when(orderRepository.save(any())).thenReturn(Fixtures.anOrder());
            when(mapper.toOrderResponse(any())).thenReturn(aMockResponse());

            sut.execute(aCommand(Fixtures.SELLER_ID), Fixtures.USER_ID,
                    "ip", "ua");

            verify(eventPublisher).publish(any(
                    com.ecommerce.order.domain.event.OrderCreatedEvent.class));
        }

        @Test
        @DisplayName("should delete buyer's cart after order creation")
        void deletesCart() {
            Cart cart = Fixtures.aCart();
            Product product = Fixtures.aProduct();
            when(productRepository.findById(any())).thenReturn(Optional.of(product));
            when(inventoryQueryPort.getAvailableStock(any(), any())).thenReturn(100);
            when(orderRepository.save(any())).thenReturn(Fixtures.anOrder());
            when(cartRepository.findByBuyerId(Fixtures.USER_ID)).thenReturn(cart);
            when(mapper.toOrderResponse(any())).thenReturn(aMockResponse());

            sut.execute(aCommand(Fixtures.SELLER_ID), Fixtures.USER_ID,
                    "ip", "ua");

            verify(cartRepository).delete(cart);
        }

        @Test
        @DisplayName("should apply voucher discount when valid code provided")
        void appliesVoucher() {
            String voucherCode = "SAVE10";
            CreateOrderCommand cmd = new CreateOrderCommand(
                    Fixtures.SELLER_ID, Fixtures.SHIPPING_ADDRESS_ID,
                    List.of(new OrderItemCommand(Fixtures.PRODUCT_ID, Fixtures.VARIANT_ID, 1, new BigDecimal("100000"), "P1", "S1", null, null)),
                    new BigDecimal("100000"), new BigDecimal("10000"), new BigDecimal("10000"), BigDecimal.ZERO, "VND", null, voucherCode);

            Product product = Fixtures.aProduct();
            
            when(productRepository.findById(any())).thenReturn(Optional.of(product));
            when(inventoryQueryPort.getAvailableStock(any(), any())).thenReturn(100);
            when(orderRepository.save(any())).thenReturn(Fixtures.anOrder());
            when(mapper.toOrderResponse(any())).thenReturn(aMockResponse());
            
            when(voucherQueryPort.calculateDiscount(eq(voucherCode), any(), any(), any(), any(), any()))
                    .thenReturn(new VoucherQueryPort.DiscountInfo(true, new BigDecimal("10000"), null));

            sut.execute(cmd, Fixtures.USER_ID, "ip", "ua");

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(orderCaptor.capture());
            assertThat(orderCaptor.getValue().getDiscountAmount()).isEqualByComparingTo("10000");
            assertThat(orderCaptor.getValue().getAppliedVoucherCode()).isEqualTo(voucherCode);
        }
    }

    @Nested
    @DisplayName("execute() — error cases")
    class ErrorCases {

        @Test
        @DisplayName("should throw PRODUCT_INSUFFICIENT_STOCK when inventory returns less than requested")
        void insufficientStock() {
            Product product = Fixtures.aProduct();
            when(productRepository.findById(any())).thenReturn(Optional.of(product));
            // Order requests 2 units (see aCommand), but inventory only has 1
            when(inventoryQueryPort.getAvailableStock(any(), any())).thenReturn(1);

            assertThatThrownBy(() -> sut.execute(
                    aCommand(Fixtures.SELLER_ID),
                    Fixtures.USER_ID, "ip", "ua"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PRODUCT_INSUFFICIENT_STOCK);

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ORDER_FORBIDDEN when sellerId mismatch")
        void sellerIdMismatch() {
            Product product = Fixtures.aProduct();
            product.setSellerId(UUID.randomUUID()); // different seller
            
            when(productRepository.findById(any())).thenReturn(Optional.of(product));

            assertThatThrownBy(() -> sut.execute(
                    aCommand(Fixtures.SELLER_ID),
                    Fixtures.USER_ID, "ip", "ua"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ORDER_FORBIDDEN);
        }
    }
}
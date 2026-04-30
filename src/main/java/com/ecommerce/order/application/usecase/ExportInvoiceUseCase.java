package com.ecommerce.order.application.usecase;

import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.mapper.OrderMapper;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.user.domain.entity.Address;
import com.ecommerce.user.domain.repository.AddressRepository;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import com.ecommerce.shared.infrastructure.util.VietnameseNumberToWords;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExportInvoiceUseCase {

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final OrderMapper mapper;
    private final TemplateEngine templateEngine;

    @Transactional(readOnly = true)
    public byte[] execute(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        Address address = addressRepository.findById(order.getShippingAddressId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        OrderResponse orderResponse = mapper.toResponse(order);

        Context context = new Context();
        context.setVariable("order", orderResponse);
        context.setVariable("buyerName", address.getRecipientName());
        context.setVariable("buyerPhone", address.getRecipientPhone());
        context.setVariable("shippingAddress", address.getFullAddress());
        context.setVariable("totalInWords", VietnameseNumberToWords.convert(order.getTotalAmount()));

        String html = templateEngine.process("export/invoice", context);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            
            // Fix Vietnamese font issue (especially on Windows)
            File fontFile = new File("C:/Windows/Fonts/arial.ttf");
            if (fontFile.exists()) {
                builder.useFont(fontFile, "Arial");
            }
            File fontBoldFile = new File("C:/Windows/Fonts/arialbd.ttf");
            if (fontBoldFile.exists()) {
                builder.useFont(fontBoldFile, "Arial", 700, BaseRendererBuilder.FontStyle.NORMAL, true);
            }
            
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

}

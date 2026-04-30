package com.ecommerce.order.presentation.controller;

import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.usecase.ExportInvoiceUseCase;
import com.ecommerce.order.application.usecase.ListOrdersUseCase;
import com.ecommerce.shared.application.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/orders/export")
@RequiredArgsConstructor
@Tag(name = "Order Export", description = "Endpoints for exporting order invoices and data")
public class OrderExportController {
    private static final Logger log = LoggerFactory.getLogger(OrderExportController.class);

    private final ListOrdersUseCase listOrdersUseCase;
    private final ExportInvoiceUseCase exportInvoiceUseCase;
    private final ExportService exportService;

    @Operation(summary = "Export order invoice as PDF")
    @GetMapping("/{orderId}/pdf")
    @PreAuthorize("hasAuthority('order:manage') or hasAuthority('order:read')")
    public void exportInvoicePdf(@PathVariable UUID orderId, HttpServletResponse response) throws IOException {
        log.info("Request to export PDF for order: {}", orderId);
        try {
            byte[] pdf = exportInvoiceUseCase.execute(orderId);
            
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=invoice-" + orderId.toString().substring(0, 8).toUpperCase() + ".pdf");
            response.getOutputStream().write(pdf);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("CRITICAL: PDF Export Failed for {}: {}", orderId, e.getMessage(), e);
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"Lỗi xuất PDF: " + e.getMessage() + "\"}");
            }
        }
    }

    @Operation(summary = "Export orders list as Excel")
    @GetMapping("/excel")
    @PreAuthorize("hasAuthority('order:manage')")
    public void exportOrdersExcel(
            @RequestParam(required = false) String status,
            HttpServletResponse response) throws IOException {
        
        log.info("Request to export Excel list for status: {}", status);
        List<OrderResponse> orders = listOrdersUseCase.exportAllOrders(status);
        
        List<String> headers = List.of(
            "Mã Đơn Hàng", "Ngày Đặt", "Họ Tên Khách Hàng", "Email / SĐT", "Sản Phẩm Mua", "Tổng Tiền", "Tiền Tệ", "Phương Thức TT", "TT Thanh Toán", "Trạng Thái Đơn"
        );
        
        List<Map<String, Object>> data = new ArrayList<>();
        for (var o : orders) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("Mã Đơn Hàng", o.getId().toString().substring(0, 8).toUpperCase());
            row.put("Ngày Đặt", o.getCreatedAt().toString());
            
            // Note: In a real scenario, you'd want to enrich this data more robustly
            row.put("Họ Tên Khách Hàng", "Buyer " + o.getBuyerId().toString().substring(0, 8));
            row.put("Email / SĐT", "N/A");

            // Aggregate product names
            String productSummary = o.getItems().stream()
                .map(item -> item.getProductName() + " (x" + item.getQuantity() + ")")
                .collect(Collectors.joining(", "));
            row.put("Sản Phẩm Mua", productSummary);

            row.put("Tổng Tiền", o.getTotalAmount());
            row.put("Tiền Tệ", o.getCurrency());
            row.put("Phương Thức TT", o.getPaymentMethod() != null ? o.getPaymentMethod() : "N/A");
            row.put("TT Thanh Toán", o.getPaymentStatus());
            row.put("Trạng Thái Đơn", o.getStatus());
            data.add(row);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=bao-cao-ke-toan-don-hang.xlsx");
        
        exportService.exportToExcel("Báo cáo kế toán", headers, data, response.getOutputStream());
        log.info("Excel list exported successfully with {} records", orders.size());
    }
}

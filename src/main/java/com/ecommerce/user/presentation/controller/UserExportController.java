package com.ecommerce.user.presentation.controller;


import com.ecommerce.shared.application.service.ExportService;
import com.ecommerce.user.application.usecase.SearchUsersUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/export")
@RequiredArgsConstructor
@Tag(name = "User Export", description = "Endpoints for exporting user lists")
public class UserExportController {

    private final SearchUsersUseCase searchUsersUseCase;
    private final ExportService exportService;

    @Operation(summary = "Export user list as Excel")
    @GetMapping("/excel")
    @PreAuthorize("hasAuthority('user:manage')")
    public void exportUsersExcel(
            @RequestParam(required = false) String query,
            HttpServletResponse response) throws IOException {
        
        var users = searchUsersUseCase.execute(null, query, Pageable.unpaged());
        
        List<String> headers = List.of("ID", "Email", "Họ tên", "Số điện thoại", "Trạng thái", "Ngày tạo");
        List<Map<String, Object>> data = new ArrayList<>();
        
        users.forEach(user -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("ID", user.getId());
            row.put("Email", user.getEmail());
            row.put("Họ tên", user.getFullName());
            row.put("Số điện thoại", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
            row.put("Trạng thái", user.isActive() ? "Hoạt động" : "Bị khóa");
            row.put("Ngày tạo", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
            data.add(row);
        });

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=users-list.xlsx");
        
        exportService.exportToExcel("Danh sách người dùng", headers, data, response.getOutputStream());
    }
}

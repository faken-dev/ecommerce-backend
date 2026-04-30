package com.ecommerce.system.presentation.controller;

import com.ecommerce.shared.response.ApiResponse;
import com.ecommerce.system.infrastructure.persistence.entity.SettingJpaEntity;
import com.ecommerce.system.infrastructure.persistence.repository.SettingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/system/settings")
@RequiredArgsConstructor
public class SettingController {

    private final SettingJpaRepository repository;

    @GetMapping
    public ApiResponse<Map<String, String>> getAllSettings() {
        Map<String, String> settings = repository.findAll().stream()
                .collect(Collectors.toMap(SettingJpaEntity::getKey, SettingJpaEntity::getValue));
        return ApiResponse.ok(settings);
    }

    @PatchMapping
    public ApiResponse<Void> updateSettings(@RequestBody Map<String, String> updates) {
        updates.forEach((key, value) -> {
            repository.findById(key).ifPresentOrElse(
                entity -> {
                    entity.setValue(value);
                    repository.save(entity);
                },
                () -> {
                    SettingJpaEntity entity = new SettingJpaEntity();
                    entity.setKey(key);
                    entity.setValue(value);
                    repository.save(entity);
                }
            );
        });
        return ApiResponse.success(null);
    }
}



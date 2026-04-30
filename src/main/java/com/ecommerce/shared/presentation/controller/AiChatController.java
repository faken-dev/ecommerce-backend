package com.ecommerce.shared.presentation.controller;

import com.ecommerce.shared.application.dto.ChatRequest;
import com.ecommerce.shared.infrastructure.ai.GeminiChatService;
import com.ecommerce.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Chat", description = "AI Assistant endpoints")
public class AiChatController {

    private final GeminiChatService geminiChatService;

    @Operation(summary = "Get AI assistant response")
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<Map<String, String>>> chat(@RequestBody ChatRequest chatRequest) {
        String reply = geminiChatService.generateResponse(chatRequest);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("reply", reply), "AI Response generated"));
    }
}



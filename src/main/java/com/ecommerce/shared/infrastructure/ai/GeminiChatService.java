package com.ecommerce.shared.infrastructure.ai;

import com.ecommerce.catalog.application.dto.ProductSummaryResponse;
import com.ecommerce.catalog.application.usecase.SearchProductsUseCase;
import com.ecommerce.feedback.application.dto.ReviewResponse;
import com.ecommerce.feedback.application.usecase.GetProductReviewsUseCase;
import com.ecommerce.shared.application.dto.ChatRequest;
import com.ecommerce.shared.infrastructure.ai.dto.GeminiRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiChatService {

    private final GeminiClient geminiClient;
    private final ChatPromptTemplate promptTemplate;
    private final SearchProductsUseCase searchProductsUseCase;
    private final GetProductReviewsUseCase getProductReviewsUseCase;

    public String generateResponse(ChatRequest chatRequest) {
        // Build request body for Gemini API using DTOs
        List<GeminiRequest.Content> contents = new ArrayList<>();

        // Add history
        if (chatRequest.getHistory() != null) {
            for (ChatRequest.ChatMessage msg : chatRequest.getHistory()) {
                contents.add(createContent(msg.getRole(), msg.getContent()));
            }
        }

        // Add current message
        contents.add(createContent("user", chatRequest.getMessage()));

        // Fetch context and build system instruction
        String context = getChatContext(chatRequest.getMessage());
        String finalSystemPrompt = promptTemplate.buildSystemPrompt(context);

        GeminiRequest request = GeminiRequest.builder()
                .contents(contents)
                .systemInstruction(GeminiRequest.SystemInstruction.builder()
                        .parts(List.of(GeminiRequest.Part.builder().text(finalSystemPrompt).build()))
                        .build())
                .build();

        String response = geminiClient.generateContent(request);
        
        if (response != null) {
            return response;
        } else {
            // Gemini call failed (null). Return a safe fallback message.
            // Keep it ASCII-only to avoid mojibake/encoding issues in the frontend.
            return "Xin loi, hien tai toi dang gap mot so loi ket noi. Vui long thu lai sau vai giay!";
        }
    }

    private GeminiRequest.Content createContent(String role, String text) {
        return GeminiRequest.Content.builder()
                .role(role.equals("model") ? "model" : "user")
                .parts(List.of(GeminiRequest.Part.builder().text(text).build()))
                .build();
    }

    private String getChatContext(String message) {
        String query = extractSearchQuery(message);
        log.info("AI Chat query: {}", query); if (query.isEmpty()) return "Khong co thong tin phu hop de tim kiem.";

        List<ProductSummaryResponse> products = List.of();
        if (!query.isEmpty()) {
            products = searchProductsUseCase.search(query, 0, 5).getData();
        }

        if (products.isEmpty()) {
            log.info("AI Chat: No products found for query '{}', fetching featured/active products as fallback.", query);
            products = searchProductsUseCase.activePublic(0, 5).getData();
        }

        if (products.isEmpty()) {
            return "Hiện tại cửa hàng chưa có sản phẩm nào được đăng bán.";
        }

        StringBuilder context = new StringBuilder("Các sản phẩm tìm thấy:\n");
        for (ProductSummaryResponse p : products) {
            context.append(String.format("- Tên: %s, Giá: %,.0f VNĐ, Đánh giá: %s/5 (%d nhận xét)\n",
                    p.name(), p.price(), p.averageRating(), p.reviewCount()));
            
            // If it's the first result, get some reviews
            if (p.id().equals(products.get(0).id())) {
                List<ReviewResponse> reviews = getProductReviewsUseCase.execute(p.id(), PageRequest.of(0, 3)).getContent();
                if (!reviews.isEmpty()) {
                    context.append("  Đánh giá chi tiết khách hàng:\n");
                    for (ReviewResponse r : reviews) {
                        context.append(String.format("    + %s: %s (Rating: %d/5)\n", r.userName(), r.content(), r.rating()));
                    }
                }
            }
        }
        return context.toString();
    }

    private String extractSearchQuery(String message) {
        return message.toLowerCase()
                .replaceAll("(?i)^(tìm|xem|mua|giá|đánh giá|cho tôi biết về|hỏi về|có|bán)\\s+", "")
                .trim();
    }
}

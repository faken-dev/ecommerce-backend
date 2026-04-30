package com.ecommerce.shared.infrastructure.ai;

import com.ecommerce.shared.infrastructure.ai.dto.GeminiRequest;
import com.ecommerce.shared.infrastructure.ai.dto.GeminiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiClient {
    private final RestTemplate restTemplate;

    @Value("${app.ai.gemini.api-key}")
    private String apiKey;

    @Value("${app.ai.gemini.model}")
    private String model;

    public String generateContent(GeminiRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("Gemini API key is missing (app.ai.gemini.api-key)");
            return null;
        }
        if (model == null || model.isBlank()) {
            log.error("Gemini model is missing (app.ai.gemini.model)");
            return null;
        }

        // Some Gemini models are not available/supported under v1beta.
        // Retry with v1 when v1beta returns 404 (not found / not supported).
        String[] apiVersions = new String[] { "v1beta", "v1" };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        for (String apiVersion : apiVersions) {
            GeminiRequest finalRequest = request;
            
            // v1 (stable) often does not support the top-level 'system_instruction' field.
            // If calling v1 and system instruction is present, move it to the contents array.
            if ("v1".equals(apiVersion) && request.getSystemInstruction() != null) {
                List<GeminiRequest.Content> newContents = new ArrayList<>();
                
                // Prepend system instruction as a user message or combined with the first message
                StringBuilder systemText = new StringBuilder();
                if (request.getSystemInstruction().getParts() != null) {
                    for (GeminiRequest.Part part : request.getSystemInstruction().getParts()) {
                        systemText.append(part.getText()).append("\n");
                    }
                }
                
                // Add the system prompt as the very first message
                newContents.add(GeminiRequest.Content.builder()
                        .role("user")
                        .parts(List.of(GeminiRequest.Part.builder()
                                .text("[System Context]\n" + systemText.toString().trim())
                                .build()))
                        .build());
                
                // Add an acknowledgement from the model to keep the conversation alternating (optional, but safer)
                newContents.add(GeminiRequest.Content.builder()
                        .role("model")
                        .parts(List.of(GeminiRequest.Part.builder().text("Understood. I will follow those instructions.").build()))
                        .build());
                
                // Add original contents
                if (request.getContents() != null) {
                    newContents.addAll(request.getContents());
                }
                
                finalRequest = GeminiRequest.builder()
                        .contents(newContents)
                        .systemInstruction(null) // Important: remove it so it doesn't cause 400 error
                        .build();
            }

            HttpEntity<GeminiRequest> entity = new HttpEntity<>(finalRequest, (MultiValueMap<String, String>) headers);
            String url = String.format(
                    "https://generativelanguage.googleapis.com/%s/models/%s:generateContent?key=%s",
                    apiVersion,
                    model,
                    apiKey
            );

            int maxRetries = 2;
            int retryCount = 0;
            
            while (retryCount <= maxRetries) {
                try {
                    log.info("Calling Gemini API ({}) for model {}: {}", apiVersion, model, maskApiKey(url));
                    ResponseEntity<GeminiResponse> response = restTemplate.postForEntity(url, entity, GeminiResponse.class);

                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        String text = response.getBody().getFirstText();
                        if (text != null) return text;
                        log.warn("Gemini API returned empty response ({}).", apiVersion);
                        return null;
                    }

                    log.error("Gemini API error ({}): Status {}, Body {}", apiVersion, response.getStatusCode(), response.getBody());
                    return null;
                } catch (HttpClientErrorException e) {
                    // 404 is what we see when the model isn't available / supported for this API version.
                    if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        log.warn("Gemini API 404 for model on {} (will retry next version if any)", apiVersion);
                        break; // Break retry loop to try next apiVersion
                    }
                    // Handle 429 (Rate Limit) by retrying after a short delay
                    if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS && retryCount < maxRetries) {
                        log.warn("Gemini API 429 (Rate Limit) on {}, retrying {}/{}...", apiVersion, retryCount + 1, maxRetries);
                        sleep(1000 * (retryCount + 1));
                        retryCount++;
                        continue;
                    }
                    
                    if ("v1beta".equals(apiVersion)) {
                        log.warn("Gemini API v1beta failed with {}: {}, trying v1...", e.getStatusCode(), e.getResponseBodyAsString());
                        break;
                    }
                    log.error("Gemini API HTTP error ({}): status={}, body={}", apiVersion, e.getStatusCode(), e.getResponseBodyAsString());
                    return null;
                } catch (HttpServerErrorException e) {
                    // Handle 503 (Service Unavailable) by retrying after a short delay
                    if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE && retryCount < maxRetries) {
                        log.warn("Gemini API 503 (Service Unavailable) on {}, retrying {}/{}...", apiVersion, retryCount + 1, maxRetries);
                        sleep(1000 * (retryCount + 1));
                        retryCount++;
                        continue;
                    }
                    log.error("Gemini API Server error ({}): status={}, body={}", apiVersion, e.getStatusCode(), e.getResponseBodyAsString());
                    return null;
                } catch (Exception e) {
                    log.error("Error calling Gemini API ({}).", apiVersion, e);
                    return null;
                }
            }
        }

        return null;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String maskApiKey(String url) {
        // Avoid leaking the API key into logs.
        return url.replaceAll("key=[^&]+", "key=***");
    }
}

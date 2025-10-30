package com.slotfy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slotfy.dto.SchedulerSuggestRequest;
import com.slotfy.dto.SchedulerSuggestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for integrating with AWS Bedrock to generate scheduling suggestions
 */
@Service
public class BedrockService {
    
    private static final Logger logger = LoggerFactory.getLogger(BedrockService.class);
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 1000;
    
    @Value("${aws.region:us-east-1}")
    private String awsRegion;
    
    @Value("${bedrock.model.id:meta.llama3-70b-instruct-v1:0}")
    private String bedrockModelId;
    
    @Value("${aws.access.key.id:}")
    private String awsAccessKeyId;
    
    @Value("${aws.secret.access.key:}")
    private String awsSecretAccessKey;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Generate scheduling suggestions using Bedrock
     */
    public SchedulerSuggestResponse generateSuggestions(SchedulerSuggestRequest request) {
        String prompt = buildPrompt(request);
        String response = invokeBedrockWithRetry(prompt);
        return parseResponse(response);
    }
    
    /**
     * Build the prompt for Bedrock model
     */
    String buildPrompt(SchedulerSuggestRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Você é um assistente que sugere horários para agendamento. RETORNE APENAS JSON válido no formato indicado. Contexto:\n\n");
        prompt.append("timezone: ").append(request.getTimezone()).append(" (IANA)\n");
        prompt.append("duration (minutos): ").append(request.getDuration()).append("\n");
        prompt.append("buffer (minutos): ").append(request.getBuffer() != null ? request.getBuffer() : 0).append("\n");
        
        // Available windows
        if (request.getAvailableWindows() != null && !request.getAvailableWindows().isEmpty()) {
            prompt.append("availableWindows: ").append(formatTimeWindows(request.getAvailableWindows())).append("\n");
        } else {
            prompt.append("availableWindows: [] (considerar dia inteiro)\n");
        }
        
        // Busy slots
        if (request.getBusySlots() != null && !request.getBusySlots().isEmpty()) {
            prompt.append("busySlots: ").append(formatTimeWindows(request.getBusySlots())).append("\n");
        } else {
            prompt.append("busySlots: [] (sem bloqueios)\n");
        }
        
        prompt.append("preferences: ").append(request.getPreferences() != null ? request.getPreferences() : "nenhuma").append("\n");
        prompt.append("maxSuggestions: ").append(request.getMaxSuggestions()).append("\n\n");
        
        prompt.append("Regras (obrigatórias):\n");
        prompt.append("1. Não sugira horários que conflitem com busySlots, considerando buffer.\n");
        prompt.append("2. Respeite availableWindows.\n");
        prompt.append("3. Cada sugestão deve ter duração completa (duration + buffers).\n");
        prompt.append("4. Gere no máximo maxSuggestions opções, ordenadas da melhor para a pior.\n");
        prompt.append("5. Evite horários fora do horário comercial (09:00–18:00 local), salvo se preferences pedir.\n");
        prompt.append("6. Retorne somente um objeto JSON com campo \"suggestions\".\n\n");
        
        prompt.append("Formato exigido da saída (copiar e respeitar):\n");
        prompt.append("{\n");
        prompt.append("  \"suggestions\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"start\": \"2025-11-03T08:30:00-03:00\",\n");
        prompt.append("      \"end\": \"2025-11-03T09:30:00-03:00\",\n");
        prompt.append("      \"reason\": \"Breve justificativa\",\n");
        prompt.append("      \"score\": 0.92\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    /**
     * Format time windows as JSON string
     */
    private String formatTimeWindows(List<SchedulerSuggestRequest.TimeWindow> windows) {
        try {
            return objectMapper.writeValueAsString(windows);
        } catch (Exception e) {
            logger.error("Error formatting time windows", e);
            return "[]";
        }
    }
    
    /**
     * Invoke Bedrock with exponential backoff retry
     */
    private String invokeBedrockWithRetry(String prompt) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                return invokeBedrock(prompt);
            } catch (Exception e) {
                if (attempt < MAX_RETRIES - 1) {
                    long backoffTime = INITIAL_BACKOFF_MS * (long) Math.pow(2, attempt);
                    logger.warn("Bedrock invocation failed, retrying in {}ms (attempt {}/{})", 
                               backoffTime, attempt + 1, MAX_RETRIES, e);
                    try {
                        Thread.sleep(backoffTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                } else {
                    logger.error("Bedrock invocation failed after {} attempts", MAX_RETRIES, e);
                    throw new RuntimeException("Failed to invoke Bedrock after retries", e);
                }
            }
        }
        throw new RuntimeException("Failed to invoke Bedrock");
    }
    
    /**
     * Invoke Bedrock model
     */
    private String invokeBedrock(String prompt) {
        try {
            BedrockRuntimeClient client = createBedrockClient();
            
            // Build request payload for Meta Llama model
            String requestBody = String.format(
                "{\"prompt\": \"%s\", \"max_gen_len\": 2048, \"temperature\": 0.1, \"top_p\": 0.9}",
                escapeJson(prompt)
            );
            
            InvokeModelRequest invokeRequest = InvokeModelRequest.builder()
                .modelId(bedrockModelId)
                .body(SdkBytes.fromUtf8String(requestBody))
                .build();
            
            logger.debug("Invoking Bedrock model: {}", bedrockModelId);
            InvokeModelResponse invokeResponse = client.invokeModel(invokeRequest);
            
            String responseBody = invokeResponse.body().asUtf8String();
            logger.debug("Received Bedrock response");
            
            // Extract generation from Meta model response
            JsonNode responseJson = objectMapper.readTree(responseBody);
            String generation = responseJson.get("generation").asText();
            
            client.close();
            return generation;
            
        } catch (Exception e) {
            logger.error("Error invoking Bedrock", e);
            throw new RuntimeException("Failed to invoke Bedrock", e);
        }
    }
    
    /**
     * Create Bedrock client
     */
    private BedrockRuntimeClient createBedrockClient() {
        software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClientBuilder builder = 
            BedrockRuntimeClient.builder()
                .region(Region.of(awsRegion));
        
        // Use explicit credentials if provided, otherwise use default credential chain
        if (awsAccessKeyId != null && !awsAccessKeyId.isEmpty() && 
            awsSecretAccessKey != null && !awsSecretAccessKey.isEmpty()) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey);
            builder.credentialsProvider(StaticCredentialsProvider.create(credentials));
        }
        
        return builder.build();
    }
    
    /**
     * Parse Bedrock response and extract JSON
     */
    SchedulerSuggestResponse parseResponse(String response) {
        try {
            // Try to parse direct JSON
            String jsonContent = extractJson(response);
            
            if (jsonContent == null || jsonContent.isEmpty()) {
                logger.warn("No valid JSON found in Bedrock response");
                return new SchedulerSuggestResponse(new ArrayList<>());
            }
            
            JsonNode rootNode = objectMapper.readTree(jsonContent);
            JsonNode suggestionsNode = rootNode.get("suggestions");
            
            if (suggestionsNode == null || !suggestionsNode.isArray()) {
                logger.warn("Invalid suggestions format in response");
                return new SchedulerSuggestResponse(new ArrayList<>());
            }
            
            List<SchedulerSuggestResponse.Suggestion> suggestions = new ArrayList<>();
            for (JsonNode suggestionNode : suggestionsNode) {
                SchedulerSuggestResponse.Suggestion suggestion = new SchedulerSuggestResponse.Suggestion(
                    suggestionNode.get("start").asText(),
                    suggestionNode.get("end").asText(),
                    suggestionNode.has("reason") ? suggestionNode.get("reason").asText() : "",
                    suggestionNode.has("score") ? suggestionNode.get("score").asDouble() : 0.0
                );
                suggestions.add(suggestion);
            }
            
            return new SchedulerSuggestResponse(suggestions);
            
        } catch (Exception e) {
            logger.error("Error parsing Bedrock response", e);
            return new SchedulerSuggestResponse(new ArrayList<>());
        }
    }
    
    /**
     * Extract JSON from response text (finds content between first { and last })
     */
    String extractJson(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        
        int firstBrace = text.indexOf('{');
        int lastBrace = text.lastIndexOf('}');
        
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return text.substring(firstBrace, lastBrace + 1);
        }
        
        return null;
    }
    
    /**
     * Escape JSON special characters
     */
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}

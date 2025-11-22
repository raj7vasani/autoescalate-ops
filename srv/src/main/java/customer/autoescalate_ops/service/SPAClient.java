package customer.autoescalate_ops.service;

import com.fasterxml.jackson.databind.JsonNode;
import customer.autoescalate_ops.config.SPAProperties;
import customer.autoescalate_ops.entity.Issue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class SPAClient {

    private static final Logger logger = LoggerFactory.getLogger(SPAClient.class);

    private final WebClient webClient;
    private final SPAProperties spaProperties;

    public SPAClient(WebClient webClient, SPAProperties spaProperties) {
        this.webClient = webClient;
        this.spaProperties = spaProperties;
    }

    public void startWorkflow(Issue issue) {
        startWorkflowWithApiKey(issue)
                .doOnSuccess(response -> logger.info("Successfully started workflow for issue {}: {}", issue.getId(), response))
                .doOnError(error -> logger.error("Failed to start workflow for issue {}: {}", issue.getId(), error.getMessage()))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    logger.error("SPA API error for issue {}: Status={}, Body={}", issue.getId(), ex.getStatusCode(), ex.getResponseBodyAsString());
                    return Mono.empty();
                })
                .subscribe();
    }

    private Mono<String> fetchToken() {
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(
                (spaProperties.getClientId() + ":" + spaProperties.getClientSecret()).getBytes()
        );

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");

        logger.info("Fetching OAuth token from: {}", spaProperties.getTokenUrl());

        return webClient.post()
                .uri(spaProperties.getTokenUrl())
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> json.get("access_token").asText())
                .doOnSuccess(token -> logger.info("Successfully retrieved OAuth token"))
                .doOnError(error -> logger.error("Failed to fetch OAuth token: {}", error.getMessage()));
    }

    private Mono<String> startWorkflowWithToken(String token, Issue issue) {
        String url = spaProperties.getBaseUrl() + "/public/workflow/rest/v1/workflow-instances";
        Map<String, Object> requestBody = buildRequestBody(issue);

        logger.info("Starting SPA workflow for issue: {} at URL: {}", issue.getId(), url);

        return webClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .header("X-Environment-Id", spaProperties.getEnvironmentId())
                .header("X-API-Key", spaProperties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);
    }

    private Mono<String> startWorkflowWithApiKey(Issue issue) {
        String url = spaProperties.getBaseUrl() + "/public/workflow/rest/v1/workflow-instances";
        Map<String, Object> requestBody = buildRequestBody(issue);

        logger.info("Starting SPA workflow for issue: {} at URL: {} (using API Key only)", issue.getId(), url);

        return webClient.post()
                .uri(url)
                .header("X-Environment-Id", spaProperties.getEnvironmentId())
                .header("X-API-Key", spaProperties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);
    }

    private Map<String, Object> buildRequestBody(Issue issue) {
        Map<String, Object> startEvent = new HashMap<>();
        startEvent.put("issueid", issue.getId().toString());
        startEvent.put("title", issue.getTitle());
        startEvent.put("description", issue.getDescription() != null ? issue.getDescription() : "");
        startEvent.put("type", issue.getType());
        startEvent.put("priority", issue.getPriority());
        startEvent.put("location", issue.getLocation() != null ? issue.getLocation() : "");
        startEvent.put("severity", issue.getSeverity() != null ? issue.getSeverity() : 0);
        startEvent.put("date", issue.getReportedAt().format(DateTimeFormatter.ISO_DATE_TIME));

        Map<String, Object> context = new HashMap<>();
        context.put("startEvent", startEvent);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("definitionId", spaProperties.getDefinitionId());
        requestBody.put("context", context);

        return requestBody;
    }
}

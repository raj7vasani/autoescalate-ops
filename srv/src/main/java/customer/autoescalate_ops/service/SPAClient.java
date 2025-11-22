package customer.autoescalate_ops.service;

import customer.autoescalate_ops.config.SPAProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SPAClient {

    private static final Logger logger = LoggerFactory.getLogger(SPAClient.class);

    private final WebClient webClient;
    private final SPAProperties spaProperties;

    public SPAClient(WebClient webClient, SPAProperties spaProperties) {
        this.webClient = webClient;
        this.spaProperties = spaProperties;
    }

    public void startWorkflow(UUID issueId, String title, String priority, String type) {
        try {
            String url = buildWorkflowUrl();
            Map<String, Object> requestBody = buildRequestBody(issueId, title, priority, type);

            logger.info("Starting SPA workflow for issue: {}", issueId);

            webClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + spaProperties.getApiKey())
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> logger.info("Successfully started workflow for issue {}: {}", issueId, response))
                    .doOnError(error -> logger.error("Failed to start workflow for issue {}: {}", issueId, error.getMessage()))
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        logger.error("SPA API error for issue {}: Status={}, Body={}", issueId, ex.getStatusCode(), ex.getResponseBodyAsString());
                        return Mono.empty();
                    })
                    .subscribe();

        } catch (Exception e) {
            logger.error("Exception while starting workflow for issue {}: {}", issueId, e.getMessage(), e);
        }
    }

    private String buildWorkflowUrl() {
        return String.format("%s/workflow/rest/v1/workflow-instances?environmentId=%s",
                spaProperties.getBaseUrl(),
                spaProperties.getEnvironmentId());
    }

    private Map<String, Object> buildRequestBody(UUID issueId, String title, String priority, String type) {
        Map<String, Object> context = new HashMap<>();
        context.put("issueId", issueId.toString());
        context.put("title", title);
        context.put("priority", priority);
        context.put("type", type);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("definitionId", spaProperties.getDefinitionId());
        requestBody.put("context", context);

        return requestBody;
    }
}

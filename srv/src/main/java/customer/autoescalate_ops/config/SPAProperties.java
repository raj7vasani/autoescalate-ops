package customer.autoescalate_ops.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spa")
public class SPAProperties {

    private String baseUrl;
    private String environmentId;
    private String apiKey;
    private String definitionId;

    // Getters and Setters
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(String definitionId) {
        this.definitionId = definitionId;
    }
}

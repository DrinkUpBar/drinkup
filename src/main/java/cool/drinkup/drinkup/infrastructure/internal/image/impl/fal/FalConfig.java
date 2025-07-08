package cool.drinkup.drinkup.infrastructure.internal.image.impl.fal;

import cool.drinkup.drinkup.infrastructure.internal.image.config.properties.FalProperties;

public record FalConfig(
        String apiKey, String endpointId, Integer timeout, FalProperties.ImageGenerationRequest imageProperties) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String apiKey;
        private String endpointId = "fal-ai/fast-sdxl";
        private Integer timeout = 30000;
        private FalProperties.ImageGenerationRequest imageProperties = new FalProperties.ImageGenerationRequest();

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder endpointId(String endpointId) {
            this.endpointId = endpointId;
            return this;
        }

        public Builder timeout(Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder imageProperties(FalProperties.ImageGenerationRequest imageProperties) {
            this.imageProperties = imageProperties;
            return this;
        }

        public FalConfig build() {
            if (apiKey == null) {
                throw new IllegalArgumentException("apiKey is required");
            }
            return new FalConfig(apiKey, endpointId, timeout, imageProperties);
        }
    }
}

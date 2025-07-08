package cool.drinkup.drinkup.infrastructure.internal.image.impl.glif;

public record GlifConfig(String apiUrl, String bearerToken, String glifId, String weights) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String apiUrl = "https://simple-api.glif.app";
        private String bearerToken;
        private String glifId;
        private String weights =
                "https://huggingface.co/alvdansen/softpasty-flux-dev/resolve/main/araminta_k_softpasty_diffusion_flux.safetensors";

        public Builder apiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
            return this;
        }

        public Builder bearerToken(String bearerToken) {
            this.bearerToken = bearerToken;
            return this;
        }

        public Builder glifId(String glifId) {
            this.glifId = glifId;
            return this;
        }

        public Builder weights(String weights) {
            this.weights = weights;
            return this;
        }

        public GlifConfig build() {
            if (bearerToken == null || glifId == null) {
                throw new IllegalArgumentException("bearerToken and glifId are required");
            }
            return new GlifConfig(apiUrl, bearerToken, glifId, weights);
        }
    }
}

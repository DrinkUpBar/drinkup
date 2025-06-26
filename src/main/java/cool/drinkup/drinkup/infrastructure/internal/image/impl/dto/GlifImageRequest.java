package cool.drinkup.drinkup.infrastructure.internal.image.impl.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class GlifImageRequest {
    private final String id;
    private final Input inputs;

    public record Input(String weights, String prompt) {}
}

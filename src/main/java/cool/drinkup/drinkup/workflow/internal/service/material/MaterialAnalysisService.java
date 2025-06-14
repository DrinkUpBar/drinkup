package cool.drinkup.drinkup.workflow.internal.service.material;

public interface MaterialAnalysisService {

    MaterialAnalysisResult analyzeMaterial(Long materialId);

    public record MaterialAnalysisResult(
        String description
    ) {
    }
}
package cool.drinkup.drinkup.workflow.internal.service.material;

public interface MaterialAnalysisService {

    MaterialAnalysisResult analyzeMaterial(String materialText);

    public record MaterialAnalysisResult(String description) {}
}

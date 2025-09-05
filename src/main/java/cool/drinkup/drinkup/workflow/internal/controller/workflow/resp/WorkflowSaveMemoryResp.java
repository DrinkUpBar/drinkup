package cool.drinkup.drinkup.workflow.internal.controller.workflow.resp;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
import lombok.Data;

@Data
public class WorkflowSaveMemoryResp {
    private String status;

    @JsonAlias("messages_saved")
    private Integer messagesSaved;

    @JsonAlias("memory_ids")
    private List<String> memoryIds;
}

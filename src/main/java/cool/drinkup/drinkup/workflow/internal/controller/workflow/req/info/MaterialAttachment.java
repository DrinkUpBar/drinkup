package cool.drinkup.drinkup.workflow.internal.controller.workflow.req.info;

import java.util.List;

import lombok.Data;

@Data
public class MaterialAttachment {
    private Long categoryId;

    private List<Long> selectedMaterialIdList;
}

package cool.drinkup.drinkup.workflow.internal.controller.bar.req;

import java.util.List;

import lombok.Data;

@Data
public class BarProcurementCreateReq {

    private List<InnerBarProcurementCreateReq> barProcurements;

    @Data
    public static class InnerBarProcurementCreateReq {
        private String name;
        private String type;
        private String iconType;
        private String description;
    }
}
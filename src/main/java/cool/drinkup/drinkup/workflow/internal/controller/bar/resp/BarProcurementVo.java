package cool.drinkup.drinkup.workflow.internal.controller.bar.resp;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import cool.drinkup.drinkup.workflow.internal.model.Bar;
import lombok.Data;

@Data
public class BarProcurementVo {

    private Long id;

    @JsonProperty("barId")
    @JsonIdentityReference(alwaysAsId = true)
    private Bar bar;

    private String name;

    private String type;

    private String iconType;

    private String description;

    private String tag;
}

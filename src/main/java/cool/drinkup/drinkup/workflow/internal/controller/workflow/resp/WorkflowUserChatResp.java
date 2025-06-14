package cool.drinkup.drinkup.workflow.internal.controller.workflow.resp;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

import lombok.Data;

@Data
public class WorkflowUserChatResp {
    @JsonAlias("chat_with_user")
    private String chatWithUser;
    @JsonAlias({"order_to_your_bartender", "order_to_yourbartender"})
    private OrderToYourBartender orderToYourBartender;

    @Data
    public static class OrderToYourBartender {
        @JsonAlias("user_demand")
        private String userDemand;
        @JsonAlias("ready_to_serve")
        private boolean readyToServe;
        @JsonAlias("image_user_stock")
        private List<UserStock> imageUserStock;
        @Data
        public static class UserStock {
            private String name;
            private String type;
            private String iconType;
            private String description;
        }
    }
}

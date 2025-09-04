package cool.drinkup.drinkup.common.log.event;

public class UserEvent {
    public static final String USER = "USER";

    public static class BehaviorEvent {
        public static final String LOGIN = "LOGIN";
        public static final String LOGOUT = "LOGOUT";
        public static final String REGISTER = "REGISTER";
        public static final String PROFILE_GET = "PROFILE_GET";
        public static final String PROFILE_UPDATE = "PROFILE_UPDATE";
    }
}

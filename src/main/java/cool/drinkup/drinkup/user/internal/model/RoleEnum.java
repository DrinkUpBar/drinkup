package cool.drinkup.drinkup.user.internal.model;

/**
 * 系统角色枚举类，定义系统中所有可能的角色
 */
public enum RoleEnum {
    ADMIN("系统管理员"),
    USER("普通用户"),
    MANAGER("管理人员");

    private final String description;

    RoleEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getRole() {
        return this.name();
    }
}

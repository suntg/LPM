package com.example.lpm.constant;

public enum UseStateEnum {

    // 删除
    DELETED(0),
    // 可使用
    USABLE(1),
    // 已使用
    USED(2),
    // 不可用
    DISABLED(3),
    // 使用中
    IN_USE(4), Null;

    private Integer state;

    UseStateEnum(int i) {}

    UseStateEnum() {}

    public static UseStateEnum valueOfState(Integer state) {
        for (UseStateEnum obj : UseStateEnum.values()) {
            if (java.util.Objects.equals(obj.state, state)) {
                return obj;
            }
        }
        return Null;
    }

    public Integer getState() {
        return state;
    }
}

package com.example.lpm.constant;

public enum FileLogTypeEnum {

    PHONE(1), PAGE(2), Null;

    private Integer type;

    FileLogTypeEnum() {}

    FileLogTypeEnum(int i) {}

    public static FileLogTypeEnum valueOfType(Integer type) {
        for (FileLogTypeEnum obj : FileLogTypeEnum.values()) {
            if (java.util.Objects.equals(obj.type, type)) {
                return obj;
            }
        }
        return Null;
    }

    public Integer getType() {
        return type;
    }
}

package org.drools.planner.examples.pas.domain;

/**
 * @author Geoffrey De Smet
 */
public enum Gender {
    MALE("M"),
    FEMALE("F");

    public static Gender valueOfCode(String code) {
        for (Gender gender : values()) {
            if (code.equalsIgnoreCase(gender.getCode())) {
                return gender;
            }
        }
        return null;
    }

    private String code;

    private Gender(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
package com.onlineSchool.model;

public enum Role {
    ADMIN("Администратор"),
    TEACHER("Учитель"),
    STUDENT("Студент"),
    USER("Пользователь");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
package com.hotel.entity;

public enum CleanTaskStatus {
    PENDING("待领取"),
    IN_PROGRESS("清洁中"),
    PENDING_REVIEW("待复查"),
    REJECTED("已退回"),
    COMPLETED("已完成");

    private String description;

    CleanTaskStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

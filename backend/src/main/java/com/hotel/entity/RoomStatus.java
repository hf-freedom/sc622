package com.hotel.entity;

public enum RoomStatus {
    AVAILABLE("可预订"),
    OCCUPIED("已入住"),
    CLEANING("清洁中"),
    PENDING_INSPECTION("待复查"),
    OUT_OF_SERVICE("维修中");

    private String description;

    RoomStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

package com.hotel.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Room {
    private Long id;
    private String roomNo;
    private String roomType;
    private Double price;
    private RoomStatus status;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private LocalDateTime lastCleanTime;
    private Integer cleanCount = 0;
    private Integer repairDays = 0;
    private String repairDescription;
    private LocalDateTime repairStartTime;
}

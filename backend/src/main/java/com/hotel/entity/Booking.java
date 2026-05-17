package com.hotel.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Booking {
    private Long id;
    private Long roomId;
    private String roomNo;
    private String guestName;
    private String guestPhone;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private LocalDateTime createTime;
    private Boolean checkedIn = false;
    private LocalDateTime actualCheckInTime;
    private LocalDateTime actualCheckOutTime;
}

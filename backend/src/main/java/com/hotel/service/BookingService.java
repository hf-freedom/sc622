package com.hotel.service;

import com.hotel.entity.*;
import com.hotel.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private RoomService roomService;

    @Autowired
    private CleanTaskService cleanTaskService;

    public String validateBooking(Long roomId, LocalDateTime checkInDate, LocalDateTime checkOutDate) {
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            return "房间不存在";
        }
        if (room.getStatus() == RoomStatus.OUT_OF_SERVICE) {
            return "该房间正在维修，无法预订";
        }
        if (checkInDate == null || checkOutDate == null) {
            return "请选择入住和退房日期";
        }
        if (checkInDate.isBefore(LocalDateTime.now())) {
            return "入住日期不能早于当前时间";
        }
        if (checkInDate.isAfter(checkOutDate)) {
            return "入住日期不能晚于退房日期";
        }
        List<Booking> existingBookings = DataStore.BOOKINGS.values().stream()
                .filter(b -> b.getRoomId().equals(roomId))
                .filter(b -> !b.getCheckedIn())
                .collect(Collectors.toList());
        
        for (Booking booking : existingBookings) {
            LocalDateTime existingCheckIn = booking.getCheckInDate();
            LocalDateTime existingCheckOut = booking.getCheckOutDate();
            if (existingCheckIn != null && existingCheckOut != null) {
                if (!(checkOutDate.isBefore(existingCheckIn) || checkInDate.isAfter(existingCheckOut))) {
                    return String.format("该房间在 %s 至 %s 已有预订，请选择其他日期", 
                        existingCheckIn.toString(), existingCheckOut.toString());
                }
            }
        }
        if (room.getStatus() == RoomStatus.OCCUPIED) {
            return "该房间当前已入住，暂时无法预订";
        }
        if (room.getStatus() == RoomStatus.CLEANING) {
            return "该房间正在清洁中，请稍后再试或选择其他房间";
        }
        if (room.getStatus() == RoomStatus.PENDING_INSPECTION) {
            return "该房间等待复查中，请稍后再试或选择其他房间";
        }
        return null;
    }

    public Booking createBooking(Booking booking) {
        String error = validateBooking(booking.getRoomId(), booking.getCheckInDate(), booking.getCheckOutDate());
        if (error != null) {
            throw new RuntimeException(error);
        }
        Room room = roomService.getRoomById(booking.getRoomId());
        booking.setId(DataStore.BOOKING_ID_GENERATOR.getAndIncrement());
        booking.setRoomNo(room.getRoomNo());
        booking.setCreateTime(LocalDateTime.now());
        booking.setCheckedIn(false);
        DataStore.BOOKINGS.put(booking.getId(), booking);
        return booking;
    }

    public Booking checkIn(Long bookingId) {
        Booking booking = DataStore.BOOKINGS.get(bookingId);
        if (booking == null) {
            throw new RuntimeException("预订不存在");
        }
        if (booking.getCheckedIn()) {
            throw new RuntimeException("该预订已入住");
        }
        Room room = roomService.getRoomById(booking.getRoomId());
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new RuntimeException("房间当前状态不可入住");
        }
        booking.setCheckedIn(true);
        booking.setActualCheckInTime(LocalDateTime.now());
        room.setStatus(RoomStatus.OCCUPIED);
        room.setCheckInTime(LocalDateTime.now());
        return booking;
    }

    public Booking checkOut(Long bookingId) {
        Booking booking = DataStore.BOOKINGS.get(bookingId);
        if (booking == null) {
            throw new RuntimeException("预订不存在");
        }
        if (!booking.getCheckedIn()) {
            throw new RuntimeException("该预订未入住");
        }
        booking.setActualCheckOutTime(LocalDateTime.now());
        Room room = roomService.getRoomById(booking.getRoomId());
        room.setCheckOutTime(LocalDateTime.now());
        CleanTask task = cleanTaskService.createCleanTask(room.getId());
        task.setCreateTime(LocalDateTime.now());
        return booking;
    }

    public List<Booking> getAllBookings() {
        return DataStore.BOOKINGS.values().stream().collect(Collectors.toList());
    }

    public Booking getBookingById(Long id) {
        return DataStore.BOOKINGS.get(id);
    }

    public boolean deleteBooking(Long id) {
        Booking booking = DataStore.BOOKINGS.get(id);
        if (booking == null) {
            throw new RuntimeException("预订不存在");
        }
        if (booking.getCheckedIn()) {
            throw new RuntimeException("已入住的预订无法删除");
        }
        DataStore.BOOKINGS.remove(id);
        return true;
    }
}

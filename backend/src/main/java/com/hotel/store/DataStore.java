package com.hotel.store;

import com.hotel.entity.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class DataStore {
    public static final Map<Long, Room> ROOMS = new ConcurrentHashMap<>();
    public static final Map<Long, Booking> BOOKINGS = new ConcurrentHashMap<>();
    public static final Map<Long, CleanTask> CLEAN_TASKS = new ConcurrentHashMap<>();
    public static final List<String> ALERTS = new ArrayList<>();

    public static final AtomicLong ROOM_ID_GENERATOR = new AtomicLong(1);
    public static final AtomicLong BOOKING_ID_GENERATOR = new AtomicLong(1);
    public static final AtomicLong TASK_ID_GENERATOR = new AtomicLong(1);

    static {
        initRooms();
    }

    private static void initRooms() {
        String[] types = {"标准间", "大床房", "豪华套房"};
        double[] prices = {299.0, 399.0, 699.0};
        
        for (int i = 1; i <= 15; i++) {
            Room room = new Room();
            room.setId(ROOM_ID_GENERATOR.getAndIncrement());
            room.setRoomNo(String.format("%d%02d", (i - 1) / 5 + 1, i));
            room.setRoomType(types[(i - 1) % 3]);
            room.setPrice(prices[(i - 1) % 3]);
            room.setStatus(RoomStatus.AVAILABLE);
            ROOMS.put(room.getId(), room);
        }
    }
}

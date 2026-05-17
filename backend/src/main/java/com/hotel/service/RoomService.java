package com.hotel.service;

import com.hotel.entity.Room;
import com.hotel.entity.RoomStatus;
import com.hotel.store.DataStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {

    public List<Room> getAllRooms() {
        return DataStore.ROOMS.values().stream().collect(Collectors.toList());
    }

    public Room getRoomById(Long id) {
        return DataStore.ROOMS.get(id);
    }

    public List<Room> getAvailableRooms() {
        return DataStore.ROOMS.values().stream()
                .filter(room -> room.getStatus() == RoomStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    public Room updateRoomStatus(Long id, RoomStatus status) {
        Room room = DataStore.ROOMS.get(id);
        if (room != null) {
            room.setStatus(status);
        }
        return room;
    }

    public Room reportRepair(Long id, String description) {
        Room room = DataStore.ROOMS.get(id);
        if (room != null) {
            room.setStatus(RoomStatus.OUT_OF_SERVICE);
            room.setRepairDescription(description);
            room.setRepairStartTime(LocalDateTime.now());
        }
        return room;
    }

    public Room fixRoom(Long id) {
        Room room = DataStore.ROOMS.get(id);
        if (room != null && room.getStatus() == RoomStatus.OUT_OF_SERVICE) {
            if (room.getRepairStartTime() != null) {
                int days = (int) ChronoUnit.DAYS.between(room.getRepairStartTime(), LocalDateTime.now());
                room.setRepairDays(room.getRepairDays() + Math.max(1, days));
            }
            room.setStatus(RoomStatus.AVAILABLE);
            room.setRepairDescription(null);
            room.setRepairStartTime(null);
        }
        return room;
    }
}

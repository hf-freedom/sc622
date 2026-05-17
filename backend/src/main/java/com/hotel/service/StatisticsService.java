package com.hotel.service;

import com.hotel.entity.*;
import com.hotel.store.DataStore;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    public Statistics getStatistics() {
        Statistics stats = new Statistics();
        
        List<Room> rooms = DataStore.ROOMS.values().stream().collect(Collectors.toList());
        stats.setTotalRooms(rooms.size());
        stats.setOccupiedRooms((int) rooms.stream().filter(r -> r.getStatus() == RoomStatus.OCCUPIED).count());
        stats.setAvailableRooms((int) rooms.stream().filter(r -> r.getStatus() == RoomStatus.AVAILABLE).count());
        stats.setCleaningRooms((int) rooms.stream().filter(r -> r.getStatus() == RoomStatus.CLEANING || r.getStatus() == RoomStatus.PENDING_INSPECTION).count());
        stats.setRepairRooms((int) rooms.stream().filter(r -> r.getStatus() == RoomStatus.OUT_OF_SERVICE).count());

        List<CleanTask> tasks = DataStore.CLEAN_TASKS.values().stream().collect(Collectors.toList());
        stats.setPendingTasks((int) tasks.stream().filter(t -> t.getStatus() == CleanTaskStatus.PENDING || t.getStatus() == CleanTaskStatus.IN_PROGRESS || t.getStatus() == CleanTaskStatus.PENDING_REVIEW).count());
        
        LocalDate today = LocalDate.now();
        stats.setCompletedTasksToday((int) tasks.stream()
                .filter(t -> t.getStatus() == CleanTaskStatus.COMPLETED && t.getCompleteTime() != null)
                .filter(t -> t.getCompleteTime().toLocalDate().equals(today))
                .count());

        long totalCompletedCleanTime = tasks.stream()
                .filter(t -> t.getStatus() == CleanTaskStatus.COMPLETED && t.getCleanDuration() != null)
                .mapToLong(CleanTask::getCleanDuration)
                .sum();
        long completedCount = tasks.stream()
                .filter(t -> t.getStatus() == CleanTaskStatus.COMPLETED && t.getCleanDuration() != null)
                .count();
        stats.setAvgCleanDuration(completedCount > 0 ? (double) totalCompletedCleanTime / completedCount : 0.0);

        int totalRepairDays = rooms.stream().mapToInt(Room::getRepairDays).sum();
        stats.setTotalRepairDays(totalRepairDays);

        int totalCleanCount = rooms.stream().mapToInt(Room::getCleanCount).sum();
        double turnoverRate = stats.getTotalRooms() > 0 ? (double) totalCleanCount / stats.getTotalRooms() : 0.0;
        stats.setTurnoverRate(turnoverRate);

        return stats;
    }

    public List<String> getAlerts() {
        return DataStore.ALERTS;
    }

    public void addAlert(String message) {
        DataStore.ALERTS.add(0, LocalDateTime.now().toString() + " - " + message);
        if (DataStore.ALERTS.size() > 100) {
            DataStore.ALERTS.remove(DataStore.ALERTS.size() - 1);
        }
    }
}

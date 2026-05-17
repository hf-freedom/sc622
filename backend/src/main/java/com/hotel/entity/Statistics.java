package com.hotel.entity;

import lombok.Data;

@Data
public class Statistics {
    private Double turnoverRate;
    private Double avgCleanDuration;
    private Integer totalRepairDays;
    private Integer totalRooms;
    private Integer occupiedRooms;
    private Integer availableRooms;
    private Integer cleaningRooms;
    private Integer repairRooms;
    private Integer pendingTasks;
    private Integer completedTasksToday;
}

package com.hotel.service;

import com.hotel.entity.*;
import com.hotel.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CleanTaskService {

    @Autowired
    private RoomService roomService;

    public CleanTask createCleanTask(Long roomId) {
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }
        CleanTask task = new CleanTask();
        task.setId(DataStore.TASK_ID_GENERATOR.getAndIncrement());
        task.setRoomId(roomId);
        task.setRoomNo(room.getRoomNo());
        task.setStatus(CleanTaskStatus.PENDING);
        task.setCreateTime(LocalDateTime.now());
        task.setPriority(0);
        DataStore.CLEAN_TASKS.put(task.getId(), task);
        room.setStatus(RoomStatus.CLEANING);
        return task;
    }

    public CleanTask claimTask(Long taskId, String cleaner) {
        CleanTask task = DataStore.CLEAN_TASKS.get(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (task.getStatus() != CleanTaskStatus.PENDING && task.getStatus() != CleanTaskStatus.REJECTED) {
            throw new RuntimeException("该任务当前不可领取");
        }
        Room room = roomService.getRoomById(task.getRoomId());
        if (room != null) {
            room.setStatus(RoomStatus.CLEANING);
        }
        task.setStatus(CleanTaskStatus.IN_PROGRESS);
        task.setCleaner(cleaner);
        task.setClaimTime(LocalDateTime.now());
        return task;
    }

    public CleanTask completeTask(Long taskId) {
        CleanTask task = DataStore.CLEAN_TASKS.get(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (task.getStatus() != CleanTaskStatus.IN_PROGRESS) {
            throw new RuntimeException("该任务尚未开始清洁");
        }
        Room room = roomService.getRoomById(task.getRoomId());
        if (room != null) {
            room.setStatus(RoomStatus.PENDING_INSPECTION);
        }
        task.setStatus(CleanTaskStatus.PENDING_REVIEW);
        task.setCompleteTime(LocalDateTime.now());
        if (task.getClaimTime() != null) {
            task.setCleanDuration(Duration.between(task.getClaimTime(), LocalDateTime.now()).toMinutes());
        }
        return task;
    }

    public CleanTask reviewTask(Long taskId, String inspector, boolean passed, String rejectReason) {
        CleanTask task = DataStore.CLEAN_TASKS.get(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (task.getStatus() != CleanTaskStatus.PENDING_REVIEW) {
            throw new RuntimeException("该任务不处于待复查状态");
        }
        task.setInspector(inspector);
        task.setReviewTime(LocalDateTime.now());
        Room room = roomService.getRoomById(task.getRoomId());
        if (passed) {
            task.setStatus(CleanTaskStatus.COMPLETED);
            if (room != null) {
                room.setStatus(RoomStatus.AVAILABLE);
                room.setLastCleanTime(LocalDateTime.now());
                room.setCleanCount(room.getCleanCount() + 1);
            }
        } else {
            task.setStatus(CleanTaskStatus.REJECTED);
            task.setRejectReason(rejectReason);
            task.setRetryCount(task.getRetryCount() + 1);
            if (room != null) {
                room.setStatus(RoomStatus.CLEANING);
            }
        }
        return task;
    }

    public List<CleanTask> getAllTasks() {
        return DataStore.CLEAN_TASKS.values().stream()
                .sorted((a, b) -> {
                    int priorityCompare = b.getPriority().compareTo(a.getPriority());
                    if (priorityCompare != 0) return priorityCompare;
                    return a.getCreateTime().compareTo(b.getCreateTime());
                })
                .collect(Collectors.toList());
    }

    public CleanTask getTaskById(Long id) {
        return DataStore.CLEAN_TASKS.get(id);
    }

    public void updateTaskPriority(Long taskId, int priority) {
        CleanTask task = DataStore.CLEAN_TASKS.get(taskId);
        if (task != null) {
            task.setPriority(priority);
        }
    }
}

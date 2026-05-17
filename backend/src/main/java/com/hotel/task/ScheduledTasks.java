package com.hotel.task;

import com.hotel.entity.Booking;
import com.hotel.entity.CleanTask;
import com.hotel.entity.CleanTaskStatus;
import com.hotel.service.CleanTaskService;
import com.hotel.service.StatisticsService;
import com.hotel.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScheduledTasks {

    @Autowired
    private CleanTaskService cleanTaskService;

    @Autowired
    private StatisticsService statisticsService;

    @Scheduled(fixedRate = 60000)
    public void checkTimeoutCleanTasks() {
        List<CleanTask> tasks = cleanTaskService.getAllTasks();
        for (CleanTask task : tasks) {
            if (task.getStatus() == CleanTaskStatus.PENDING || task.getStatus() == CleanTaskStatus.IN_PROGRESS) {
                long minutes = Duration.between(task.getCreateTime(), LocalDateTime.now()).toMinutes();
                if (minutes > 120) {
                    String message = String.format("房间 %s 清洁任务已超时%d分钟，请主管及时处理！", task.getRoomNo(), minutes);
                    statisticsService.addAlert(message);
                }
            }
        }
    }

    @Scheduled(fixedRate = 30000)
    public void checkUpcomingCheckIns() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = DataStore.BOOKINGS.values().stream()
                .filter(b -> !b.getCheckedIn())
                .collect(Collectors.toList());
        
        for (Booking booking : bookings) {
            if (booking.getCheckInDate() != null) {
                long hours = Duration.between(now, booking.getCheckInDate()).toHours();
                if (hours > 0 && hours <= 2) {
                    List<CleanTask> roomTasks = DataStore.CLEAN_TASKS.values().stream()
                            .filter(t -> t.getRoomId().equals(booking.getRoomId()))
                            .filter(t -> t.getStatus() != CleanTaskStatus.COMPLETED)
                            .collect(Collectors.toList());
                    
                    if (!roomTasks.isEmpty()) {
                        CleanTask task = roomTasks.get(0);
                        if (task.getPriority() < 2) {
                            cleanTaskService.updateTaskPriority(task.getId(), 2);
                            String message = String.format("紧急：房间 %s 将在%d小时后入住，请优先完成清洁！", task.getRoomNo(), hours);
                            statisticsService.addAlert(message);
                        }
                    }
                }
            }
        }
    }
}

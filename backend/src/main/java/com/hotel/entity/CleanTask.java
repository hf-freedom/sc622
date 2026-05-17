package com.hotel.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CleanTask {
    private Long id;
    private Long roomId;
    private String roomNo;
    private CleanTaskStatus status;
    private String cleaner;
    private String inspector;
    private LocalDateTime createTime;
    private LocalDateTime claimTime;
    private LocalDateTime completeTime;
    private LocalDateTime reviewTime;
    private Integer priority = 0;
    private String rejectReason;
    private Long cleanDuration;
    private Integer retryCount = 0;
}

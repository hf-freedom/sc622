package com.hotel.controller;

import com.hotel.entity.CleanTask;
import com.hotel.service.CleanTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clean-tasks")
@CrossOrigin
public class CleanTaskController {

    @Autowired
    private CleanTaskService cleanTaskService;

    @GetMapping
    public List<CleanTask> getAllTasks() {
        return cleanTaskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CleanTask> getTaskById(@PathVariable Long id) {
        CleanTask task = cleanTaskService.getTaskById(id);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody Map<String, Long> body) {
        try {
            Long roomId = body.get("roomId");
            CleanTask result = cleanTaskService.createCleanTask(roomId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/claim")
    public ResponseEntity<?> claimTask(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String cleaner = body.get("cleaner");
            CleanTask result = cleanTaskService.claimTask(id, cleaner);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeTask(@PathVariable Long id) {
        try {
            CleanTask result = cleanTaskService.completeTask(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<?> reviewTask(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            String inspector = (String) body.get("inspector");
            Boolean passed = (Boolean) body.get("passed");
            String rejectReason = (String) body.getOrDefault("rejectReason", "");
            CleanTask result = cleanTaskService.reviewTask(id, inspector, passed, rejectReason);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

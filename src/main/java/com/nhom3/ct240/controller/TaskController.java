package com.nhom3.ct240.controller;

import com.nhom3.ct240.dto.TaskDTO.AssignTaskDTO;
import com.nhom3.ct240.dto.TaskDTO.CreateTaskDTO;
import com.nhom3.ct240.entity.Task;
import com.nhom3.ct240.entity.enums.TaskStatus;
import com.nhom3.ct240.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // CN_17: Tạo công việc mới
    @PostMapping
    public ResponseEntity<?> createTask(
            @Valid @RequestBody CreateTaskDTO createTaskDTO,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Task createdTask = taskService.createTask(createTaskDTO, username);
            return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // CN_18: Chỉnh sửa công việc
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable String id,
            @RequestBody Task taskDetails,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Task updatedTask = taskService.updateTask(id, taskDetails, username);
            return ResponseEntity.ok(updatedTask);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // CN_19: Xóa công việc
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable String id,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            taskService.deleteTask(id, username);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // CN_21: Xem danh sách công việc
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) TaskStatus status,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            if (projectId != null) {
                return ResponseEntity.ok(taskService.getTasksByProjectAndStatus(projectId, status, username));
            }
            return ResponseEntity.ok(taskService.findAllTasks());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // CN_22: Xem chi tiết công việc
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(
            @PathVariable String id,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Optional<Task> task = taskService.findTaskById(id, username);
            return task.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // CN_20: Cập nhật trạng thái công việc
    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateTaskStatus(
            @PathVariable String id,
            @RequestParam TaskStatus status,
            @RequestParam(required = false) String cancelReason,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Task updatedTask = taskService.updateTaskStatus(id, status, cancelReason, username);
            return ResponseEntity.ok(updatedTask);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // CN_23: Phân công người thực hiện công việc
    @PatchMapping("/{id}/assign")
    public ResponseEntity<Task> assignTask(
            @PathVariable String id,
            @RequestBody AssignTaskDTO assignTaskDTO,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Task updatedTask = taskService.assignTask(id, assignTaskDTO.getAssigneeId(), username);
            return ResponseEntity.ok(updatedTask);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

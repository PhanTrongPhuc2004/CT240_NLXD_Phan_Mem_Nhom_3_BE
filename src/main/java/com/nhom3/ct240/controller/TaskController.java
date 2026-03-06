package com.nhom3.ct240.controller;

import com.nhom3.ct240.dto.AssignTaskDTO;
import com.nhom3.ct240.entity.Task;
import com.nhom3.ct240.entity.enums.TaskStatus;
import com.nhom3.ct240.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*") // Cho phép frontend gọi API từ domain khác (trong quá trình phát triển)
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // CN_17: Tạo công việc mới
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task createdTask = taskService.createTask(task);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    // CN_18: Chỉnh sửa công việc
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable String id, @RequestBody Task taskDetails) {
        try {
            Task updatedTask = taskService.updateTask(id, taskDetails);
            return ResponseEntity.ok(updatedTask);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // CN_19: Xóa công việc
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    // CN_21: Xem danh sách công việc (có thể lọc theo projectId và status)
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) TaskStatus status) {
        
        if (projectId != null) {
            return ResponseEntity.ok(taskService.getTasksByProjectAndStatus(projectId, status));
        }
        return ResponseEntity.ok(taskService.findAllTasks());
    }

    // CN_22: Xem chi tiết công việc
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable String id) {
        Optional<Task> task = taskService.findTaskById(id);
        return task.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // CN_20: Cập nhật trạng thái công việc
    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateTaskStatus(
            @PathVariable String id,
            @RequestParam TaskStatus status,
            @RequestParam(required = false) String cancelReason) {
        try {
            Task updatedTask = taskService.updateTaskStatus(id, status, cancelReason);
            return ResponseEntity.ok(updatedTask);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // CN_23: Phân công người thực hiện công việc
    @PatchMapping("/{id}/assign")
    public ResponseEntity<Task> assignTask(
            @PathVariable String id,
            @RequestBody AssignTaskDTO assignTaskDTO) {
        try {
            Task updatedTask = taskService.assignTask(id, assignTaskDTO.getAssigneeId());
            return ResponseEntity.ok(updatedTask);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

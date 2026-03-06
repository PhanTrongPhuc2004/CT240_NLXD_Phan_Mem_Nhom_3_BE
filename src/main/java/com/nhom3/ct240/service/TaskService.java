package com.nhom3.ct240.service;

import com.nhom3.ct240.entity.Task;
import com.nhom3.ct240.entity.enums.TaskStatus;
import com.nhom3.ct240.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // CN_21: Xem danh sách công việc (tất cả)
    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }

    // CN_22: Xem chi tiết công việc
    public Optional<Task> findTaskById(String id) {
        return taskRepository.findById(id);
    }

    // CN_17: Tạo công việc mới
    public Task createTask(Task task) {
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        // Thêm logic kiểm tra quyền hạn hoặc nghiệp vụ khác ở đây nếu cần
        return taskRepository.save(task);
    }

    // CN_18: Chỉnh sửa công việc
    public Task updateTask(String id, Task taskDetails) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        existingTask.setTitle(taskDetails.getTitle());
        existingTask.setDescription(taskDetails.getDescription());
        existingTask.setDeadline(taskDetails.getDeadline());
        existingTask.setPriority(taskDetails.getPriority());
        // Không cho phép cập nhật status và assigneeId trực tiếp qua updateTask này
        // Vì có các phương thức riêng cho việc đó để xử lý logic nghiệp vụ cụ thể

        existingTask.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(existingTask);
    }

    // CN_19: Xóa công việc
    public void deleteTask(String id) {
        // Thêm logic kiểm tra quyền hạn trước khi xóa
        taskRepository.deleteById(id);
    }

    // CN_21: Xem danh sách công việc theo Project
    public List<Task> findTasksByProjectId(String projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    // CN_20: Cập nhật trạng thái công việc
    public Task updateTaskStatus(String taskId, TaskStatus newStatus, String cancelReason) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        // Logic chuyển trạng thái (có thể phức tạp hơn với Strategy Pattern sau này)
        existingTask.setStatus(newStatus);
        if (newStatus == TaskStatus.CANCELLED) {
            existingTask.setCancelReason(cancelReason);
        } else {
            existingTask.setCancelReason(null); // Xóa lý do hủy nếu không phải trạng thái hủy
        }
        existingTask.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(existingTask);
    }

    // CN_23: Phân công người thực hiện công việc
    public Task assignTask(String taskId, String assigneeId) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        existingTask.setAssigneeId(assigneeId);
        existingTask.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(existingTask);
    }

    // Phương thức hỗ trợ lọc công việc theo trạng thái (cho CN_21)
    public List<Task> getTasksByProjectAndStatus(String projectId, TaskStatus filterStatus) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        if (filterStatus != null) {
            return tasks.stream()
                    .filter(t -> t.getStatus() == filterStatus)
                    .toList();
        }
        return tasks;
    }
}

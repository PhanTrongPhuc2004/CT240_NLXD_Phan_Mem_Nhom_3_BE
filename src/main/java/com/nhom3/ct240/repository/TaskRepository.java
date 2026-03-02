package com.nhom3.ct240.repository;

import com.nhom3.ct240.entity.Task;
import com.nhom3.ct240.entity.enums.TaskStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository cho entity Task
 * - CN_17: Tạo → save()
 * - CN_18: Chỉnh sửa → save()
 * - CN_19: Xóa → deleteById()
 * - CN_20: Cập nhật trạng thái → save()
 * - CN_21: Xem danh sách → findByProjectId(), findByAssigneeId(),...
 * - CN_22: Xem chi tiết → findById()
 * - CN_23: Phân công → save()
 */
@Repository
public interface TaskRepository extends MongoRepository<Task, String> {

    // Danh sách task theo dự án (CN_21)
    List<Task> findByProjectId(String projectId);

    // Task được giao cho user (CN_21)
    List<Task> findByAssigneeId(String assigneeId);

    // Task quá hạn (CN_29)
    List<Task> findByDeadlineBeforeAndStatusNot(LocalDateTime now, TaskStatus status);

    // Task theo trạng thái (CN_21, CN_29)
    List<Task> findByProjectIdAndStatus(String projectId, TaskStatus status);

    // TODO: thêm query cho priority, search title,...
}
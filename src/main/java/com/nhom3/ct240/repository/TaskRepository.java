package com.nhom3.ct240.repository;

import com.nhom3.ct240.entity.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {

    /**
     * Tìm tất cả các công việc thuộc về một dự án cụ thể.
     * @param projectId ID của dự án
     * @return Danh sách các công việc
     */
    List<Task> findByProjectId(String projectId);

    /**
     * Tìm tất cả các công việc được giao cho một người dùng cụ thể.
     * @param assigneeId ID của người được giao
     * @return Danh sách các công việc
     */
    List<Task> findByAssigneeId(String assigneeId);
}

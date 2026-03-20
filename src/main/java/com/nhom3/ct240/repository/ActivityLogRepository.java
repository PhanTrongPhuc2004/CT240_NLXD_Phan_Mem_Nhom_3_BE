package com.nhom3.ct240.repository;

import com.nhom3.ct240.entity.ActivityLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends MongoRepository<ActivityLog, String> {
    
    // Lấy danh sách log theo projectId, sắp xếp tự động qua Pageable
    List<ActivityLog> findByProjectIdOrderByCreatedAtDesc(String projectId, Pageable pageable);
}

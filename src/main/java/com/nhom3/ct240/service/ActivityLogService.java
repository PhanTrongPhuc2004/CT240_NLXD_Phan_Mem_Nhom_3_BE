package com.nhom3.ct240.service;

import com.nhom3.ct240.entity.ActivityLog;
import com.nhom3.ct240.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Autowired
    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public void logActivity(String projectId, String userId, String action, String details, String icon, String color) {
        ActivityLog log = new ActivityLog();
        log.setProjectId(projectId);
        log.setUserId(userId);
        log.setAction(action);
        log.setDetails(details);
        log.setIcon(icon);
        log.setColor(color);
        
        activityLogRepository.save(log);
    }

    public List<ActivityLog> getRecentActivitiesByProject(String projectId, int limit) {
        // Lấy danh sách giới hạn số lượng (limit), đã sắp xếp giảm dần trong Repository
        return activityLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId, PageRequest.of(0, limit));
    }
}

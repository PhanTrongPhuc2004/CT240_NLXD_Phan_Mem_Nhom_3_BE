package com.nhom3.ct240.service;

import com.nhom3.ct240.dto.ProjectDTO;
import com.nhom3.ct240.entity.Project;

import java.util.List;

/**
 * Service cho quản lý dự án
 * - CN_11: Tạo dự án mới
 * - CN_12: Chỉnh sửa thông tin dự án
 * - CN_13: Xóa dự án
 * - CN_14: Xem chi tiết dự án
 * - CN_15: Phân quyền quản lý dự án
 * - CN_16: Tham gia/rời dự án
 */
public interface ProjectService {

    Project createProject(ProjectDTO dto, String currentUserId);

    Project updateProject(String projectId, ProjectDTO dto, String currentUserId);

    void deleteProject(String projectId, String currentUserId);

    Project getProjectDetail(String projectId, String currentUserId);

    // Lấy danh sách dự án của người dùng
    List<Project> getAllProjects(String currentUserId);

    // CN_15: Phân quyền quản lý dự án
    Project assignManager(String projectId, String userIdToAssign, String currentUserId);
    Project removeManager(String projectId, String userIdToRemove, String currentUserId);
    Project assignMember(String projectId, String userIdToAssign, String currentUserId);
    Project removeMember(String projectId, String userIdToRemove, String currentUserId);

    // CN_16: Tham gia/rời dự án
    void requestToJoinProject(String projectId, String currentUserId);
    Project approveJoinRequest(String projectId, String userIdToApprove, String currentUserId);
    Project rejectJoinRequest(String projectId, String userIdToReject, String currentUserId);
    void leaveProject(String projectId, String currentUserId);
}
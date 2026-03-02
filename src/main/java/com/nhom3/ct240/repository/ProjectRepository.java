package com.nhom3.ct240.repository;

import com.nhom3.ct240.entity.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho entity Project
 * - CN_11: Tạo dự án mới → save()
 * - CN_12: Chỉnh sửa → save()
 * - CN_13: Xóa → deleteById()
 * - CN_14: Xem chi tiết → findById()
 * - CN_15, CN_16: Quản lý thành viên → custom query nếu cần
 */
@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {

    // Tìm dự án theo owner (CN_11, CN_13)
    List<Project> findByOwnerId(String ownerId);

    // Tìm dự án mà user tham gia (CN_14, CN_16)
    List<Project> findByMemberIdsContaining(String userId);

    // Tìm dự án theo status (CN_29, CN_30)
    List<Project> findByStatus(String status);

    // TODO: thêm query tùy chỉnh nếu cần lọc theo tên, ngày tạo,...
}
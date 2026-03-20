package com.nhom3.ct240.repository;

import com.nhom3.ct240.entity.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {

    // Tìm dự án theo owner
    List<Project> findByOwnerId(String ownerId);

    // Tìm dự án mà user tham gia
    List<Project> findByMemberIdsContaining(String userId);

    // Tìm dự án theo status
    List<Project> findByStatus(String status);

    // Dùng Query Method của Spring Data thay cho @Query JSON để tránh lỗi cú pháp
    List<Project> findByVisibilityOrOwnerIdOrMemberIdsContainingOrManagerIdsContaining(
            String visibility, 
            String ownerId, 
            String memberId, 
            String managerId
    );
}

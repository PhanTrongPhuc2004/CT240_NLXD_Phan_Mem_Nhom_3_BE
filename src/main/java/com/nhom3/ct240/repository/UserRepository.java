package com.nhom3.ct240.repository;

import com.nhom3.ct240.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByFullName(String fullName);

    void deleteByUsername(String username);

    // Sử dụng Query Method thay vì @Query JSON để tránh lỗi cú pháp
    List<User> findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String fullName, String email);

    // Tìm danh sách user theo danh sách ID
    List<User> findByIdIn(List<String> ids);
}
//src/main/java/com/nhom3/ct240/repository/UserRepository.java
package com.nhom3.ct240.repository;

import com.nhom3.ct240.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho entity User, sử dụng Spring Data MongoDB
 * Cung cấp các phương thức tìm kiếm và kiểm tra tồn tại
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Tìm user theo username (dùng cho xác thực đăng nhập)
     */
    Optional<User> findByUsername(String username);

    /**
     * Tìm user theo email (dùng cho kiểm tra trùng lặp khi đăng ký hoặc reset password)
     */
    Optional<User> findByEmail(String email);

    /**
     * Kiểm tra username đã tồn tại chưa (dùng trong register)
     */
    boolean existsByUsername(String username);

    /**
     * Kiểm tra email đã tồn tại chưa (dùng trong register)
     */
    boolean existsByEmail(String email);

    /**
     * Tìm user theo fullName (nếu cần tìm kiếm theo tên hiển thị)
     */
    Optional<User> findByFullName(String fullName);

    /**
     * Tìm tất cả user có role cụ thể (dành cho Admin dashboard hoặc lọc)
     */
    // List<User> findByRole(Role role);  // Uncomment nếu cần sau này

    /**
     * Tìm tất cả user đang active/inactive
     */
//     List<User> findByActive(boolean active);  // Uncomment nếu cần

    /**
     * Xóa user theo username (nếu cần soft-delete hoặc admin xóa)
     */
    void deleteByUsername(String username);
}
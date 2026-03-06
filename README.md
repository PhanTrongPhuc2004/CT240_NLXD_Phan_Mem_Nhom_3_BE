# Ứng dụng Quản lý Công việc Nhóm - Backend

**Tên dự án**: CT240_NLX_D_Phan_Mem_Nhom_3  
**Môn học**: Nguyên lý Xây dựng Phần mềm (CT240)  
**Nhóm thực hiện**: Nhóm 3  
**Thành viên**:
- Phan Trọng Phúc - B2203525 (Backend Lead)
- Lê Đình Duy - B2203494
- Nguyễn Khánh Duy - B2203496
- Nguyễn Hoàng Vinh - B2303856
- Nguyễn Thanh Yến Khoa - B2303823 
- Nguyễn Kim Yến - B2303860
 
**Giảng viên hướng dẫn**: TS. Trương Minh Thái  
**Học kỳ**: II, Năm học 2025-2026  
**Ngày hoàn thành**: Tháng 01/2026

## Mô tả dự án

Backend của ứng dụng quản lý công việc nhóm là một hệ thống **client-server** được xây dựng bằng **Spring Boot (Java)** và **MongoDB**, hỗ trợ các chức năng:
- Quản lý người dùng & xác thực (đăng ký, đăng nhập JWT, phân vai trò Admin/Manager/Member)
- Quản lý dự án (tạo, sửa, xóa, phân quyền, tham gia/rời dự án)
- Quản lý công việc (task) (CRUD task, cập nhật trạng thái, gán người thực hiện, lọc/sắp xếp)
- Bình luận & trao đổi trong task (thêm, xem, sửa/xóa comment)
- Thông báo real-time (thay đổi trạng thái task, deadline gần đến, gán task)
- Thống kê & báo cáo (tiến độ dự án, báo cáo chi tiết theo user/thời gian, export CSV/PDF)

Ứng dụng được thiết kế theo **kiến trúc multi-layered** (Controller → Service → Repository), áp dụng các **design patterns**:
- Observer (thông báo real-time qua WebSocket)
- Strategy (cập nhật trạng thái task)
- Factory (tạo object động)
- Template Method (quy trình xử lý task)

Hỗ trợ **concurrency** (Spring @Async, @Scheduled), **unit test** (JUnit + Mockito), và **mở rộng** (plugin system với Java Reflection).

Frontend (Vue.js) kết nối qua **REST API** và **WebSocket** (STOMP).

## Công nghệ sử dụng

- **Ngôn ngữ**: Java 17/21
- **Framework**: Spring Boot 4.0.3
    - Spring Web (REST API)
    - Spring Data MongoDB
    - Spring Security + JWT
    - Spring WebSocket + STOMP (real-time)
    - Spring Scheduler & @Async (concurrency)
- **Database**: MongoDB (local hoặc cloud)
- **Xác thực**: JWT (JSON Web Token)
- **Build tool**: Maven
- **Test**: JUnit 5, Mockito
- **IDE**: Eclipse / IntelliJ IDEA

## Cấu trúc thư mục (Backend)

```text
src/main/java/com/nhom3/ct240/
├── config/                    # Cấu hình chung (Security, WebSocket, Jackson,...)
│   ├── SecurityConfig.java    
│   └── WebSocketConfig.java   # (skeleton mới)
├── controller/                # Các REST API controller
│   ├── AuthController.java    
│   ├── UserController.java
│   ├── ProjectController.java
│   ├── TaskController.java
│   ├── CommentController.java
│   ├── NotificationController.java
│   └── ReportController.java
├── dto/                       # DTOs dùng chung
│   ├── AuthResponse.java      
│   ├── UserResponseDTO.java   
│   ├── ProjectDTO.java        
│   ├── TaskDTO.java           
│   ├── CommentDTO.java        
│   ├── NotificationDTO.java   
│   ├── RegisterRequest.java   
│   └── LoginRequest.java      
├── entity/                    # Các entity MongoDB 
│   ├── User.java              
│   ├── Project.java           
│   ├── Task.java              
│   ├── Comment.java           
│   ├── Notification.java      
│   ├── TaskHistory.java       
│   └── enums/                 # Các enum
│       ├── Role.java
│       ├── ProjectStatus.java
│       ├── TaskStatus.java
│       ├── Priority.java
│       └── NotificationType.java
├── repository/                # Repository interfaces
│   ├── UserRepository.java    
│   ├── ProjectRepository.java
│   ├── TaskRepository.java
│   ├── CommentRepository.java
│   └── NotificationRepository.java
├── service/                   # Business logic (interface + impl)
│   ├── UserService.java       
│   ├── UserServiceImpl.java   
│   ├── ProjectService.java
│   ├── TaskService.java
│   ├── CommentService.java
│   ├── NotificationService.java
│   └── ReportService.java
├── security/                  # Bảo mật
│   └── JwtAuthenticationFilter.java  
├── util/                      # Các utility chung
│   └── JwtUtil.java           
├── exception/                 # Xử lý lỗi chung
│   └── GlobalExceptionHandler.java
├── plugin/                    # Nếu áp dụng plugin architecture (theo yêu cầu đồ án)
│   ├── Plugin.java            # Interface plugin
│   ├── PluginLoader.java      # Load plugin bằng reflection
│   └── HostContext.java       # Context cho plugin
└── Ct240Application.java      # Main class 
```

## Yêu cầu cài đặt & Chạy local

### Yêu cầu hệ thống
- Java JDK 17+ (tải tại https://adoptium.net/)
- Maven 3.8+ (hoặc dùng wrapper mvnw)
- MongoDB Community Server (local: cài tại https://www.mongodb.com/try/download/community)
- IDE: IntelliJ IDEA (với plugin Maven & Spring Tools)

### Các bước chạy
1. **Clone repo** (nếu chưa có):
   git clone https://github.com/PhanTrongPhuc2004/CT240_NLXD_Phan_Mem_Nhom_3_BE

2. **Cài đặt dependencies**:
   mvn clean install

3. **Khởi động MongoDB**:
   Local cài đặt: Mở terminal/command prompt, chạy:
   ```text   
   mongod
   ```
4. **Chạy ứng dụng**:
   Dùng Maven:
   ```text
   mvn spring-boot:run
   ```
   Hoặc trong IntelliJ:
   ```text
   Run Ct240Application
   ```

5. **Kiểm tra ứng dụng đã chạy**
   Test API bằng Postman:
   ```text
   POST http://localhost:8080/api/auth/register
   POST http://localhost:8080/api/auth/login
   ```
6. **Dừng ứng dụng:**: Nhấn Ctrl + C trong terminal, hgit add README.mdoặc trong IntelliJ: Nhấn nút đỏ (Terminate).

Chúc bạn chạy thành công! Nếu gặp lỗi, kiểm tra console log và báo lại nhóm nhé.

© 2026 Nhóm 3 - Ứng dụng Quản lý Công việc Nhóm
Đại học Cần Thơ - Trường Công nghệ Thông tin và Truyền thông
  
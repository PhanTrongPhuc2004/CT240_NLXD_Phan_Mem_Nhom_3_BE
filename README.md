# Ứng dụng Quản lý Công việc Nhóm - Backend

**Tên dự án**: CT240_NLXD_Phan_Mem_Nhom_3  
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
**Ngày hoàn thành**: Tháng 03/2026

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
- **Framework**: Spring Boot 3.x
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

## Cấu trúc thư mục

```text
src/main/java/com/nhom3/ct240/
├── config/                    # Cấu hình chung hệ thống
│   ├── SecurityConfig.java    # Cấu hình bảo mật
│   └── WebSocketConfig.java   # Cấu hình WebSocket real-time
├── controller/                # REST API Controllers (Tiếp nhận request)
│   ├── AuthController.java    
│   ├── UserController.java
│   ├── ProjectController.java
│   ├── TaskController.java
│   └── ...
├── dto/                       # Data Transfer Objects (Dữ liệu trao đổi)
│   ├── auth/                  # DTO cho xác thực (Login, Register...)
│   ├── task/                  # DTO cho Task (CreateTaskDTO...)
│   └── ...
├── entity/                    # Các Entity lưu trữ trong MongoDB
│   ├── User.java              
│   ├── Project.java           
│   ├── Task.java              
│   └── enums/                 # Các Enum (Role, TaskStatus,...)
├── event/                     # [OBSERVER PATTERN] Các sự kiện Domain
│   ├── BaseEvent.java         
│   ├── TaskCreatedEvent.java  
│   ├── TaskAssignedEvent.java
│   ├── TaskStatusChangedEvent.java
│   └── ...
├── factory/                   # [FACTORY PATTERN] Khởi tạo đối tượng phức tạp
│   └── TaskFactory.java       # Factory chuyên biệt để tạo Task
├── listener/                  # [OBSERVER PATTERN] Xử lý sự kiện bất đồng bộ
│   └── TaskEventListener.java # Lắng nghe sự kiện để Gửi thông báo & Ghi log
├── plugin/                    # [PLUGIN ARCHITECTURE] Hệ thống mở rộng
│   ├── Plugin.java            # Interface chung cho mọi plugin
│   ├── PluginLoader.java      # Load file .jar bên ngoài bằng Java Reflection
│   └── HostContext.java       # Cung cấp ngữ cảnh ứng dụng cho plugin
├── repository/                # Data Access Layer (Giao tiếp MongoDB)
│   ├── UserRepository.java    
│   ├── TaskRepository.java
│   └── ...
├── security/                  # Authentication & Authorization
│   ├── JwtAuthenticationFilter.java  
│   └── ...
├── service/                   # Business Logic Layer
│   ├── TaskService.java       # Xử lý nghiệp vụ chính của Task
│   ├── NotificationService.java
│   ├── ActivityLogService.java
│   └── ...
├── strategy/                  # [STRATEGY & TEMPLATE METHOD PATTERN] Xử lý trạng thái
│   ├── TaskStatusUpdateStrategy.java          # Interface Strategy
│   ├── AbstractTaskStatusUpdateStrategy.java  # [TEMPLATE METHOD] Class cha định nghĩa quy trình
│   ├── TaskStatusStrategyFactory.java         # Factory chọn chiến lược phù hợp
│   ├── ToDoStatusUpdateStrategy.java          # Chiến lược: Chuyển sang To Do
│   ├── InProgressStatusUpdateStrategy.java    # Chiến lược: Chuyển sang In Progress
│   ├── DoneStatusUpdateStrategy.java          # Chiến lược: Chuyển sang Done
│   └── CancelledStatusUpdateStrategy.java     # Chiến lược: Chuyển sang Cancelled (có lý do)
└── Ct240Application.java      # Main Application Class
```

## Các Design Pattern đã áp dụng

Hệ thống áp dụng 5 Design Pattern chính theo yêu cầu đồ án:

### 1. Observer Pattern (Mẫu Quan sát viên)
- **Vị trí**: Package `event/` và `listener/`.
- **Mục đích**: Tách rời logic nghiệp vụ chính (Tạo/Sửa Task) khỏi các tác vụ phụ (Gửi thông báo, Ghi log).
- **Cách hoạt động**: Khi `TaskService` thực hiện xong hành động, nó bắn ra một `Event`. `TaskEventListener` sẽ bắt sự kiện này để xử lý tiếp mà không làm chậm luồng chính.

### 2. Strategy Pattern (Mẫu Chiến lược)
- **Vị trí**: Package `strategy/`.
- **Mục đích**: Xử lý logic cập nhật trạng thái Task (To Do -> Done, Cancelled...) một cách linh hoạt mà không dùng `if-else` phức tạp.
- **Cách hoạt động**: `TaskStatusStrategyFactory` sẽ chọn một `Strategy` tương ứng với trạng thái đích để thực thi.

### 3. Template Method Pattern (Mẫu Phương thức khuôn mẫu)
- **Vị trí**: Class `strategy/AbstractTaskStatusUpdateStrategy.java`.
- **Mục đích**: Định nghĩa khung sườn (quy trình chuẩn) cho việc cập nhật trạng thái: `Validate` -> `Change Status` -> `Update Timestamp` -> `Post Process`. Các lớp con chỉ cài đặt chi tiết từng bước.

### 4. Factory Pattern (Mẫu Nhà máy)
- **Vị trí**: Class `factory/TaskFactory.java`.
- **Mục đích**: Đóng gói logic khởi tạo đối tượng `Task`, đảm bảo các thuộc tính mặc định luôn được thiết lập đúng.

### 5. Plugin Architecture (Kiến trúc Plugin)
- **Vị trí**: Package `plugin/`.
- **Mục đích**: Cho phép mở rộng tính năng của hệ thống mà không cần sửa code nguồn (Open/Closed Principle).
- **Cách hoạt động**: `PluginLoader` sử dụng **Java Reflection** để quét và tải các file `.jar` từ thư mục bên ngoài (dynamic loading).

## Yêu cầu cài đặt & Chạy local

### Yêu cầu hệ thống
- Java JDK 17+ (tải tại https://adoptium.net/)
- Maven 3.8+ (hoặc dùng wrapper mvnw)
- MongoDB Community Server (local: cài tại https://www.mongodb.com/try/download/community)
- IDE: IntelliJ IDEA (với plugin Maven & Spring Tools)

### Các bước chạy
1. **Clone repo** (nếu chưa có):
   ```bash
   git clone https://github.com/PhanTrongPhuc2004/CT240_NLXD_Phan_Mem_Nhom_3_BE
   ```

2. **Cài đặt dependencies**:
   ```bash
   mvn clean install
   ```

3. **Khởi động MongoDB**:
   Local cài đặt: Mở terminal/command prompt, chạy:
   ```bash   
   mongod
   ```

4. **Chạy ứng dụng**:
   Dùng Maven:
   ```bash
   mvn spring-boot:run
   ```
   Hoặc trong IntelliJ:
   - Mở class `Ct240Application.java`
   - Nhấn nút Play (Run)

5. **Kiểm tra ứng dụng đã chạy**
   Test API bằng Postman:
   ```text
   POST http://localhost:8080/api/auth/register
   POST http://localhost:8080/api/auth/login
   ```

6. **Dừng ứng dụng**: 
   - Nhấn Ctrl + C trong terminal
   - Hoặc trong IntelliJ: Nhấn nút đỏ (Stop).

### Hướng dẫn chạy Plugin
1. Tạo thư mục `plugins` tại thư mục gốc của dự án.
2. Build các module mở rộng thành file `.jar`.
3. Copy file `.jar` vào thư mục `plugins`.
4. Khởi động lại Server, hệ thống sẽ tự động nhận diện và kích hoạt plugin.

---
© 2026 Nhóm 3 - Ứng dụng Quản lý Công việc Nhóm  
Đại học Cần Thơ - Trường Công nghệ Thông tin và Truyền thông

package com.nhom3.ct240.plugin;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * HostContext - Cung cấp context cho các plugin
 * - Cho phép plugin truy cập Spring beans, services, repositories
 * - Yêu cầu đồ án: hỗ trợ plugin architecture (Reflection + HostContext)
 * - Plugin có thể gọi các service chung như UserService, ProjectService, NotificationService,...
 *
 * Cách dùng trong plugin:
 *   HostContext.getBean(UserService.class).getCurrentUser();
 */
@Component
public class HostContext {

    private static ApplicationContext applicationContext;

    /**
     * Inject ApplicationContext từ Spring khi khởi động
     * Gọi trong @PostConstruct hoặc ApplicationRunner
     */
    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    /**
     * Lấy bean từ Spring context
     * @param clazz Class của bean cần lấy
     * @return Instance của bean
     */
    public static <T> T getBean(Class<T> clazz) {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext chưa được khởi tạo");
        }
        return applicationContext.getBean(clazz);
    }

    /**
     * Kiểm tra xem plugin có quyền truy cập service nào đó không
     * (có thể mở rộng sau với security check)
     */
    public static boolean hasAccess(String serviceName) {
        // TODO: implement quyền truy cập cho plugin nếu cần
        return true;
    }

    // TODO: thêm method hỗ trợ plugin khác nếu cần (ví dụ: publish event, send notification từ plugin)
}
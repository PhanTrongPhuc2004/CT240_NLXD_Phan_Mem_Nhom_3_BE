package com.nhom3.ct240.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service xử lý thống kê và báo cáo
 * - CN_29: Xem thống kê dự án (tiến độ %, biểu đồ trạng thái, task trễ hạn, workload theo thành viên)
 * - CN_30: Xem báo cáo chi tiết (hiệu suất thành viên, phân bổ theo thời gian, lọc date range)
 * - CN_31: Xuất báo cáo (PDF, CSV)
 *
 * Service này sẽ được gọi từ ReportController
 * Dữ liệu lấy từ ProjectService, TaskService, UserService
 */
public interface ReportService {

    /**
     * CN_29: Thống kê tổng quan dự án
     * @param projectId ID dự án
     * @param currentUserId Người xem (phải có quyền)
     * @return Map chứa: tiến độ %, số task theo trạng thái, task trễ hạn, workload thành viên
     */
    Map<String, Object> getProjectStatistics(String projectId, String currentUserId);

    /**
     * CN_30: Báo cáo hiệu suất thành viên trong dự án
     * @param projectId ID dự án
     * @param startDate Ngày bắt đầu lọc (tùy chọn)
     * @param endDate Ngày kết thúc lọc (tùy chọn)
     * @return List<UserResponseDTO> kèm metrics: số task giao, hoàn thành đúng hạn, trễ hạn, thời gian TB hoàn thành
     */
    List<Map<String, Object>> getMemberPerformanceReport(String projectId, LocalDate startDate, LocalDate endDate);

    /**
     * CN_30: Báo cáo phân bổ công việc theo thời gian
     * @param projectId ID dự án
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Map<date, số task tạo mới + hoàn thành>
     */
    Map<LocalDate, Map<String, Integer>> getTaskDistributionByTime(String projectId, LocalDate startDate, LocalDate endDate);

    /**
     * CN_31: Xuất báo cáo thống kê dự án ra CSV
     * @param projectId ID dự án
     * @param type Loại báo cáo: "statistics", "member_performance", "time_distribution"
     * @return byte[] nội dung file CSV
     */
    byte[] exportProjectReportToCsv(String projectId, String type);

    /**
     * CN_31: Xuất báo cáo thống kê dự án ra PDF (sử dụng iText hoặc Flying Saucer sau này)
     * @param projectId ID dự án
     * @param type Loại báo cáo
     * @return byte[] nội dung file PDF
     */
    byte[] exportProjectReportToPdf(String projectId, String type);

    // TODO: thêm method cho lọc theo user, export theo date range, cache report nếu dữ liệu lớn
}
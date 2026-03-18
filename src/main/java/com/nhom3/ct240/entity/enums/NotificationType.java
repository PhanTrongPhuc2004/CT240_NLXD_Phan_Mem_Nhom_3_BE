package com.nhom3.ct240.entity.enums;

public enum NotificationType {
    TASK_ASSIGNED,           // Công việc được giao
    TASK_DEADLINE_SOON,      // Sắp đến hạn
    TASK_OVERDUE,            // Quá hạn
    TASK_STATUS_CHANGED,     // Trạng thái thay đổi
    COMMENT_ADDED,           // Bình luận mới
    MENTIONED_IN_COMMENT,    // Tag @bạn
    PROJECT_INVITE,          // Mời tham gia
    PROJECT_JOIN_APPROVED,   // Chấp nhận yêu cầu
    PROJECT_DELETED,         // Dự án bị xóa
    PROJECT_JOIN_REJECTED,
    MEMBER_REQUEST_JOIN,     // Manager nhận: Yêu cầu tham gia
    MEMBER_LEFT_PROJECT,     // Manager nhận: Thành viên rời dự án
    PROJECT_PROGRESS_CHANGED // Manager nhận: Tiến độ thay đổi

}
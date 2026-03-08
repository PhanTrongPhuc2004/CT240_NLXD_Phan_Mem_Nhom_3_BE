package com.nhom3.ct240.dto.user;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String fullName;
    private String avatarUrl;
    private Boolean active;   // ← THÊM DÒNG NÀY (cho phép Admin bật/tắt trạng thái)
}
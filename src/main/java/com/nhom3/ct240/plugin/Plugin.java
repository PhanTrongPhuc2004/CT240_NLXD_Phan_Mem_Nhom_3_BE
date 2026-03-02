package com.nhom3.ct240.plugin;

/**
 * Interface chung cho các plugin (mở rộng hệ thống)
 * - Yêu cầu đồ án: hỗ trợ plugin architecture bằng reflection
 */
public interface Plugin {

    String getName();

    String getDescription();

    void initialize();

    void execute(); // hoặc các method tùy chức năng plugin
}
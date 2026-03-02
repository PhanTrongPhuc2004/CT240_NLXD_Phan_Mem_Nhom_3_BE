package com.nhom3.ct240.plugin;

import java.util.List;

/**
 * Plugin Loader sử dụng Java Reflection
 * - Quét package/plugin, load class implement Plugin
 * - Dùng cho tính mở rộng (theo yêu cầu đồ án)
 */
public class PluginLoader {

    public List<Plugin> loadPlugins() {
        // TODO: implement reflection scan (ví dụ dùng Reflections library hoặc ClassLoader)
        return List.of();
    }

    public void executeAll() {
        // TODO: gọi initialize() và execute() cho từng plugin
    }
}
package com.nhom3.ct240.plugin;

import lombok.Getter;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

/**
 * Plugin Loader sử dụng Java Reflection để tải các plugin từ file .jar bên ngoài
 */
@Getter
@Component
public class PluginLoader {

    private final List<Plugin> loadedPlugins = new ArrayList<>();

    /**
     * Tải tất cả plugin từ thư mục chỉ định
     * @param pluginDir Đường dẫn thư mục chứa file .jar
     * @return Danh sách plugin đã tải
     */
    public List<Plugin> loadPlugins(String pluginDir) {
        loadedPlugins.clear();
        File dir = new File(pluginDir);
        
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("Plugin directory not found: " + pluginDir);
            return loadedPlugins;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".jar"));
        if (files == null) return loadedPlugins;

        for (File file : files) {
            try {
                // Tạo URLClassLoader để load classes từ file jar
                URL[] urls = { new URL("jar:file:" + file.getAbsolutePath() + "!/") };
                
                // Sử dụng try-with-resources để tự động đóng ClassLoader
                try (URLClassLoader cl = URLClassLoader.newInstance(urls, getClass().getClassLoader());
                     JarFile jarFile = new JarFile(file)) {
                     
                    jarFile.stream().forEach(jarEntry -> {
                        if (jarEntry.getName().endsWith(".class")) {
                            String className = jarEntry.getName()
                                    .replace("/", ".")
                                    .replace(".class", "");
                            try {
                                Class<?> cls = cl.loadClass(className);
                                // Kiểm tra xem class có implement Plugin interface không
                                if (Plugin.class.isAssignableFrom(cls) && !cls.isInterface()) {
                                    // Tạo instance mới
                                    Plugin plugin = (Plugin) cls.getDeclaredConstructor().newInstance();
                                    plugin.initialize();
                                    loadedPlugins.add(plugin);
                                    System.out.println("Loaded plugin successfully: " + plugin.getName());
                                }
                            } catch (Exception e) {
                                // Bỏ qua lỗi khi load class không phải plugin hoặc lỗi dependency
                            }
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to load plugin from file: " + file.getName());
            }
        }
        return loadedPlugins;
    }

    public void executeAll() {
        for (Plugin plugin : loadedPlugins) {
            try {
                plugin.execute();
            } catch (Exception e) {
                System.err.println("Error executing plugin " + plugin.getName() + ": " + e.getMessage());
            }
        }
    }

}

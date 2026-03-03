package com.nhom3.ct240;

import com.nhom3.ct240.plugin.HostContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class Ct240Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Ct240Application.class, args);
        // Inject vào HostContext
        HostContext.setApplicationContext(context);

    }
}



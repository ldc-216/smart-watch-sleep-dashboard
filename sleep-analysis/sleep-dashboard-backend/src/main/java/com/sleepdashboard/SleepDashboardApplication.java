package com.sleepdashboard;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.sleepdashboard.*.mapper")
public class SleepDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(SleepDashboardApplication.class, args);
    }
}

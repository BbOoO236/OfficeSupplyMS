package com.officesupplyms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 企业办公室物资申领管理系统 - 主启动类
 */
@SpringBootApplication
@MapperScan("com.officesupplyms.mapper")
@EnableScheduling
public class OfficeSupplyManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(OfficeSupplyManagementSystemApplication.class, args);
        System.out.println("========================================");
        System.out.println("企业办公室物资申领管理系统启动成功！");
        System.out.println("后端API地址: http://localhost:8080/api");
        System.out.println("========================================");
    }
}
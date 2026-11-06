package com.officesupplyms.controller;

import com.officesupplyms.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 首页控制器，提供API根路径访问
 */
@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public Result<Map<String, Object>> home() {
        Map<String, Object> data = new HashMap<>();
        data.put("application", "企业办公室物资申领管理系统");
        data.put("version", "1.0.0");
        data.put("timestamp", LocalDateTime.now());
        data.put("status", "running");
        data.put("message", "API服务正常运行中");

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("用户管理", "/api/users");
        endpoints.put("物资管理", "/api/materials");
        endpoints.put("申领管理", "/api/applications");
        endpoints.put("入库管理", "/api/stock-ins");
        endpoints.put("出库管理", "/api/stock-outs");
        endpoints.put("库存盘点", "/api/inventory-checks");
        endpoints.put("消耗历史", "/api/consumption-histories");
        endpoints.put("预警记录", "/api/alert-records");
        endpoints.put("算法服务", "/api/algorithms");
        endpoints.put("认证服务", "/api/auth");

        data.put("endpoints", endpoints);

        return Result.success("欢迎使用企业办公室物资申领管理系统API", data);
    }

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("service", "office-supply-management-system");
        return Result.success(data);
    }
}
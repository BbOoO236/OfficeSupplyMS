package com.officesupplyms.controller;

import com.officesupplyms.model.dto.LoginDTO;
import com.officesupplyms.model.vo.LoginVO;
import com.officesupplyms.service.UserService;
import com.officesupplyms.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody LoginDTO loginDTO) {
        String token = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
        LoginVO loginVO = new LoginVO();
        // 这里应该返回用户信息，简化处理
        loginVO.setToken(token);
        return Result.success("登录成功", loginVO);
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        boolean success = userService.logout();
        return success ? Result.success("注销成功") : Result.error("注销失败");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public Result<Object> getCurrentUser() {
        return Result.success(userService.getCurrentUserInfo());
    }

    /**
     * 认证服务根路径，显示可用端点
     */
    @GetMapping
    public Result<Map<String, String>> authHome() {
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("login", "POST /api/auth/login - 用户登录");
        endpoints.put("logout", "POST /api/auth/logout - 用户注销");
        endpoints.put("current", "GET /api/auth/current - 获取当前用户信息");
        return Result.success("认证服务API", endpoints);
    }
}
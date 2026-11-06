package com.officesupplyms.controller;

import com.officesupplyms.model.dto.UserDTO;
import com.officesupplyms.model.vo.PageResult;
import com.officesupplyms.model.vo.UserVO;
import com.officesupplyms.service.UserService;
import com.officesupplyms.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 创建用户
     */
    @PostMapping
    public Result<UserVO> createUser(@Validated @RequestBody UserDTO userDTO) {
        return Result.success("创建用户成功", userService.createUser(userDTO));
    }

    /**
     * 登录
     */

    /**
     * 更新用户
     */
    @PutMapping("/{userId}")
    public Result<UserVO> updateUser(@PathVariable Long userId,
                                     @RequestBody UserDTO userDTO) {
        return Result.success("更新用户成功", userService.updateUser(userId, userDTO));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        boolean success = userService.deleteUser(userId);
        return success ? Result.success("删除用户成功") : Result.error("删除用户失败");
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(@PathVariable Long userId) {
        return Result.success(userService.getUserById(userId));
    }

    /**
     * 查询所有用户（支持多条件过滤）
     */
    @GetMapping
    public Result<PageResult<UserVO>> getAllUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long page,
            @RequestParam(required = false) Long limit) {
        List<UserVO> users = userService.getAllUsers();
        // 按用户名过滤
        if (username != null && !username.isEmpty()) {
            final String u = username;
            users = users.stream()
                    .filter(u2 -> u2.getUsername() != null && u2.getUsername().contains(u))
                    .collect(Collectors.toList());
        }
        // 按真实姓名过滤
        if (realName != null && !realName.isEmpty()) {
            final String name = realName;
            users = users.stream()
                    .filter(u2 -> u2.getRealName() != null && u2.getRealName().contains(name))
                    .collect(Collectors.toList());
        }
        // 按角色过滤
        if (role != null && !role.isEmpty()) {
            final String r = role;
            users = users.stream()
                    .filter(u2 -> r.equals(u2.getRole()))
                    .collect(Collectors.toList());
        }
        // 按部门过滤
        if (department != null && !department.isEmpty()) {
            final String d = department;
            users = users.stream()
                    .filter(u2 -> u2.getDepartment() != null && u2.getDepartment().contains(d))
                    .collect(Collectors.toList());
        }
        // 按状态过滤
        if (status != null) {
            final Integer s = status;
            users = users.stream()
                    .filter(u2 -> s.equals(u2.getStatus()))
                    .collect(Collectors.toList());
        }

        // 记录总数
        long total = users.size();

        // 应用分页
        if (page != null && limit != null && limit > 0) {
            int start = (int) ((page - 1) * limit);
            int end = (int) Math.min(start + limit, users.size());
            if (start < users.size()) {
                users = users.subList(start, end);
            } else {
                users = new java.util.ArrayList<>();
            }
        }

        return Result.success(PageResult.of(users, total));
    }

    /**
     * 根据角色查询用户
     */
    @GetMapping("/role/{role}")
    public Result<List<UserVO>> getUsersByRole(@PathVariable String role) {
        return Result.success(userService.getUsersByRole(role));
    }

    /**
     * 根据部门查询用户
     */
    @GetMapping("/department/{department}")
    public Result<List<UserVO>> getUsersByDepartment(@PathVariable String department) {
        return Result.success(userService.getUsersByDepartment(department));
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{userId}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long userId,
                                      @RequestParam String newPassword) {
        userService.resetPassword(userId, newPassword);
        return Result.success("重置密码成功");
    }

    /**
     * 修改当前用户密码
     */
    @PutMapping("/change-password")
    public Result<Void> changePassword(@RequestParam String oldPassword,
                                       @RequestParam String newPassword) {
        userService.changePassword(oldPassword, newPassword);
        return Result.success("修改密码成功");
    }

}
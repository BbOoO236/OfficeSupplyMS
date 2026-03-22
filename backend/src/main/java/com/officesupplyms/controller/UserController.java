package com.officesupplyms.controller;

import com.officesupplyms.model.dto.UserDTO;
import com.officesupplyms.model.vo.UserVO;
import com.officesupplyms.service.UserService;
import com.officesupplyms.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * 更新用户
     */
    @PutMapping("/{userId}")
    public Result<UserVO> updateUser(@PathVariable Long userId,
                                     @Validated @RequestBody UserDTO userDTO) {
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
     * 查询所有用户
     */
    @GetMapping
    public Result<List<UserVO>> getAllUsers() {
        return Result.success(userService.getAllUsers());
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
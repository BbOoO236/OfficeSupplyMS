package com.officesupplyms.service;

import com.officesupplyms.model.dto.UserDTO;
import com.officesupplyms.model.entity.User;
import com.officesupplyms.model.vo.UserVO;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户登录
     */
    String login(String username, String password);

    /**
     * 获取当前用户信息
     */
    UserVO getCurrentUserInfo();

    /**
     * 创建用户
     */
    UserVO createUser(UserDTO userDTO);

    /**
     * 更新用户
     */
    UserVO updateUser(Long userId, UserDTO userDTO);

    /**
     * 删除用户（逻辑删除）
     */
    boolean deleteUser(Long userId);

    /**
     * 根据ID查询用户
     */
    UserVO getUserById(Long userId);

    /**
     * 查询所有用户
     */
    List<UserVO> getAllUsers();

    /**
     * 根据角色查询用户
     */
    List<UserVO> getUsersByRole(String role);

    /**
     * 根据部门查询用户
     */
    List<UserVO> getUsersByDepartment(String department);

    /**
     * 重置用户密码
     */
    boolean resetPassword(Long userId, String newPassword);

    /**
     * 修改当前用户密码
     */
    boolean changePassword(String oldPassword, String newPassword);

    /**
     * 用户注销
     */
    boolean logout();
}
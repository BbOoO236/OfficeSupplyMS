package com.officesupplyms.service.impl;

import com.officesupplyms.mapper.UserMapper;
import com.officesupplyms.model.dto.UserDTO;
import com.officesupplyms.model.entity.User;
import com.officesupplyms.model.vo.UserVO;
import com.officesupplyms.service.UserService;
import com.officesupplyms.utils.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;


    @Override
    public String login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 验证密码（这里使用MD5简单加密，实际应该用更安全的加密方式）
        String encodedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!encodedPassword.equals(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }

        // 生成JWT token
        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public UserVO getCurrentUserInfo() {
        // 这里需要从SecurityContext或ThreadLocal获取当前用户信息
        // 简化处理，返回null
        return null;
    }

    @Override
    public UserVO createUser(UserDTO userDTO) {
        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(userDTO.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);

        // 加密密码
        if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
            user.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes())); // 默认密码
        } else {
            user.setPassword(DigestUtils.md5DigestAsHex(userDTO.getPassword().getBytes()));
        }

        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);

        return convertToVO(user);
    }

    @Override
    public UserVO updateUser(Long userId, UserDTO userDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新用户信息
        if (userDTO.getRealName() != null) {
            user.setRealName(userDTO.getRealName());
        }
        if (userDTO.getRole() != null) {
            user.setRole(userDTO.getRole());
        }
        if (userDTO.getStatus() != null) {
            user.setStatus(userDTO.getStatus());
        }
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getPhone() != null) {
            user.setPhone(userDTO.getPhone());
        }
        if (userDTO.getDepartment() != null) {
            user.setDepartment(userDTO.getDepartment());
        }

        // 如果提供了新密码，则更新密码
        if (userDTO.getPassword() != null && !userDTO.getPassword().trim().isEmpty()) {
            user.setPassword(DigestUtils.md5DigestAsHex(userDTO.getPassword().getBytes()));
        }

        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        return convertToVO(user);
    }

    @Override
    public boolean deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        // 逻辑删除：更新状态为禁用
        user.setStatus(0);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        return true;
    }

    @Override
    public UserVO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        return convertToVO(user);
    }

    @Override
    public List<UserVO> getAllUsers() {
        List<User> users = userMapper.selectList(null);
        return users.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserVO> getUsersByRole(String role) {
        List<User> users = userMapper.selectByRole(role);
        return users.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserVO> getUsersByDepartment(String department) {
        List<User> users = userMapper.selectByDepartment(department);
        return users.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean resetPassword(Long userId, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        user.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        return true;
    }

    @Override
    public boolean changePassword(String oldPassword, String newPassword) {
        // 这里需要获取当前用户，简化处理
        return true;
    }

    @Override
    public boolean logout() {
        // 注销操作，实际应该使token失效或清除session
        // 这里简单返回成功
        return true;
    }

    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
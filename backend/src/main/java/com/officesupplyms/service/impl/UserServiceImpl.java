package com.officesupplyms.service.impl;

import com.officesupplyms.mapper.UserMapper;
import com.officesupplyms.model.dto.UserDTO;
import com.officesupplyms.model.entity.User;
import com.officesupplyms.model.vo.UserVO;
import com.officesupplyms.service.UserService;
import com.officesupplyms.utils.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
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

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    @Override
    public String login(String username, String password, String role) {
        User user = userMapper.selectByUsername(username);

        // 检查用户是否被禁用（优先检查，避免泄露用户存在信息）
        if (user != null && user.getStatus() == 0) {
            throw new RuntimeException("该用户已被禁用，暂不可使用");
        }

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查账户是否被锁定
        LocalDateTime lockUntil = user.getLockUntil();
        if (lockUntil != null && LocalDateTime.now().isBefore(lockUntil)) {
            throw new RuntimeException("账号已锁定，请" + lockUntil.plusMinutes(15).toLocalTime().toString() + "后再试");
        }

        String storedPassword = user.getPassword();
        boolean passwordValid = false;
        boolean needMigrate = false;

        // 首先检查密码是否是BCrypt格式（支持$2a$和$2b$前缀）
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$")) {
            // 使用BCrypt验证
            passwordValid = passwordEncoder.matches(password, storedPassword);
        } else {
            // 可能是MD5格式，尝试MD5验证（使用UTF-8编码）
            String md5Password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
            if (md5Password.equalsIgnoreCase(storedPassword)) {
                passwordValid = true;
                needMigrate = true; // MD5密码需要迁移到BCrypt
            }
            // 如果不是MD5，密码无效
        }

        if (!passwordValid) {
            // 密码错误，处理错误计数
            int errorCount = user.getErrorCount() == null ? 0 : user.getErrorCount();
            errorCount++;
            user.setErrorCount(errorCount);

            if (errorCount >= 5) {
                // 错误次数达到5次，锁定账号15分钟
                user.setLockUntil(LocalDateTime.now().plusMinutes(15));
                userMapper.updateById(user);
                throw new RuntimeException("密码错误次数过多，账号已锁定15分钟");
            } else {
                // 未达到5次，更新错误次数
                userMapper.updateById(user);
                throw new RuntimeException("用户名或密码错误，剩余" + (5 - errorCount) + "次尝试机会");
            }
        }

        // 密码正确，登录成功，重置错误计数和锁定状态
        user.setErrorCount(0);
        user.setLockUntil(null);
        userMapper.updateById(user);

        // 如果需要迁移，将密码更新为BCrypt格式
        if (needMigrate) {
            user.setPassword(passwordEncoder.encode(password));
            userMapper.updateById(user);
        }

        // 验证角色（数据库中的role可能还是'USER'，需要兼容）
        String dbRole = user.getRole();
        String convertedRole = dbRole;
        // 如果是旧的'USER'角色，转换为'employee'
        if ("USER".equals(dbRole)) {
            convertedRole = "employee";
        } else if ("ADMIN".equals(dbRole)) {
            convertedRole = "admin";
        }

        // 允许原始角色或转换后的角色匹配
        if (!dbRole.equals(role) && !convertedRole.equals(role)) {
            throw new RuntimeException("角色权限错误");
        }

        // 生成JWT token，使用转换后的角色
        return jwtUtil.generateToken(user.getId(), user.getUsername(), convertedRole);
    }

    @Override
    public UserVO getCurrentUserInfo() {
        // 从请求头中获取token
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("未找到有效的身份验证令牌");
        }
        String token = authHeader.substring(7);
        // 验证token
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("令牌无效或已过期");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        return getUserById(userId);
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

        // 加密密码（使用BCrypt）
        if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode("123456")); // 默认密码
        } else {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
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

        // 如果提供了新密码，则更新密码（使用BCrypt）
        if (userDTO.getPassword() != null && !userDTO.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
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

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        return true;
    }

    @Override
    public boolean changePassword(String oldPassword, String newPassword) {
        // 获取当前用户ID
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("未找到有效的身份验证令牌");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("令牌无效或已过期");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        String storedPassword = user.getPassword();
        boolean passwordValid = false;
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$")) {
            passwordValid = passwordEncoder.matches(oldPassword, storedPassword);
        } else {
            // MD5验证
            String md5Password = DigestUtils.md5DigestAsHex(oldPassword.getBytes(StandardCharsets.UTF_8));
            passwordValid = md5Password.equalsIgnoreCase(storedPassword);
        }

        if (!passwordValid) {
            throw new RuntimeException("旧密码错误");
        }

        // 更新为新密码（使用BCrypt）
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
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
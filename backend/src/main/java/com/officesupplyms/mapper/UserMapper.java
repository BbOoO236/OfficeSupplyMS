package com.officesupplyms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.officesupplyms.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM user WHERE username = #{username} AND status = 1")
    User selectByUsername(@Param("username") String username);

    /**
     * 查询所有用户（分页）
     */
    List<User> selectAllUsers();

    /**
     * 根据角色查询用户
     */
    @Select("SELECT * FROM user WHERE role = #{role} AND status = 1 ORDER BY create_time DESC")
    List<User> selectByRole(@Param("role") String role);

    /**
     * 根据部门查询用户
     */
    @Select("SELECT * FROM user WHERE department = #{department} AND status = 1 ORDER BY create_time DESC")
    List<User> selectByDepartment(@Param("department") String department);
}
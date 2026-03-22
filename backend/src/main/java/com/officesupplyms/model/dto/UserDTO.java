package com.officesupplyms.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户创建/更新DTO
 */
@Data
public class UserDTO {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "角色不能为空")
    private String role; // USER, ADMIN

    private Integer status = 1; // 1-正常, 0-禁用

    private String email;

    private String phone;

    private String department;
}
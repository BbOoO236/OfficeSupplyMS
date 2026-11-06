package com.officesupplyms.model.vo;

import lombok.Data;

/**
 * 登录响应VO
 */
@Data
public class LoginVO {

    private Long userId;

    private String username;

    private String realName;

    private String role;

    private String token;

    private String department;
}
package com.officesupplyms.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 部门DTO
 */
@Data
public class DepartmentDTO {

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 100, message = "部门名称最多100个字符")
    private String name;

    @Size(max = 500, message = "部门描述最多500个字符")
    private String description;

    private Integer status;
}

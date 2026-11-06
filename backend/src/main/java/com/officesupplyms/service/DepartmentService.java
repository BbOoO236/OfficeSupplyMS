package com.officesupplyms.service;

import com.officesupplyms.model.dto.DepartmentDTO;
import com.officesupplyms.model.vo.DepartmentVO;

import java.util.List;

/**
 * 部门服务接口
 */
public interface DepartmentService {

    /**
     * 创建部门
     */
    DepartmentVO createDepartment(DepartmentDTO departmentDTO);

    /**
     * 更新部门
     */
    DepartmentVO updateDepartment(Long id, DepartmentDTO departmentDTO);

    /**
     * 删除部门
     */
    boolean deleteDepartment(Long id);

    /**
     * 根据ID查询部门
     */
    DepartmentVO getDepartmentById(Long id);

    /**
     * 查询所有部门
     */
    List<DepartmentVO> getAllDepartments();

    /**
     * 检查部门名称是否存在
     */
    boolean isDepartmentNameExists(String name);
}

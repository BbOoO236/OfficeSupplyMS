package com.officesupplyms.controller;

import com.officesupplyms.model.dto.DepartmentDTO;
import com.officesupplyms.model.vo.DepartmentVO;
import com.officesupplyms.service.DepartmentService;
import com.officesupplyms.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 */
@RestController
@RequestMapping("/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    /**
     * 创建部门
     */
    @PostMapping
    public Result<DepartmentVO> createDepartment(@Validated @RequestBody DepartmentDTO departmentDTO) {
        return Result.success("创建部门成功", departmentService.createDepartment(departmentDTO));
    }

    /**
     * 更新部门
     */
    @PutMapping("/{id}")
    public Result<DepartmentVO> updateDepartment(@PathVariable Long id,
                                                @Validated @RequestBody DepartmentDTO departmentDTO) {
        return Result.success("更新部门成功", departmentService.updateDepartment(id, departmentDTO));
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return Result.success("删除部门成功");
    }

    /**
     * 根据ID查询部门
     */
    @GetMapping("/{id}")
    public Result<DepartmentVO> getDepartmentById(@PathVariable Long id) {
        return Result.success(departmentService.getDepartmentById(id));
    }

    /**
     * 查询所有部门
     */
    @GetMapping
    public Result<List<DepartmentVO>> getAllDepartments() {
        return Result.success(departmentService.getAllDepartments());
    }
}

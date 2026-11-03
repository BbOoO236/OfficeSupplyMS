package com.officesupplyms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.officesupplyms.mapper.DepartmentMapper;
import com.officesupplyms.mapper.UserMapper;
import com.officesupplyms.model.dto.DepartmentDTO;
import com.officesupplyms.model.entity.Department;
import com.officesupplyms.model.entity.User;
import com.officesupplyms.model.vo.DepartmentVO;
import com.officesupplyms.service.DepartmentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门服务实现类
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public DepartmentVO createDepartment(DepartmentDTO departmentDTO) {
        // 检查名称是否重复
        if (isDepartmentNameExists(departmentDTO.getName())) {
            throw new RuntimeException("部门名称已存在");
        }

        Department department = new Department();
        BeanUtils.copyProperties(departmentDTO, department);
        department.setCreateTime(LocalDateTime.now());
        if (department.getStatus() == null) {
            department.setStatus(1);
        }

        departmentMapper.insert(department);
        return convertToVO(department);
    }

    @Override
    public DepartmentVO updateDepartment(Long id, DepartmentDTO departmentDTO) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new RuntimeException("部门不存在");
        }

        // 检查新名称是否与其他部门重复
        if (!department.getName().equals(departmentDTO.getName())) {
            if (isDepartmentNameExists(departmentDTO.getName())) {
                throw new RuntimeException("部门名称已存在");
            }
        }

        BeanUtils.copyProperties(departmentDTO, department);
        departmentMapper.updateById(department);
        return convertToVO(department);
    }

    @Override
    public boolean deleteDepartment(Long id) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new RuntimeException("部门不存在");
        }

        // 检查是否有用户属于该部门
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("department", department.getName());
        Long userCount = userMapper.selectCount(queryWrapper);
        if (userCount > 0) {
            throw new RuntimeException("该部门下有用户，无法删除");
        }

        return departmentMapper.deleteById(id) > 0;
    }

    @Override
    public DepartmentVO getDepartmentById(Long id) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            return null;
        }
        return convertToVO(department);
    }

    @Override
    public List<DepartmentVO> getAllDepartments() {
        List<Department> departments = departmentMapper.selectList(null);
        return departments.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isDepartmentNameExists(String name) {
        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        return departmentMapper.selectCount(queryWrapper) > 0;
    }

    private DepartmentVO convertToVO(Department department) {
        DepartmentVO vo = new DepartmentVO();
        BeanUtils.copyProperties(department, vo);
        return vo;
    }
}

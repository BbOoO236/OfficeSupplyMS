<template>
  <div class="department-container">
    <div class="filter-container">
      <el-form :inline="true" :model="listQuery" class="demo-form-inline">
        <el-form-item label="部门名称">
          <el-input
            v-model="listQuery.name"
            placeholder="请输入部门名称"
            clearable
            @keyup.enter.native="handleFilter"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="listQuery.status" placeholder="请选择" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="operation-container">
      <el-button type="primary" icon="el-icon-plus" @click="handleCreate">新增部门</el-button>
    </div>

    <el-table
      v-loading="listLoading"
      :data="list"
      border
      fit
      highlight-current-row
      style="width: 100%;"
    >
      <el-table-column label="部门ID" prop="id" align="center" width="100" />
      <el-table-column label="部门名称" prop="name" align="center" min-width="150">
        <template slot-scope="{row}">
          <span>{{ row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="部门描述" prop="description" align="center" min-width="200">
        <template slot-scope="{row}">
          <span>{{ row.description || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" align="center" width="100">
        <template slot-scope="{row}">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" align="center" width="180">
        <template slot-scope="{row}">
          <span>{{ row.createTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template slot-scope="{row}">
          <el-button type="primary" size="mini" @click="handleUpdate(row)">编辑</el-button>
          <el-button type="danger" size="mini" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form ref="departmentForm" :model="departmentForm" :rules="departmentRules" label-width="100px">
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="departmentForm.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="部门描述" prop="description">
          <el-input
            v-model="departmentForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入部门描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="departmentForm.status" placeholder="请选择状态">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSave">确认</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as departmentApi from '@/api/department'

export default {
  name: 'DepartmentList',
  data() {
    return {
      list: null,
      listLoading: true,
      listQuery: {
        name: '',
        status: ''
      },
      dialogVisible: false,
      dialogTitle: '',
      isEdit: false,
      departmentForm: {
        id: null,
        name: '',
        description: '',
        status: 1
      },
      departmentRules: {
        name: [
          { required: true, message: '请输入部门名称', trigger: 'blur' }
        ],
        status: [
          { required: true, message: '请选择状态', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.listLoading = true
      departmentApi.getDepartmentList().then(response => {
        let data = response.data || []
        // 前端过滤
        if (this.listQuery.name) {
          data = data.filter(d => d.name.includes(this.listQuery.name))
        }
        if (this.listQuery.status !== '' && this.listQuery.status !== null) {
          data = data.filter(d => d.status === this.listQuery.status)
        }
        this.list = data
        this.listLoading = false
      }).catch(() => {
        this.listLoading = false
      })
    },
    handleFilter() {
      this.getList()
    },
    resetFilter() {
      this.listQuery = {
        name: '',
        status: ''
      }
      this.getList()
    },
    handleCreate() {
      this.dialogTitle = '新增部门'
      this.isEdit = false
      this.departmentForm = {
        id: null,
        name: '',
        description: '',
        status: 1
      }
      this.dialogVisible = true
    },
    handleUpdate(row) {
      this.dialogTitle = '编辑部门'
      this.isEdit = true
      this.departmentForm = {
        id: row.id,
        name: row.name,
        description: row.description,
        status: row.status
      }
      this.dialogVisible = true
    },
    handleDelete(row) {
      this.$confirm(`确认删除部门"${row.name}"吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        departmentApi.deleteDepartment(row.id).then(() => {
          this.$message.success('删除成功')
          this.getList()
        }).catch((error) => {
          this.$message.error(error.message || '删除失败')
        })
      }).catch(() => {})
    },
    confirmSave() {
      this.$refs.departmentForm.validate(valid => {
        if (valid) {
          if (this.isEdit) {
            departmentApi.updateDepartment(this.departmentForm.id, this.departmentForm).then(() => {
              this.$message.success('更新成功')
              this.dialogVisible = false
              this.getList()
            })
          } else {
            departmentApi.createDepartment(this.departmentForm).then(() => {
              this.$message.success('创建成功')
              this.dialogVisible = false
              this.getList()
            })
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.department-container {
  padding: 0;
}

.filter-container {
  background: $surface-color;
  border-radius: $border-radius-xl;
  padding: 20px;
  margin-bottom: 16px;
  border: 1px solid $border-color;

  ::v-deep .el-form-item {
    margin-bottom: 0;
  }
}

.operation-container {
  margin-bottom: 16px;
}

::v-deep .el-table {
  border-radius: $border-radius-xl;
  overflow: hidden;

  th.el-table__cell {
    background-color: $background-color !important;
  }
}
</style>

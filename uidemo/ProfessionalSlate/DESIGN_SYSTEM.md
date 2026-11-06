# OfficeSupplyMS 设计系统 - Professional Slate

## 设计理念

稳重商务风格，深蓝为主色配合石灰灰，体现权威性和专业感。适合金融、医疗、法律等传统企业级管理系统。

## 色彩体系

| 角色 | 色值 | 用途 |
|------|------|------|
| Primary | `#1E40AF` | 主色，深蓝 |
| Secondary | `#3B82F6` | 辅助色，亮蓝 |
| Accent | `#F59E0B` | 强调色，金色 |
| Success | `#059669` | 成功色，绿色 |
| Warning | `#D97706` | 警告色，琥珀 |
| Danger | `#DC2626` | 危险色，红色 |
| Background | `#F1F5F9` | 页面背景，浅灰蓝 |
| Surface | `#FFFFFF` | 卡片背景，纯白 |
| Text Primary | `#0F172A` | 主文本，深色 |
| Text Secondary | `#64748B` | 次要文本，灰 |
| Border | `#CBD5E1` | 边框色，石灰 |

## 字体

- **标题字体**: Poppins (Google Fonts)
  - 特点: 现代、几何、专业
  - 字重: 500-700

- **正文字体**: Open Sans (Google Fonts)
  - 特点: 清晰、易读、通用
  - 字重: 400-600

## 样式特点

### Professional Slate 风格
- 权威感与信任感并重
- 安全认证徽章展示
- 数据指标突出显示
- 清晰的合规标识

### 布局特点
- 经典侧边导航
- 卡片与表格结合
- 状态指示清晰
- 适度的视觉层次

### 动效规范
- 过渡时间: 200ms
- 稳重而非花哨
- 成功/警告/错误状态分明
- 徽章悬停效果

## 首页结构

1. **侧边导航** - 企业Logo + 分类菜单 + 用户信息
2. **顶部导航** - 页面标题 + 快捷搜索 + 通知 + 操作按钮
3. **欢迎横幅** - 问候 + 日期 + 本周统计
4. **数据仪表盘** - KPI指标卡片组
5. **待办事项** - 待审批任务列表
6. **快捷功能** - 常用操作入口
7. **公告中心** - 系统通知
8. **库存监控** - 预警物资列表

## 组件规范

### 卡片组件
- 圆角: `rounded-lg`
- 阴影: `shadow-sm`
- 内边距: `p-5`
- 可选: 顶部彩色边框表示状态

### 按钮组件
- 主按钮: `bg-blue-700 text-white hover:bg-blue-800`
- 次按钮: `border border-blue-700 text-blue-700 hover:bg-blue-50`
- 圆角: `rounded-md`

### 状态徽章
- 成功: `bg-emerald-100 text-emerald-700`
- 警告: `bg-amber-100 text-amber-700`
- 错误: `bg-red-100 text-red-700`
- 信息: `bg-blue-100 text-blue-700`

### 导航
- 侧边栏宽度: `w-60`
- 选中状态: `bg-blue-50 text-blue-700 border-l-4 border-blue-700`

## 反模式 (避免)

- ❌ 过于活泼的颜色
- ❌ 隐藏凭证/认证
- ❌ 复杂动画
- ❌ 拥挤布局

# OfficeSupplyMS 设计系统 - Corporate Teal

## 设计理念

现代企业风格，青色为主色调，配合橙色CTA，强调功能性与现代感的平衡。适合SaaS平台和数据分析类管理工具。

## 色彩体系

| 角色 | 色值 | 用途 |
|------|------|------|
| Primary | `#0D9488` | 主色，青色 |
| Secondary | `#14B8A6` | 辅助色，浅青 |
| Accent | `#F97316` | 强调色，橙色 |
| Background | `#F0FDFA` | 页面背景，青白 |
| Surface | `#FFFFFF` | 卡片背景，纯白 |
| Text Primary | `#0F172A` | 主文本，深色 |
| Text Secondary | `#64748B` | 次要文本，灰 |
| Border | `#E2E8F0` | 边框色 |

## 字体

- **字体**: Plus Jakarta Sans (Google Fonts)
  - 特点: 现代、友好、专业
  - 字重: 400-600
  - Mood: SaaS, clean, approachable

## 样式特点

### Corporate Teal 风格
- 现代微交互设计
- 卡片网格布局
- 数据可视化优先
- 快速响应反馈

### 布局特点
- 侧边导航 + 顶部工具栏
- 卡片式数据展示
- 网格化快捷入口
- 适度的圆角和阴影

### 动效规范
- 过渡时间: 150-200ms
- 微交互: 50-100ms
- hover 颜色/阴影变化
- 成功/错误状态动画

## 首页结构

1. **侧边导航** - Logo + 功能菜单 + 用户信息
2. **顶部工具栏** - 搜索 + 通知 + 快捷操作
3. **欢迎区域** - 时间问候 + 本周概览
4. **数据看板** - 关键指标卡片
5. **快捷入口** - 核心功能网格
6. **最近动态** - 申请/审批列表
7. **库存状态** - 预警卡片

## 组件规范

### 卡片组件
- 圆角: `rounded-xl`
- 阴影: `shadow-sm hover:shadow-md`
- 内边距: `p-5`
- 过渡: `transition-shadow duration-200`

### 按钮组件
- 主按钮: `bg-teal-600 text-white hover:bg-teal-700`
- 次按钮: `border border-teal-600 text-teal-600 hover:bg-teal-50`
- 圆角: `rounded-lg`

### 导航
- 侧边栏宽度: `w-64`
- 选中状态: `bg-teal-50 text-teal-700 border-l-4 border-teal-600`

## 反模式 (避免)

- ❌ 复杂入职流程
- ❌ 缓慢加载
- ❌ 过度动画
- ❌ 拥挤布局

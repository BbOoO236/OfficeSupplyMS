# OfficeSupplyMS 设计系统 - Swiss Minimalism

## 设计理念

极简瑞士风格，追求极致的简洁与功能性。黑白为主色调，配合单一强调色点缀，体现高端B2B工具的专业感。

## 色彩体系

| 角色 | 色值 | 用途 |
|------|------|------|
| Primary | `#000000` | 主色，纯黑 |
| Secondary | `#F5F5F5` | 浅灰背景 |
| Accent | `#2563EB` | 强调色，纯蓝 |
| Background | `#FFFFFF` | 页面背景，纯白 |
| Surface | `#FFFFFF` | 卡片背景 |
| Text Primary | `#000000` | 主文本，纯黑 |
| Text Secondary | `#6B7280` | 次要文本，灰 |
| Border | `#E5E7EB` | 边框色 |

**特点：无阴影、无渐变、高对比度**

## 字体

- **字体**: Inter (Google Fonts)
  - 特点: 瑞士风格、现代、清晰
  - 字重: 400-700
  - 等宽数字: Tabular nums

## 样式特点

### Swiss Minimalism 风格
- 严格的12栏网格系统
- 数学比例间距（8px基准单位）
- 黑白为主，单一强调色
- 零阴影、零渐变
- 快速加载，高对比度

### 布局特点
- 极简导航，只保留核心功能
- 网格化卡片布局
- 大留白，0px圆角（可选4px）
- 清晰的信息层级

### 动效规范
- 过渡时间: 200-250ms
- 使用 ease-out 缓动
- hover 仅用颜色/透明度变化
- 无尺寸变化动画

## 首页结构

1. **顶部导航栏** - Logo + 核心导航 + 简洁用户区
2. **统计概览** - 网格布局的数据指标
3. **主要操作区** - 核心功能入口
4. **数据列表** - 简洁表格展示
5. **系统状态** - 简洁状态指示

## 组件规范

### 卡片组件
- 圆角: `rounded-none` 或 `rounded-sm`
- 边框: `border border-gray-200`
- 内边距: `p-6`
- 无阴影

### 按钮组件
- 主按钮: `bg-black text-white hover:bg-gray-800`
- 次按钮: `border border-black text-black hover:bg-gray-50`
- 圆角: `rounded-sm`

### 导航
- 侧边栏宽度: `w-56`
- 选中状态: 左侧 `border-l-2 border-black bg-gray-50`

## 反模式 (避免)

- ❌ 阴影或渐变
- ❌ 多个强调色
- ❌ 圆角卡片
- ❌ 装饰性元素
- ❌ Emoji 作为图标
- ❌ 过于拥挤的布局

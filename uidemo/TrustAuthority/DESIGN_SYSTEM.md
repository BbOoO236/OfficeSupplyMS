# OfficeSupplyMS 设计系统 - Trust Authority

## 设计理念

简约大气、偏欧美企业风格，强调专业、可信赖、高效。

## 色彩体系

| 角色 | 色值 | 用途 |
|------|------|------|
| Primary | `#0891B2` | 主色调，青色，代表专业与信任 |
| Secondary | `#22D3EE` | 辅助色，浅青色，用于hover和强调 |
| CTA | `#22C55E` | 行动按钮，绿色，代表确认与成功 |
| Background | `#F8FAFC` | 页面背景，石板灰白 |
| Surface | `#FFFFFF` | 卡片背景，纯白 |
| Text Primary | `#0F172A` | 主文本，深色 slate-900 |
| Text Secondary | `#475569` | 次要文本，slate-600 |
| Border | `#E2E8F0` | 边框色，slate-200 |

## 字体

- **标题字体**: Lexend (Google Fonts)
  - 特点: 现代、清晰、易读性强
  - 字重: 600-700 用于主标题，500 用于副标题

- **正文字体**: Source Sans 3 (Google Fonts)
  - 特点: 专业、适合长文本阅读
  - 字重: 400-500 用于正文

## 样式特点

### Trust & Authority 风格
- 强调数据指标和可视化
- 使用徽章和认证标志增强信任感
- 清晰的信息层级
- 专业的数据卡片展示

### 布局特点
- 侧边导航 + 顶部导航栏
- 卡片式数据展示
- 简洁的表格列表
- 大留白，简约不拥挤

### 动效规范
- 过渡时间: 150-300ms
- 使用 ease-out 缓动
- hover 状态使用颜色变化而非尺寸变化
- 避免复杂动画，保持专业感

## 首页结构

1. **顶部导航栏** - Logo + 主导航 + 用户信息
2. **欢迎区域** - 问候语 + 快速统计
3. **快捷入口卡片** - 核心功能快速访问
4. **数据概览** - 今日/本周关键指标
5. **最近操作** - 最新物资申请/审批记录
6. **系统公告** - 重要通知

## 组件规范

### 卡片组件
- 圆角: `rounded-xl`
- 阴影: `shadow-sm`
- 内边距: `p-6`
- 间距: 卡片间距 `gap-6`

### 按钮组件
- 主按钮: `bg-primary text-white hover:bg-primary/90`
- 次按钮: `border border-primary text-primary hover:bg-primary/10`
- 圆角: `rounded-lg`

### 导航
- 侧边栏宽度: `w-64`
- 选中状态: 左侧 `border-l-4 border-primary bg-primary/5`

## 反模式 (避免)

- ❌ 过度使用渐变背景
- ❌ 过于花哨的动画
- ❌ Emoji 作为图标
- ❌ 拥挤的布局
- ❌ 过浅的文本颜色

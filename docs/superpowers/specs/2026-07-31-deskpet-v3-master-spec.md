# DeskPet v3 主设计文档 — 功能补全与完善

> 日期：2026-07-31
> 状态：已确认，立即实施
> 架构：主Spec + 5份子Spec层级结构

## 背景

v2.x 完成了宠物主体从矢量绘制到图片资源的迁移（水彩风格静态PNG + Compose代码动效）。但在功能核对中发现多个模块存在严重缺失：
- 语音对话仅2句硬编码，已有的28句DialogueBank未被调用
- 48件装饰中仅16件有矢量绘制，其余32件回退emoji
- 墙纸4款中仅2款有矢量绘制
- 服装类8件中仅围巾1件有矢量绘制

## 公共约束（所有子任务必须遵守）

### 1. 锚点系统统一
所有矢量绘制必须基于 PetCanvas.kt 中已定义的 PetAnchors：
- headCy / headR — 头部位置和半径
- bodyCy / bodyRy — 身体位置和半径
- headTopY / collarY / clothingY / tailX / tailY — 装饰锚点
- 所有坐标用 size.width * RATIO 计算，禁止硬编码 px

### 2. 水彩调色板统一
所有矢量绘制复用 OutfitRenderer.drawWatercolorBlob 函数和统一调色板：
- 主色 + 高光色(hi) + 暗部色(dk) 三层渐变
- 半透明叠加模拟水彩晕染

### 3. OutfitRenderer 扩展规则
- 新增绘制函数命名：draw{ItemName}（如 drawSweater、drawDress）
- 在 render() 的 when 分支新增对应 ID
- 返回 true 表示已渲染，false 走 emoji fallback
- 锚点通过 getPosition() 返回 (x, y) 对齐

### 4. 版本管理
- versionCode 从 12 递增，每子任务完成后独立提交
- sub1→v3.1, sub2→v3.2, sub3→v3.3, sub4→v3.4, sub5→v3.5

### 5. 依赖关系
```
sub1(语音) ──────────────────────────────→ 独立
sub2(服装矢量) ──┐
sub3(墙纸矢量) ──┼──→ 共享 OutfitRenderer/FurnitureRenderer ──→ 独立
sub4(其他矢量) ──┘
sub5(系统核对) ──→ 依赖前4个完成后再核对
```

sub1-sub4 可完全并行，sub5 在前4个合并后执行。

## 子任务列表

| 编号 | 名称 | Spec文件 | 涉及文件 |
|------|------|---------|---------|
| sub1 | 语音对话系统重构 | sub1-voice-system.md | DialogueBank.kt, PetViewModel.kt, PetOverlayService.kt |
| sub2 | 服装矢量补全(7件) | sub2-clothing-vector.md | OutfitRenderer.kt |
| sub3 | 墙纸矢量补全(2件) | sub3-wallpaper-vector.md | FurnitureRenderer.kt |
| sub4 | 眼镜/项圈/尾饰/随身矢量(19件) | sub4-accessory-vector.md | OutfitRenderer.kt |
| sub5 | 其他系统核对修复 | sub5-system-audit.md | 旅行/成就/记忆/打卡等 |

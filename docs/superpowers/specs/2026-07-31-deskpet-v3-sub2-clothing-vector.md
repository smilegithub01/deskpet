# Sub2：服装矢量补全（7件）

> 目标：为CLOTHING类剩余7件服装实现水彩风格矢量绘制

## 现状
CLOTHING类8件中仅 cloth_scarf（围巾）有矢量绘制，其余7件回退emoji。

## 需补全的服装

| ID | 名称 | 形状描述 | 矢量策略 |
|----|------|---------|---------|
| cloth_sweater | 毛衣 | 椭圆覆盖身体+领口 | drawWatercolorBlob椭圆+圆弧领口 |
| cloth_dress | 小裙子 | 梯形+褶皱下摆 | Path梯形+锯齿边 |
| cloth_cape | 披风 | 半圆+波浪边 | Path弧形+cubicTo波浪 |
| cloth_suit | 西装 | 矩形+V领+领带 | Path矩形+三角领口 |
| cloth_kimono | 和服 | 梯形+腰带+交叉领 | Path梯形+矩形腰带 |
| cloth_swimsuit | 泳衣 | 小椭圆+肩带 | drawWatercolorBlob+线条 |
| cloth_pajama | 睡衣 | 椭圆+条纹 | drawWatercolorBlob+横纹线 |

## 实施规则
1. 每件服装函数签名：`private fun DrawScope.drawXxx(cx: Float, cy: Float, r: Float)`
2. 使用 OutfitRenderer 已有调色板（GoldColor/RedColor/PinkAccent/BlueColor等）
3. 在 render() 的 when 分支新增7个case
4. 锚点复用 getPosition(CLOTHING) 返回的位置
5. 水彩效果用 drawWatercolorBlob/drawWatercolorCircle

## 涉及文件
- `ui/components/OutfitRenderer.kt` — 新增7个绘制函数+7个when分支

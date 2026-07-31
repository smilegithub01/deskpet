# Sub3：墙纸矢量补全（2件）

> 目标：为 wall_star（星空）和 wall_rainbow（彩虹）实现矢量绘制

## 现状
4款墙纸中 wall_pink 和 wall_mint 有矢量绘制（drawWallpaper函数），wall_star 和 wall_rainbow 回退emoji。

## 需补全的墙纸

| ID | 名称 | 视觉描述 | 矢量策略 |
|----|------|---------|---------|
| wall_star | 星空墙纸 | 深蓝渐变背景+随机星星+月亮 | 渐变rect+drawCircle星星+drawArc月亮 |
| wall_rainbow | 彩虹墙纸 | 浅色背景+彩虹弧线+云朵 | 多色drawArc叠加+drawCircle云朵 |

## 实施规则
1. 在 FurnitureRenderer.render() 的 when 分支新增2个case
2. wall_star：深蓝→紫色渐变背景 + 20-30个随机大小星星 + 1个月牙
3. wall_rainbow：浅粉背景 + 7色彩虹弧（红橙黄绿蓝靛紫）+ 2-3朵白云
4. 复用现有 drawWallpaper 的布局逻辑（墙面占h*0.6）
5. 删除 wall_sky 死代码（渲染器有但目录无）

## 涉及文件
- `ui/components/FurnitureRenderer.kt` — 新增drawStarWallpaper和drawRainbowWallpaper+删除wall_sky

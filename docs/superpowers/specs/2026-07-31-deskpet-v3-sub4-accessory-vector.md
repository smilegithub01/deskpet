# Sub4：眼镜/项圈/尾饰/随身矢量补全（19件）

> 目标：为GLASSES(5件)、COLLAR(6件)、TAIL(6件)、ACCESSORY(7件)实现矢量绘制

## 需补全的装饰

### GLASSES 眼镜（5件）
| ID | 名称 | 矢量策略 |
|----|------|---------|
| glasses_3d | 3D眼镜 | 矩形+红蓝镜片 |
| glasses_star | 星星眼镜 | 星星形镜框(drawStar) |
| glasses_monocle | 单片眼镜 | 单圆框+链条 |
| glasses_party | 派对眼镜 | 圆框+彩色彩带 |
| glasses_neon | 霓虹眼镜 | 方框+霓虹渐变色 |

### COLLAR 项圈（6件）
| ID | 名称 | 矢量策略 |
|----|------|---------|
| collar_bow | 蝴蝶项圈 | drawBow+横线 |
| collar_pearl | 珍珠项链 | 多个小圆珠排列 |
| collar_gold | 金链 | 金色drawLine+椭圆环 |
| collar_bone | 骨头吊坠 | 横线+骨头形Path |
| collar_crystal | 水晶吊坠 | 横线+菱形Path+渐变 |
| collar_flower | 花朵项圈 | 横线+drawFlower |

### TAIL 尾饰（6件）
| ID | 名称 | 矢量策略 |
|----|------|---------|
| tail_flower | 花朵 | drawFlower |
| tail_balloon | 气球 | drawBalloon |
| tail_butterfly | 蝴蝶结 | 双drawWatercolorLeaf+身体 |
| tail_rainbow | 彩虹 | 多色drawArc叠加 |
| tail_cloud | 云朵 | 3个drawCircle重叠 |
| tail_heart | 爱心 | Path心形+渐变 |

### ACCESSORY 随身（7件）
| ID | 名称 | 矢量策略 |
|----|------|---------|
| acc_lollipop | 棒棒糖 | drawCircle螺旋+棍子 |
| acc_umbrella | 小伞 | drawArc半圆+伞柄 |
| acc_wand | 魔法棒 | 星星drawStar+棍子+光晕 |
| acc_book | 魔法书 | drawRoundRect书本+星星装饰 |
| acc_camera | 相机 | drawRoundRect机身+drawCircle镜头 |
| acc_gift | 礼物盒 | drawRoundRect+十字丝带 |
| acc_star | 星星权杖 | drawStar+棍子 |

## 实施规则
1. 每件函数签名：`private fun DrawScope.drawXxx(cx: Float, cy: Float, r: Float)`
2. 复用 OutfitRenderer 已有调色板和 drawWatercolorBlob/drawWatercolorCircle/drawWatercolorLeaf
3. 在 render() 的 when 分支新增19个case
4. 锚点复用 getPosition() 返回的位置

## 涉及文件
- `ui/components/OutfitRenderer.kt` — 新增19个绘制函数+19个when分支

// Render Plan D preview to PNG using node-canvas
const { createCanvas } = require('canvas');
const fs = require('fs');

const W = 280, H = 280;
const COLS = 4;
const PADDING = 16;
const LABEL_H = 40;
const HEADER_H = 60;

function shade(hex, f){
  let c = hex.replace('#','');
  let r = parseInt(c.substr(0,2),16), g = parseInt(c.substr(2,2),16), b = parseInt(c.substr(4,2),16);
  r = Math.min(255, Math.max(0, Math.round(r*f)));
  g = Math.min(255, Math.max(0, Math.round(g*f)));
  b = Math.min(255, Math.max(0, Math.round(b*f)));
  return [r,g,b];
}
function rgba(arr, a){ return `rgba(${arr[0]},${arr[1]},${arr[2]},${a})`; }

function anchors(w, h){
  const bodyCx=w*0.50, bodyCy=h*0.73, bodyRx=w*0.26, bodyRy=h*0.22;
  const headCx=w*0.50, headCy=h*0.39, headR=w*0.235;
  const bodyTop = bodyCy-bodyRy, bodyBottom = bodyCy+bodyRy;
  return {
    w,h, bodyCx, bodyCy, bodyRx, bodyRy, bodyTop, bodyBottom,
    bodyLeft: bodyCx-bodyRx, bodyRight: bodyCx+bodyRx,
    headCx, headCy, headR,
    eyeY: headCy + headR*0.14,
    leftEyeX: headCx - headR*0.42,
    rightEyeX: headCx + headR*0.42,
    eyeW: headR*0.24, eyeH: headR*0.28,
    blushY: headCy + headR*0.42,
    leftBlushX: headCx - headR*0.58,
    rightBlushX: headCx + headR*0.58,
    blushR: headR*0.20,
    noseY: headCy + headR*0.52,
    noseR: headR*0.075,
    mouthY: headCy + headR*0.66,
    leftPawX: bodyCx - bodyRx*0.48,
    rightPawX: bodyCx + bodyRx*0.48,
    pawY: bodyBottom - bodyRy*0.10,
    pawR: headR*0.13,
    leftFootX: bodyCx - bodyRx*0.68,
    rightFootX: bodyCx + bodyRx*0.68,
    footY: bodyBottom,
    footR: headR*0.15,
    headTopY: headCy - headR*1.02,
    collarY: bodyTop + bodyRy*0.15,
    clothingY: bodyCy + bodyRy*0.20
  };
}

function drawWatercolorBlob(ctx, cx, cy, rx, ry, main, hi, dk){
  const maxR = Math.max(rx,ry);
  let g1 = ctx.createRadialGradient(cx,cy,0,cx,cy,maxR*1.8);
  g1.addColorStop(0, rgba(main,0.10));
  g1.addColorStop(1, rgba(main,0));
  ctx.fillStyle = g1;
  ctx.beginPath(); ctx.ellipse(cx,cy,rx*1.7,ry*1.7,0,0,Math.PI*2); ctx.fill();
  let g2 = ctx.createRadialGradient(cx-rx*0.25, cy-ry*0.3, 0, cx-rx*0.25, cy-ry*0.3, maxR*1.15);
  g2.addColorStop(0, rgba(hi,0.95));
  g2.addColorStop(0.55, rgba(main,0.95));
  g2.addColorStop(0.85, rgba(dk,0.85));
  g2.addColorStop(1, rgba(dk,0));
  ctx.fillStyle = g2;
  ctx.beginPath(); ctx.ellipse(cx,cy,rx,ry,0,0,Math.PI*2); ctx.fill();
  let g3 = ctx.createRadialGradient(cx-rx*0.3, cy-ry*0.45, 0, cx-rx*0.3, cy-ry*0.45, rx*0.6);
  g3.addColorStop(0, rgba(hi,0.55));
  g3.addColorStop(1, rgba(hi,0));
  ctx.fillStyle = g3;
  ctx.beginPath(); ctx.ellipse(cx-rx*0.3, cy-ry*0.45, rx*0.55, ry*0.45, 0, 0, Math.PI*2); ctx.fill();
}

function drawSoftOutline(ctx, cx, cy, rx, ry, color, width, alpha){
  ctx.strokeStyle = rgba(color, alpha);
  ctx.lineWidth = width;
  ctx.lineCap = 'round';
  ctx.beginPath();
  ctx.ellipse(cx, cy, rx, ry, 0, 0, Math.PI*2);
  ctx.stroke();
}

function drawWatercolorCircle(ctx, cx, cy, r, main, hi, dk){
  drawWatercolorBlob(ctx, cx, cy, r, r, main, hi, dk);
}
function drawWatercolorLeaf(ctx, cx, cy, r, main, hi, dk, angle){
  ctx.save();
  ctx.translate(cx, cy);
  ctx.rotate(angle);
  let g = ctx.createLinearGradient(0, -r, 0, r);
  g.addColorStop(0, rgba(hi, 0.95));
  g.addColorStop(0.5, rgba(main, 0.92));
  g.addColorStop(1, rgba(dk, 0.8));
  ctx.fillStyle = g;
  ctx.beginPath();
  ctx.moveTo(0, -r);
  ctx.quadraticCurveTo(r*0.6, 0, 0, r);
  ctx.quadraticCurveTo(-r*0.6, 0, 0, -r);
  ctx.fill();
  ctx.strokeStyle = rgba(main, 0.15);
  ctx.lineWidth = r*0.2;
  ctx.stroke();
  ctx.restore();
}

function drawEarTriangle(ctx, x1, y1, x2, y2, x3, y3, main, hi, dk){
  const cx = (x1+x2+x3)/3, cy = (y1+y2+y3)/3;
  const r = Math.max(Math.hypot(x1-cx,y1-cy), Math.hypot(x2-cx,y2-cy));
  let g = ctx.createRadialGradient(cx - r*0.2, cy - r*0.2, 0, cx, cy, r*1.1);
  g.addColorStop(0, rgba(hi, 0.95));
  g.addColorStop(0.6, rgba(main, 0.92));
  g.addColorStop(1, rgba(dk, 0.85));
  ctx.fillStyle = g;
  ctx.beginPath();
  ctx.moveTo(x1,y1); ctx.lineTo(x2,y2); ctx.lineTo(x3,y3); ctx.closePath();
  ctx.fill();
}

function drawCatEars(ctx, a, base, dark, light, lighter){
  const earTopY = a.headCy - a.headR*0.95;
  const earBaseY = a.headCy - a.headR*0.55;
  drawEarTriangle(ctx, a.headCx - a.headR*0.55, earBaseY, a.headCx - a.headR*0.85, earTopY, a.headCx - a.headR*0.25, earBaseY + a.headR*0.05, base, lighter, dark);
  drawEarTriangle(ctx, a.headCx + a.headR*0.55, earBaseY, a.headCx + a.headR*0.85, earTopY, a.headCx + a.headR*0.25, earBaseY + a.headR*0.05, base, lighter, dark);
  ctx.fillStyle = rgba([255,190,206], 0.6);
  ctx.beginPath();
  ctx.moveTo(a.headCx - a.headR*0.65, earBaseY - a.headR*0.05);
  ctx.lineTo(a.headCx - a.headR*0.78, earTopY + a.headR*0.15);
  ctx.lineTo(a.headCx - a.headR*0.4, earBaseY);
  ctx.closePath(); ctx.fill();
  ctx.beginPath();
  ctx.moveTo(a.headCx + a.headR*0.65, earBaseY - a.headR*0.05);
  ctx.lineTo(a.headCx + a.headR*0.78, earTopY + a.headR*0.15);
  ctx.lineTo(a.headCx + a.headR*0.4, earBaseY);
  ctx.closePath(); ctx.fill();
}
function drawDogEars(ctx, a, base, dark, light, lighter){
  drawWatercolorBlob(ctx, a.headCx - a.headR*0.92, a.headCy + a.headR*0.15, a.headR*0.22, a.headR*0.42, base, lighter, dark);
  drawWatercolorBlob(ctx, a.headCx + a.headR*0.92, a.headCy + a.headR*0.15, a.headR*0.22, a.headR*0.42, base, lighter, dark);
}
function drawRabbitEars(ctx, a, base, dark, light, lighter){
  drawWatercolorBlob(ctx, a.headCx - a.headR*0.32, a.headCy - a.headR*1.05, a.headR*0.15, a.headR*0.55, base, lighter, dark);
  drawWatercolorBlob(ctx, a.headCx + a.headR*0.32, a.headCy - a.headR*1.05, a.headR*0.15, a.headR*0.55, base, lighter, dark);
  ctx.fillStyle = rgba([255,190,206], 0.45);
  ctx.beginPath();
  ctx.ellipse(a.headCx - a.headR*0.32, a.headCy - a.headR*1.05, a.headR*0.07, a.headR*0.42, 0, 0, Math.PI*2);
  ctx.fill();
  ctx.beginPath();
  ctx.ellipse(a.headCx + a.headR*0.32, a.headCy - a.headR*1.05, a.headR*0.07, a.headR*0.42, 0, 0, Math.PI*2);
  ctx.fill();
}
function drawHamsterEars(ctx, a, base, dark, light, lighter){
  drawWatercolorBlob(ctx, a.headCx - a.headR*0.75, a.headCy - a.headR*0.7, a.headR*0.16, a.headR*0.16, base, lighter, dark);
  drawWatercolorBlob(ctx, a.headCx + a.headR*0.75, a.headCy - a.headR*0.7, a.headR*0.16, a.headR*0.16, base, lighter, dark);
  ctx.fillStyle = rgba([255,190,206], 0.45);
  ctx.beginPath(); ctx.arc(a.headCx - a.headR*0.75, a.headCy - a.headR*0.7, a.headR*0.07, 0, Math.PI*2); ctx.fill();
  ctx.beginPath(); ctx.arc(a.headCx + a.headR*0.75, a.headCy - a.headR*0.7, a.headR*0.07, 0, Math.PI*2); ctx.fill();
}

function drawEyes(ctx, a){
  ctx.fillStyle = rgba([45,36,32], 0.92);
  ctx.beginPath(); ctx.ellipse(a.leftEyeX, a.eyeY, a.eyeW, a.eyeH, 0, 0, Math.PI*2); ctx.fill();
  ctx.beginPath(); ctx.ellipse(a.rightEyeX, a.eyeY, a.eyeW, a.eyeH, 0, 0, Math.PI*2); ctx.fill();
  ctx.fillStyle = rgba([254,245,234], 0.85);
  ctx.beginPath(); ctx.ellipse(a.leftEyeX - a.eyeW*0.3, a.eyeY - a.eyeH*0.3, a.eyeW*0.32, a.eyeH*0.32, 0, 0, Math.PI*2); ctx.fill();
  ctx.beginPath(); ctx.ellipse(a.rightEyeX - a.eyeW*0.3, a.eyeY - a.eyeH*0.3, a.eyeW*0.32, a.eyeH*0.32, 0, 0, Math.PI*2); ctx.fill();
  ctx.fillStyle = rgba([240,238,232], 0.5);
  ctx.beginPath(); ctx.arc(a.leftEyeX + a.eyeW*0.25, a.eyeY + a.eyeH*0.2, a.eyeW*0.12, 0, Math.PI*2); ctx.fill();
  ctx.beginPath(); ctx.arc(a.rightEyeX + a.eyeW*0.25, a.eyeY + a.eyeH*0.2, a.eyeW*0.12, 0, Math.PI*2); ctx.fill();
}

function drawBlush(ctx, a){
  const blush = [244,175,195];
  let g1 = ctx.createRadialGradient(a.leftBlushX, a.blushY, 0, a.leftBlushX, a.blushY, a.blushR);
  g1.addColorStop(0, rgba(blush, 0.55));
  g1.addColorStop(1, rgba(blush, 0));
  ctx.fillStyle = g1;
  ctx.beginPath(); ctx.arc(a.leftBlushX, a.blushY, a.blushR, 0, Math.PI*2); ctx.fill();
  let g2 = ctx.createRadialGradient(a.rightBlushX, a.blushY, 0, a.rightBlushX, a.blushY, a.blushR);
  g2.addColorStop(0, rgba(blush, 0.55));
  g2.addColorStop(1, rgba(blush, 0));
  ctx.fillStyle = g2;
  ctx.beginPath(); ctx.arc(a.rightBlushX, a.blushY, a.blushR, 0, Math.PI*2); ctx.fill();
}

function drawSnout(ctx, a, species){
  ctx.fillStyle = rgba([255,143,171], 0.9);
  ctx.beginPath();
  if(species==='cat'){
    ctx.moveTo(a.headCx, a.noseY + a.noseR*0.5);
    ctx.lineTo(a.headCx - a.noseR, a.noseY - a.noseR*0.3);
    ctx.lineTo(a.headCx + a.noseR, a.noseY - a.noseR*0.3);
    ctx.closePath();
  } else {
    ctx.ellipse(a.headCx, a.noseY, a.noseR, a.noseR*0.8, 0, 0, Math.PI*2);
  }
  ctx.fill();
  ctx.strokeStyle = rgba([110,85,70], 0.5);
  ctx.lineWidth = 1.2;
  ctx.lineCap = 'round';
  ctx.beginPath();
  ctx.moveTo(a.headCx, a.noseY + a.noseR*0.6);
  ctx.lineTo(a.headCx, a.mouthY - a.headR*0.05);
  ctx.moveTo(a.headCx, a.mouthY - a.headR*0.05);
  ctx.quadraticCurveTo(a.headCx - a.headR*0.12, a.mouthY + a.headR*0.05, a.headCx - a.headR*0.18, a.mouthY - a.headR*0.02);
  ctx.moveTo(a.headCx, a.mouthY - a.headR*0.05);
  ctx.quadraticCurveTo(a.headCx + a.headR*0.12, a.mouthY + a.headR*0.05, a.headCx + a.headR*0.18, a.mouthY - a.headR*0.02);
  ctx.stroke();
}

function drawPaws(ctx, a, light, mid){
  drawWatercolorBlob(ctx, a.leftPawX, a.pawY, a.pawR, a.pawR, mid, light, mid);
  drawWatercolorBlob(ctx, a.rightPawX, a.pawY, a.pawR, a.pawR, mid, light, mid);
  drawWatercolorBlob(ctx, a.leftFootX, a.footY, a.footR, a.footR, mid, light, mid);
  drawWatercolorBlob(ctx, a.rightFootX, a.footY, a.footR, a.footR, mid, light, mid);
  ctx.fillStyle = rgba([255,190,206], 0.7);
  ctx.beginPath(); ctx.arc(a.leftFootX, a.footY, a.footR*0.35, 0, Math.PI*2); ctx.fill();
  ctx.beginPath(); ctx.arc(a.rightFootX, a.footY, a.footR*0.35, 0, Math.PI*2); ctx.fill();
}

const GOLD = [255, 215, 0], GOLD_HI = [255, 235, 130], GOLD_DK = [212, 160, 23];
const PINK = [255, 107, 157], PINK_HI = [255, 179, 206], PINK_DK = [228, 77, 130];
const RED = [232, 57, 43], RED_HI = [255, 120, 100], RED_DK = [180, 30, 20];

function drawBow(ctx, cx, cy, r){
  drawWatercolorLeaf(ctx, cx - r*0.6, cy, r*0.7, PINK, PINK_HI, PINK_DK, -0.3);
  drawWatercolorLeaf(ctx, cx + r*0.6, cy, r*0.7, PINK, PINK_HI, PINK_DK, 0.3);
  drawWatercolorCircle(ctx, cx, cy, r*0.22, PINK_DK, PINK, PINK_DK);
}
function drawCrown(ctx, cx, cy, r){
  const pts = [[cx-r,cy+r*0.4],[cx-r,cy-r*0.1],[cx-r*0.5,cy+r*0.15],[cx,cy-r*0.5],[cx+r*0.5,cy+r*0.15],[cx+r,cy-r*0.1],[cx+r,cy+r*0.4]];
  let g = ctx.createLinearGradient(cx, cy - r*0.5, cx, cy + r*0.4);
  g.addColorStop(0, rgba(GOLD_HI, 0.95));
  g.addColorStop(0.5, rgba(GOLD, 0.92));
  g.addColorStop(1, rgba(GOLD_DK, 0.85));
  ctx.fillStyle = g;
  ctx.beginPath();
  ctx.moveTo(pts[0][0], pts[0][1]);
  for(let i=1;i<pts.length;i++) ctx.lineTo(pts[i][0], pts[i][1]);
  ctx.closePath(); ctx.fill();
  drawWatercolorCircle(ctx, cx, cy - r*0.2, r*0.1, PINK, PINK_HI, PINK_DK);
  drawWatercolorCircle(ctx, cx - r*0.5, cy + r*0.05, r*0.07, [76,195,247], [180,225,255], [40,150,220]);
  drawWatercolorCircle(ctx, cx + r*0.5, cy + r*0.05, r*0.07, [76,195,247], [180,225,255], [40,150,220]);
}
function drawFlower(ctx, cx, cy, r){
  for(let i=0;i<5;i++){
    const angle = (i*72 - 90) * Math.PI / 180;
    const px = cx + r*0.55*Math.cos(angle);
    const py = cy + r*0.55*Math.sin(angle);
    drawWatercolorCircle(ctx, px, py, r*0.4, PINK, PINK_HI, PINK_DK);
  }
  drawWatercolorCircle(ctx, cx, cy, r*0.3, GOLD, GOLD_HI, GOLD_DK);
}
function drawRoundGlasses(ctx, cx, cy, r){
  ctx.strokeStyle = rgba([45,36,32], 0.85);
  ctx.lineWidth = r*0.08;
  ctx.lineCap = 'round';
  ctx.beginPath(); ctx.arc(cx - r*0.55, cy, r*0.5, 0, Math.PI*2); ctx.stroke();
  ctx.beginPath(); ctx.arc(cx + r*0.55, cy, r*0.5, 0, Math.PI*2); ctx.stroke();
  ctx.beginPath(); ctx.moveTo(cx - r*0.05, cy); ctx.lineTo(cx + r*0.05, cy); ctx.stroke();
  ctx.fillStyle = rgba([180,225,255], 0.18);
  ctx.beginPath(); ctx.arc(cx - r*0.55, cy, r*0.45, 0, Math.PI*2); ctx.fill();
  ctx.beginPath(); ctx.arc(cx + r*0.55, cy, r*0.45, 0, Math.PI*2); ctx.fill();
}
function drawBellCollar(ctx, cx, cy, r){
  ctx.strokeStyle = rgba(GOLD_DK, 0.85);
  ctx.lineWidth = r*0.1;
  ctx.lineCap = 'round';
  ctx.beginPath(); ctx.moveTo(cx - r, cy); ctx.lineTo(cx + r, cy); ctx.stroke();
  drawWatercolorCircle(ctx, cx, cy + r*0.25, r*0.3, GOLD, GOLD_HI, GOLD_DK);
  ctx.strokeStyle = rgba(GOLD_DK, 0.7);
  ctx.lineWidth = r*0.04;
  ctx.beginPath(); ctx.moveTo(cx - r*0.2, cy + r*0.25); ctx.lineTo(cx + r*0.2, cy + r*0.25); ctx.stroke();
}
function drawScarf(ctx, cx, cy, r){
  let g = ctx.createLinearGradient(cx, cy - r*0.3, cx, cy + r*0.5);
  g.addColorStop(0, rgba(RED_HI, 0.92));
  g.addColorStop(0.5, rgba(RED, 0.9));
  g.addColorStop(1, rgba(RED_DK, 0.82));
  ctx.fillStyle = g;
  ctx.beginPath();
  ctx.moveTo(cx - r, cy - r*0.3);
  ctx.lineTo(cx + r, cy - r*0.3);
  ctx.lineTo(cx + r*0.8, cy + r*0.5);
  ctx.lineTo(cx + r*0.3, cy + r*0.3);
  ctx.lineTo(cx - r*0.3, cy + r*0.5);
  ctx.lineTo(cx - r*0.8, cy + r*0.3);
  ctx.closePath(); ctx.fill();
  ctx.strokeStyle = rgba(RED_DK, 0.4);
  ctx.lineWidth = r*0.04;
  for(let i=-2;i<=2;i++){
    ctx.beginPath();
    ctx.moveTo(cx + i*r*0.15, cy + r*0.3);
    ctx.lineTo(cx + i*r*0.15, cy + r*0.48);
    ctx.stroke();
  }
}
function drawStar(ctx, cx, cy, r){
  let g = ctx.createRadialGradient(cx, cy, 0, cx, cy, r);
  g.addColorStop(0, rgba(GOLD_HI, 0.95));
  g.addColorStop(0.6, rgba(GOLD, 0.92));
  g.addColorStop(1, rgba(GOLD_DK, 0.82));
  ctx.fillStyle = g;
  ctx.beginPath();
  for(let i=0;i<=10;i++){
    const angle = (i*36 - 90) * Math.PI / 180;
    const radius = i%2===0 ? r : r*0.4;
    const x = cx + radius*Math.cos(angle);
    const y = cy + radius*Math.sin(angle);
    if(i===0) ctx.moveTo(x,y); else ctx.lineTo(x,y);
  }
  ctx.closePath(); ctx.fill();
}

function drawPet(ctx, ox, oy, species, color, outfits){
  ctx.save();
  ctx.translate(ox, oy);
  const a = anchors(W, H);
  const base = shade(color, 1.0);
  const dark = shade(color, 0.80);
  const mid = base;
  const light = shade(color, 1.12);
  const lighter = shade(color, 1.24);
  const outline = [110,85,70];

  let gsh = ctx.createRadialGradient(a.bodyCx, a.bodyBottom+4, 0, a.bodyCx, a.bodyBottom+4, a.bodyRx*1.5);
  gsh.addColorStop(0, rgba(dark, 0.22));
  gsh.addColorStop(1, rgba(dark, 0));
  ctx.fillStyle = gsh;
  ctx.beginPath();
  ctx.ellipse(a.bodyCx, a.bodyBottom+2, a.bodyRx*1.25, a.bodyRy*0.3, 0, 0, Math.PI*2);
  ctx.fill();

  drawWatercolorBlob(ctx, a.bodyCx, a.bodyCy, a.bodyRx, a.bodyRy, mid, lighter, dark);
  drawSoftOutline(ctx, a.bodyCx, a.bodyCy, a.bodyRx*0.985, a.bodyRy*0.985, outline, 1.0, 0.35);

  if(species==='dog') drawDogEars(ctx, a, base, dark, light, lighter);
  if(species==='hamster') drawHamsterEars(ctx, a, base, dark, light, lighter);
  if(species==='cat') drawCatEars(ctx, a, base, dark, light, lighter);

  drawWatercolorBlob(ctx, a.headCx, a.headCy, a.headR, a.headR, mid, lighter, dark);
  drawSoftOutline(ctx, a.headCx, a.headCy, a.headR*0.985, a.headR*0.985, outline, 0.9, 0.35);

  if(species==='rabbit') drawRabbitEars(ctx, a, base, dark, light, lighter);

  drawPaws(ctx, a, light, mid);
  drawBlush(ctx, a);
  drawEyes(ctx, a);
  drawSnout(ctx, a, species);

  outfits.forEach(o => {
    const r = a.headR;
    if(o==='head_bow') drawBow(ctx, a.headCx, a.headTopY - r*0.1, r*0.55);
    if(o==='head_crown') drawCrown(ctx, a.headCx, a.headTopY - r*0.05, r*0.6);
    if(o==='head_flower') drawFlower(ctx, a.headCx, a.headTopY - r*0.05, r*0.5);
    if(o==='glasses_round') drawRoundGlasses(ctx, a.headCx, a.eyeY, r*0.55);
    if(o==='collar_bell') drawBellCollar(ctx, a.headCx, a.collarY, r*0.6);
    if(o==='cloth_scarf') drawScarf(ctx, a.headCx, a.clothingY, r*0.75);
    if(o==='tail_star') drawStar(ctx, a.bodyRight + r*0.15, a.bodyBottom - r*0.25, r*0.4);
  });

  ctx.restore();
}

const cases = [
  {sp:'cat',     color:'#FF8FAB', outfits:['head_crown','glasses_round','collar_bell'], label:'猫咪 · 暖粉', sub:'皇冠 + 圆框眼镜 + 铃铛项圈'},
  {sp:'dog',     color:'#FFD4A8', outfits:['cloth_scarf','head_bow'],                   label:'小狗 · 蜜桃', sub:'围巾 + 蝴蝶结'},
  {sp:'rabbit',  color:'#A8D8FF', outfits:['head_flower','glasses_round','tail_star'],  label:'兔子 · 天蓝', sub:'花朵 + 圆框眼镜 + 星星尾饰'},
  {sp:'hamster', color:'#B8F2D8', outfits:['head_bow','collar_bell','cloth_scarf'],     label:'仓鼠 · 薄荷', sub:'蝴蝶结 + 铃铛 + 围巾'}
];

const totalW = COLS * W + (COLS+1) * PADDING;
const totalH = HEADER_H + H + LABEL_H + PADDING*2;
const canvas = createCanvas(totalW, totalH);
const ctx = canvas.getContext('2d');

// 背景
let bg = ctx.createLinearGradient(0, 0, 0, totalH);
bg.addColorStop(0, '#fbf5ef');
bg.addColorStop(1, '#f3e9de');
ctx.fillStyle = bg;
ctx.fillRect(0, 0, totalW, totalH);

// 标题
ctx.fillStyle = '#7a5a42';
ctx.font = '20px sans-serif';
ctx.textAlign = 'center';
ctx.fillText('方案 D · 矢量水彩扁平风（本体 + 装饰风格统一）', totalW/2, 30);
ctx.fillStyle = '#a0866a';
ctx.font = '12px sans-serif';
ctx.fillText('本体与装饰同为水彩扁平插画 —— 柔和渐变 / 半透明边缘 / 统一锚点定位', totalW/2, 50);

cases.forEach((c, i) => {
  const x = PADDING + i * (W + PADDING);
  const y = HEADER_H + PADDING;
  // 卡片背景
  ctx.fillStyle = 'rgba(255,255,255,0.55)';
  ctx.beginPath();
  // roundRect
  const rr = 14;
  ctx.moveTo(x+rr, y);
  ctx.lineTo(x+W-rr, y);
  ctx.quadraticCurveTo(x+W, y, x+W, y+rr);
  ctx.lineTo(x+W, y+H+LABEL_H-rr);
  ctx.quadraticCurveTo(x+W, y+H+LABEL_H, x+W-rr, y+H+LABEL_H);
  ctx.lineTo(x+rr, y+H+LABEL_H);
  ctx.quadraticCurveTo(x, y+H+LABEL_H, x, y+H+LABEL_H-rr);
  ctx.lineTo(x, y+rr);
  ctx.quadraticCurveTo(x, y, x+rr, y);
  ctx.closePath();
  ctx.fill();

  drawPet(ctx, x, y, c.sp, c.color, c.outfits);

  // 标签
  ctx.fillStyle = '#8a6a52';
  ctx.font = 'bold 13px sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText(c.label, x + W/2, y + H + 18);
  ctx.fillStyle = '#b8987a';
  ctx.font = '11px sans-serif';
  ctx.fillText(c.sub, x + W/2, y + H + 34);
});

const out = '/workspace/docs/pet_preview_plan_d.png';
fs.writeFileSync(out, canvas.toBuffer('image/png'));
console.log('Saved:', out);
console.log('Size:', totalW + 'x' + totalH);

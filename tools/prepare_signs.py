"""Recorta las láminas del alfabeto (marco morado y etiqueta inferior) y las
convierte en máscaras alfa para poder teñirlas con el color del tema en la app.

Uso: python3 tools/prepare_signs.py <carpeta con A.png ... Ñ.png>
"""
import sys
from pathlib import Path

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "app" / "src" / "main" / "assets" / "signos"
SIZE = 360


def process(path: Path) -> Image.Image:
    a = np.array(Image.open(path).convert("RGB")).astype(int)
    r, g, b = a[..., 0], a[..., 1], a[..., 2]
    h, w = r.shape
    purple = (b - np.maximum(r, g)) > 30
    ys, xs = np.where(purple)
    dark = (r + g + b) < 300
    label_rows = [y for y in range(int(h * 0.7), h) if dark[y, int(w * 0.2):int(w * 0.8)].sum() > w * 0.25]
    x0, x1 = xs.min() + 14, xs.max() - 14
    y0, y1 = ys.min() + 14, label_rows[0] - 4
    gray = a[y0:y1, x0:x1].mean(axis=2)
    alpha = np.clip((235 - gray) * 255 / 200, 0, 255).astype(np.uint8)
    yy, xx = np.where(alpha > 20)
    alpha = alpha[yy.min():yy.max() + 1, xx.min():xx.max() + 1]
    ch, cw = alpha.shape
    side = int(max(ch, cw) * 1.08)
    canvas = np.zeros((side, side), dtype=np.uint8)
    oy, ox = (side - ch) // 2, (side - cw) // 2
    canvas[oy:oy + ch, ox:ox + cw] = alpha
    mask = Image.fromarray(canvas).resize((SIZE, SIZE), Image.LANCZOS)
    rgba = Image.new("RGBA", mask.size, (255, 255, 255, 0))
    rgba.putalpha(mask)
    return rgba


if __name__ == "__main__":
    src = Path(sys.argv[1])
    OUT.mkdir(parents=True, exist_ok=True)
    for p in sorted(src.glob("*.png")):
        name = "N_TILDE" if p.stem == "Ñ" else p.stem
        process(p).save(OUT / f"{name}.png", optimize=True)
        print("ok", name)

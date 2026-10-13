"""Convierte el ExtraTreesClassifier (.pkl) a un binario compacto que la app lee
sin depender de Python, y genera vectores de prueba para verificar en Kotlin que
las predicciones del móvil son idénticas a las de scikit-learn.

Uso: python3 tools/export_model.py
"""
import json
import struct
import warnings
from math import acos, atan2, degrees
from pathlib import Path

import joblib
import numpy as np

warnings.filterwarnings("ignore")
ROOT = Path(__file__).resolve().parent.parent
PKL = ROOT / "tools" / "extra_trees_final.pkl"
OUT_MODEL = ROOT / "app" / "src" / "main" / "assets" / "extra_trees.bin"
OUT_TESTS = ROOT / "app" / "src" / "test" / "resources" / "vectors.json"


def export(model):
    classes = [str(c) for c in model.classes_]
    n_classes = len(classes)
    buf = bytearray()
    buf += b"ETRF"
    buf += struct.pack("<iiii", 1, model.n_features_in_, n_classes, len(model.estimators_))
    for c in classes:
        raw = c.encode("utf-8")
        buf += struct.pack("<B", len(raw)) + raw
    for est in model.estimators_:
        t = est.tree_
        n = t.node_count
        is_leaf = t.children_left == -1
        leaf_ids = np.cumsum(is_leaf) - 1
        # En las hojas, left = -(índice de hoja + 1); right no se usa.
        left = np.where(is_leaf, -(leaf_ids + 1), t.children_left).astype("<i4")
        right = t.children_right.astype("<i4")
        feature = np.where(is_leaf, -1, t.feature).astype("<i2")
        threshold = t.threshold.astype("<f8")
        values = t.value[is_leaf, 0, :].astype(np.float64)
        sums = values.sum(axis=1, keepdims=True)
        sums[sums == 0] = 1.0
        proba = (values / sums).astype("<f4")
        buf += struct.pack("<ii", n, int(is_leaf.sum()))
        buf += left.tobytes() + right.tobytes() + feature.tobytes() + threshold.tobytes()
        buf += proba.tobytes()
    OUT_MODEL.write_bytes(bytes(buf))
    return classes


def features_reference(coords):
    """Copia literal del cálculo de ángulos de TraductorEstatico/app/app.py."""
    coordenadas = [np.array(c) for c in coords]
    angulos = []
    for i in range(21):
        if i == 0 or i in [4, 8, 12, 16, 20]:
            a = b = c = 0
        elif i in [5, 9, 13, 17]:
            a = np.linalg.norm(coordenadas[0] - coordenadas[i + 1])
            b = np.linalg.norm(coordenadas[0] - coordenadas[i])
            c = np.linalg.norm(coordenadas[i + 1] - coordenadas[i])
        else:
            a = np.linalg.norm(coordenadas[i - 1] - coordenadas[i + 1])
            b = np.linalg.norm(coordenadas[i - 1] - coordenadas[i])
            c = np.linalg.norm(coordenadas[i + 1] - coordenadas[i])
        angulo = 0
        if a != 0 and b != 0 and c != 0:
            cos = (b**2 + c**2 - a**2) / (2 * b * c)
            if -1 < cos < 1:
                angulo = round(degrees(abs(acos(cos))))
            angulos.append(angulo)
    x = coords[9][0] - coords[0][0]
    y = (1 - coords[9][1]) - (1 - coords[0][1])
    g = degrees(atan2(y, x))
    if g < 0:
        g += 360
    angulos.append(g)
    return angulos


def make_tests(model, classes, n=400):
    rng = np.random.default_rng(7)
    cases = []
    for _ in range(n):
        # Mano sintética: muñeca + 5 dedos de 4 articulaciones con giros aleatorios.
        base = rng.uniform(0.3, 0.7, size=2)
        rot = rng.uniform(0, 2 * np.pi)
        pts = [[base[0], base[1], 0.0]]
        for f in range(5):
            ang = rot + (f - 2) * 0.35 + rng.normal(0, 0.1)
            p = np.array([base[0], base[1], 0.0])
            for j in range(4):
                ang += rng.uniform(-0.9, 0.9) * (j > 0)
                step = rng.uniform(0.02, 0.06)
                p = p + np.array([np.cos(ang) * step, -np.sin(ang) * step, rng.normal(0, 0.01)])
                pts.append(p.tolist())
        feats = features_reference(pts)
        case = {"landmarks": pts, "features": [float(v) for v in feats]}
        if len(feats) == 16:
            X = np.array(feats, dtype=np.float64).reshape(1, -1)
            proba = model.predict_proba(X)[0]
            case["label"] = str(model.predict(X)[0])
            case["proba"] = [float(v) for v in proba]
        cases.append(case)
    OUT_TESTS.write_text(json.dumps({"classes": classes, "cases": cases}))
    return cases


if __name__ == "__main__":
    m = joblib.load(PKL)
    cls = export(m)
    cases = make_tests(m, cls)
    labels = sorted({c.get("label") for c in cases if "label" in c})
    print(f"modelo: {OUT_MODEL.stat().st_size / 1e6:.2f} MB, clases={cls}")
    print(f"vectores de prueba: {len(cases)}, letras cubiertas: {labels}")

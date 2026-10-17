"""Comprueba métricas, particiones y predicciones contra la ejecución publicada."""
import argparse
from pathlib import Path
import numpy as np
import pandas as pd

root = Path(__file__).resolve().parent
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--run-dir', type=Path, default=root/'runs/latest')
args = parser.parse_args()
actual = args.run_dir/'results'
reference = root/'results'
a = pd.read_csv(actual/'comparison.csv').set_index('model').sort_index()
b = pd.read_csv(reference/'comparison.csv').set_index('model').sort_index()
assert list(a.index) == list(b.index), 'Faltan algoritmos'
metrics = [c for c in b.columns if c not in ('fit_seconds', 'batch_ms_per_sample')]
np.testing.assert_allclose(a[metrics], b[metrics], rtol=0, atol=1e-12)
for name in ('decision_tree', 'extra_trees', 'knn', 'logistic_regression', 'random_forest', 'svm_rbf'):
    pd.testing.assert_frame_equal(pd.read_csv(actual/f'predictions_{name}.csv', keep_default_na=False),
                                  pd.read_csv(reference/f'predictions_{name}.csv', keep_default_na=False))
with np.load(actual/'splits.npz') as x, np.load(reference/'splits.npz') as y:
    assert set(x.files) == set(y.files)
    for key in x.files:
        np.testing.assert_array_equal(x[key], y[key])
    assert not np.intersect1d(x['random_train'], x['random_test']).size
    assert len(x['random_train']) == 2000 and len(x['random_test']) == 500
np.testing.assert_array_equal(np.load(actual/'confusion_extra_trees.npy'),
                              np.load(reference/'confusion_extra_trees.npy'))
print('OK: seis modelos, métricas, predicciones, particiones y matriz reproducidos.')
print('Los tiempos no se comparan: dependen del equipo y su carga.')

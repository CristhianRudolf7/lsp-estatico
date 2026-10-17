"""Exporta el modelo reentrenado con el formato binario existente; no reemplaza producción."""
from pathlib import Path
import argparse,importlib.util,json,joblib,numpy as np,pandas as pd
root=Path(__file__).resolve().parent
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--run-dir',type=Path,default=root/'runs/latest')
parser.add_argument('--data',type=Path,default=root/'data/csv-angles-static/df.csv')
args=parser.parse_args(); run=args.run_dir.resolve()
spec=importlib.util.spec_from_file_location('export_model',root.parent/'tools/export_model.py');mod=importlib.util.module_from_spec(spec);spec.loader.exec_module(mod)
m=joblib.load(run/'models/extra_trees_retrained.pkl')
mod.OUT_MODEL=run/'models/extra_trees_retrained.bin';mod.OUT_TESTS=run/'results/parity_vectors.json'
classes=mod.export(m);mod.make_tests(m,classes,n=400)
# Compara también el modelo previo para verificar si esta tabla explica su entrenamiento.
import warnings
with warnings.catch_warnings():
 warnings.simplefilter('ignore');old=joblib.load(root.parent/'tools/extra_trees_final.pkl')
d=pd.read_csv(args.data,keep_default_na=False);X=d.drop(columns='Letra').to_numpy(float)
identical=0
for a,b in zip(old.estimators_,m.estimators_):
 ta,tb=a.tree_,b.tree_
 if all(np.array_equal(getattr(ta,k),getattr(tb,k)) for k in ['children_left','children_right','feature','threshold','value']):identical+=1
report={'classes_match':classes==[str(c) for c in old.classes_],'features':m.n_features_in_,'trees':len(m.estimators_),'identical_trees_to_original':identical,'original_accuracy_on_complete_csv':float(np.mean(old.predict(X)==d.Letra)),'retrained_accuracy_on_complete_csv':float(np.mean(m.predict(X)==d.Letra)),'binary_bytes':mod.OUT_MODEL.stat().st_size,'note':'Exactitud sobre la tabla completa no es una evaluación independiente.'}
(run/'results/model_compatibility.json').write_text(json.dumps(report,indent=2))
print(json.dumps(report,indent=2))

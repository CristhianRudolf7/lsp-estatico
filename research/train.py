"""Experimento reproducible en CPU con el CSV angular publicado. No modifica modelos de producción."""
import os
for name in ('OMP_NUM_THREADS','OPENBLAS_NUM_THREADS','MKL_NUM_THREADS'):
    os.environ[name]='1'
import argparse,json,hashlib,time,platform,sys,warnings
from pathlib import Path
from datetime import datetime
from zoneinfo import ZoneInfo
import numpy as np
import pandas as pd
import joblib,sklearn
from sklearn.base import clone
from sklearn.model_selection import train_test_split,StratifiedKFold,cross_validate
from sklearn.metrics import accuracy_score,precision_recall_fscore_support,confusion_matrix,classification_report,matthews_corrcoef
from sklearn.ensemble import ExtraTreesClassifier,RandomForestClassifier
from sklearn.tree import DecisionTreeClassifier
from sklearn.neighbors import KNeighborsClassifier
from sklearn.svm import SVC
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import StandardScaler
from threadpoolctl import threadpool_limits
ROOT=Path(__file__).resolve().parent
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--data',type=Path,default=ROOT/'data/csv-angles-static/df.csv')
parser.add_argument('--output-dir',type=Path,default=ROOT/'runs/latest')
args=parser.parse_args()
source=args.data.resolve(); OUT=args.output_dir.resolve()
for folder in ('models','results'):
    (OUT/folder).mkdir(parents=True,exist_ok=True)
expected='021c16456ee1a9930721d34222461bc19da5d876f6e255a3392686e356482f1a'
if hashlib.sha256(source.read_bytes()).hexdigest()!=expected:
    raise ValueError('El CSV no coincide con el SHA-256 del experimento publicado.')
cols=['1','2','3','5','6','7','9','10','11','13','14','15','17','18','19','angulo']
df=pd.read_csv(source,keep_default_na=False)
assert list(df.columns)==['Letra']+cols
X=df[cols].to_numpy(dtype=float);y=df.Letra.to_numpy();labels=np.unique(y)
assert X.shape==(2500,16) and len(labels)==25 and ' ' in labels
assert np.isfinite(X).all() and not df.duplicated().any()
assert ((X[:,:15]>=0)&(X[:,:15]<=180)).all() and ((X[:,15]>=0)&(X[:,15]<360)).all()
assert np.array_equal(np.unique(y,return_counts=True)[1],np.full(25,100))
idx=np.arange(len(y));tr,te=train_test_split(idx,test_size=.2,random_state=42,stratify=y)
# Prueba adicional: últimos 20 registros de cada clase; no se afirma que sean otra persona/sesión.
blocked_te=np.concatenate([np.flatnonzero(y==c)[-20:] for c in labels]);blocked_tr=np.setdiff1d(idx,blocked_te)
np.savez(OUT/'results/splits.npz',random_train=tr,random_test=te,ordered_train=blocked_tr,ordered_test=blocked_te)
models={
 'Extra Trees':ExtraTreesClassifier(n_estimators=100,max_features='sqrt',bootstrap=False,random_state=42,n_jobs=1),
 'Random Forest':RandomForestClassifier(n_estimators=100,max_features='sqrt',random_state=42,n_jobs=1),
 'Árbol de decisión':DecisionTreeClassifier(random_state=42),
 'KNN':make_pipeline(StandardScaler(),KNeighborsClassifier(n_neighbors=5,n_jobs=1)),
 'SVM RBF':make_pipeline(StandardScaler(),SVC(C=1,gamma='scale')),
 'Regresión logística':make_pipeline(StandardScaler(),LogisticRegression(C=1,max_iter=3000,random_state=42)),
}
rows=[];cv=StratifiedKFold(n_splits=5,shuffle=True,random_state=42)
with threadpool_limits(limits=1):
 for name,est in models.items():
  print('Entrenando:',name,flush=True)
  cvres=cross_validate(est,X[tr],y[tr],cv=cv,scoring={'accuracy':'accuracy','f1':'f1_macro'},n_jobs=1,error_score='raise')
  start=time.perf_counter();m=clone(est).fit(X[tr],y[tr]);fit=time.perf_counter()-start
  # Repetición por lotes para evitar afirmar latencia de cámara.
  m.predict(X[te]);start=time.perf_counter()
  for _ in range(10):pred=m.predict(X[te])
  infer=(time.perf_counter()-start)/10/len(te)*1000
  precision,recall,f1,_=precision_recall_fscore_support(y[te],pred,average='macro',zero_division=0)
  b=clone(est).fit(X[blocked_tr],y[blocked_tr]);bp=b.predict(X[blocked_te]);bf=precision_recall_fscore_support(y[blocked_te],bp,average='macro',zero_division=0)[2]
  row={'model':name,'cv_accuracy':float(cvres['test_accuracy'].mean()),'cv_sd':float(cvres['test_accuracy'].std(ddof=1)),'cv_f1':float(cvres['test_f1'].mean()),'accuracy':float(accuracy_score(y[te],pred)),'precision_macro':float(precision),'recall_macro':float(recall),'f1_macro':float(f1),'mcc':float(matthews_corrcoef(y[te],pred)),'fit_seconds':fit,'batch_ms_per_sample':infer,'ordered_accuracy':float(accuracy_score(y[blocked_te],bp)),'ordered_f1':float(bf),'correct':int(np.sum(pred==y[te])),'test_size':len(te)}
  rows.append(row);print(json.dumps(row,ensure_ascii=False),flush=True)
  slug={'Extra Trees':'extra_trees','Random Forest':'random_forest','Árbol de decisión':'decision_tree','KNN':'knn','SVM RBF':'svm_rbf','Regresión logística':'logistic_regression'}[name]
  joblib.dump(m,OUT/f'models/{slug}_evaluation.pkl')
  pd.DataFrame({'row':te,'truth':y[te],'prediction':pred}).to_csv(OUT/f'results/predictions_{slug}.csv',index=False)
  if name=='Extra Trees':
   cm=confusion_matrix(y[te],pred,labels=labels);np.save(OUT/'results/confusion_extra_trees.npy',cm)
   (OUT/'results/per_class_extra_trees.json').write_text(json.dumps(classification_report(y[te],pred,labels=labels,output_dict=True,zero_division=0),indent=2))
   pd.DataFrame({'feature':cols,'importance':m.feature_importances_}).to_csv(OUT/'results/feature_importances.csv',index=False)
   rng=np.random.default_rng(42);correct=(pred==y[te]).astype(float);boots=np.array([correct[rng.integers(0,len(te),len(te))].mean() for _ in range(10000)])
   ci=np.quantile(boots,[.025,.975]).tolist()
 # Reajuste final independiente de la tabla de evaluación: 2500 filas.
 final=clone(models['Extra Trees']).fit(X,y)
 joblib.dump(final,OUT/'models/extra_trees_retrained.pkl')
 pd.DataFrame(rows).to_csv(OUT/'results/comparison.csv',index=False)
 meta={'date_lima':datetime.now(ZoneInfo('America/Lima')).isoformat(),'dataset':'cristhianegoavil/csv-angles-static','second_dataset':'cristhianegoavil/21-puntos-csv-estatico','sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'rows':len(df),'features':cols,'labels':labels.tolist(),'counts':{c:int(np.sum(y==c)) for c in labels},'train_size':len(tr),'test_size':len(te),'seed':42,'cv_folds':5,'extra_trees_bootstrap95_accuracy':ci,'python':platform.python_version(),'sklearn':sklearn.__version__,'numpy':np.__version__,'pandas':pd.__version__,'cpu_count':os.cpu_count(),'platform':platform.platform(),'models':{k:str(v) for k,v in models.items()},'production_model_unchanged':True}
 (OUT/'results/metadata.json').write_text(json.dumps(meta,indent=2,ensure_ascii=False))
 print('Experimento completo; modelo final con 2500 filas.',flush=True)

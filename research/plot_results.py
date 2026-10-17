from pathlib import Path
import json, numpy as np, pandas as pd
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import argparse
parser=argparse.ArgumentParser(description='Regenera las dos figuras desde los resultados de una ejecución.')
parser.add_argument('--run-dir',type=Path,default=Path(__file__).resolve().parent/'runs/latest')
R=parser.parse_args().run_dir.resolve();P=R/'figures';P.mkdir(parents=True,exist_ok=True)
m=json.loads((R/'results/metadata.json').read_text());df=pd.read_csv(R/'results/comparison.csv');df=df.set_index('model').loc[['Árbol de decisión','Extra Trees','KNN','Regresión logística','Random Forest','SVM RBF']].reset_index()
plt.rcParams.update({'font.family':'DejaVu Sans','font.size':10,'axes.spines.top':False,'axes.spines.right':False,'pdf.fonttype':42})
fig,ax=plt.subplots(figsize=(7,3.3));x=np.arange(len(df));ax.bar(x-.17,df.accuracy*100,.34,label='Aleatoria estratificada',color='#4b6380');ax.bar(x+.17,df.ordered_accuracy*100,.34,label='Orden conservado por clase',color='#b3bcc6');ax.set_xticks(x,['Árbol','Extra\nTrees','KNN','Regresión\nlogística','Random\nForest','SVM\nRBF']);ax.set_ylim(80,100);ax.set_ylabel('Exactitud (%)');ax.legend(frameon=False,fontsize=9,loc='lower center',bbox_to_anchor=(.5,1.01),ncol=2);ax.grid(axis='y',alpha=.15);ax.set_axisbelow(True);fig.tight_layout();fig.savefig(P/'comparison.pdf');plt.close(fig)
cm=np.load(R/'results/confusion_extra_trees.npy');fig,ax=plt.subplots(figsize=(6.4,5.6));im=ax.imshow(cm,cmap='Blues',vmin=0,vmax=20);labels=['Esp.' if c==' ' else c for c in m['labels']];ax.set_xticks(range(25),labels,rotation=90,fontsize=8);ax.set_yticks(range(25),labels,fontsize=8);ax.set_xlabel('Predicción');ax.set_ylabel('Etiqueta real');
for i in range(25):
 for j in range(25):
  if cm[i,j]>0:ax.text(j,i,str(cm[i,j]),ha='center',va='center',fontsize=8,color='white' if cm[i,j]>=12 else '#303030')
fig.colorbar(im,ax=ax,fraction=.035,pad=.03,label='Número de registros');fig.tight_layout();fig.savefig(P/'confusion.pdf');plt.close(fig)

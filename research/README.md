# Experimento LSP estático en CPU

Material completo de reproducción del [artículo CREH Labs](../docs/investigacion/articulo-creh-labs.pdf): datos, entrenamiento, resultados por algoritmo, modelos, exportación y comprobaciones. El [LaTeX y sus recursos](../docs/investigacion/) se mantienen en una sola carpeta; allí está también la cita bibliográfica.

## Reproducir desde una copia nueva

Requisitos: Python **3.13** (ejecución original: 3.13.5), `venv` y pip. Para comprobar el lector web: Node.js y npm (verificado con Node.js **26.6.0**). Los paquetes Python se fijan en `requirements.txt`; no se necesita cámara, Kaggle, GPU ni credenciales.

Desde la raíz del repositorio:

```bash
python3.13 -m venv research/.venv
source research/.venv/bin/activate
python -m pip install -r research/requirements.txt
python research/train.py
python research/export.py
python research/verify_results.py
python research/plot_results.py
npm --prefix web ci
cd web
npx tsx scripts/research-parity.ts
cd ..
```

Las nuevas salidas se guardan en `research/runs/latest/{models,results,figures}`. No se sobrescriben las salidas publicadas en `research/models` y `research/results`, ni los modelos de Android o web. Repetir la orden sí reemplaza la ejecución local `latest`; se puede elegir otra carpeta:

```bash
python research/train.py --output-dir research/runs/mi-ejecucion
python research/export.py --run-dir research/runs/mi-ejecucion
python research/verify_results.py --run-dir research/runs/mi-ejecucion
python research/plot_results.py --run-dir research/runs/mi-ejecucion
cd web
npx tsx scripts/research-parity.ts ../research/runs/mi-ejecucion
```

La comprobación debe reproducir las predicciones y métricas publicadas de los seis algoritmos. Los tiempos, fecha y datos del equipo variarán. La prueba TypeScript verifica 400 vectores sintéticos, con tolerancia de `1e-6` en probabilidades: no representa 400 personas ni fotografías.

Para comprobar solo los artefactos incluidos, sin entrenar:

```bash
cd web
npm ci
npx tsx scripts/research-parity.ts ../research
```

Para verificar la integridad de los datos, modelos y resultados históricos desde la raíz:

```bash
sha256sum -c research/ARTIFACTS.sha256
```

## Organización

| Ruta | Contenido |
|---|---|
| `data/` | Una copia del CSV, procedencia, esquema y SHA-256. |
| `train.py` | Entrenamiento, validación cruzada, pruebas, bootstrap y ajuste final. |
| `export.py` | Exportación binaria y comprobación con el modelo original. |
| `verify_results.py` | Comparación contra las métricas, particiones y predicciones publicadas. |
| `plot_results.py` | Regeneración de comparación de exactitud y matriz de confusión. |
| `models/*_evaluation.pkl` | Seis modelos ajustados con 2000 registros. |
| `models/extra_trees_retrained.*` | Modelo final ajustado con los 2500 registros, en joblib y binario. |
| `results/` | Ejecución histórica del 13 de octubre de 2026, hora de Perú. |
| `sources/` | Extracto histórico de captura; no necesario para repetir el entrenamiento angular. |
| `runs/` | Ejecuciones locales, excluidas de Git. |

Los `.pkl` se serializan con joblib; se cargan con las versiones fijadas. `metadata.json` registra versiones, hash, etiquetas, semilla y parámetros. `comparison.csv` contiene métricas y tiempos; `predictions_*.csv` permiten auditarlas por fila; `splits.npz` conserva índices de entrenamiento/prueba aleatorios y por orden. `feature_importances.csv`, la matriz y el informe por clase amplían el análisis de Extra Trees. `model_compatibility.json` describe la relación con el modelo original; sus exactitudes sobre todo el CSV no son una validación independiente.

## Verificación del paquete publicado

El [registro de verificación](verification.json) documenta una ejecución completa en una copia aislada de los archivos del repositorio: seis modelos entrenados, métricas y predicciones iguales a las publicadas, figuras regeneradas y 400/400 casos de paridad tanto para el modelo recién exportado como para el artefacto histórico. Las dependencias web se instalaron mediante `npm ci`; las versiones Python instaladas coincidían con `requirements.txt`. No se creó un entorno Python nuevo durante esa comprobación.

## Protocolo y alcance

- 2500 filas, 16 atributos y 25 clases balanceadas (24 letras y espacio).
- Separación 80/20 estratificada, semilla 42: 2000 ejemplos para entrenamiento, 500 para prueba.
- Validación cruzada estratificada de cinco particiones exclusivamente dentro del entrenamiento.
- Extra Trees, Random Forest, árbol de decisión, KNN, SVM radial y regresión logística.
- StandardScaler ajustado dentro de cada partición para KNN, SVM y logística.
- Prueba adicional con los primeros 80 y últimos 20 registros de cada clase; no equivale a separar personas o sesiones.
- Ajuste final de Extra Trees sobre los 2500 registros, separado de la evaluación sobre los 500 reservados.
- CPU con un hilo; costes de inferencia por lote, sin captura ni MediaPipe. Los tiempos no son latencia de cámara.

Extra Trees alcanza 97,80 % de exactitud y 97,78 % de F1 macro en la prueba aleatoria. La dependencia entre capturas no puede descartarse: el bootstrap no la corrige. No hay evaluación poblacional, por personas nuevas ni de conversación continua.

La web y Android conservan el modelo original de `tools/extra_trees_final.pkl`. El modelo de esta investigación no se instala automáticamente en producción.

## Artículo y captura histórica

Para compilar el artículo, véanse las [instrucciones de LaTeX](../docs/investigacion/README.md#archivos-y-compilación). `plot_results.py` regenera las figuras, pero no reescribe el artículo histórico ni sus cifras: una nueva campaña requiere revisar expresamente el documento.

El [extracto de captura](sources/captura_original.py) documenta el procedimiento histórico: 21 puntos, recorte con margen de 25 % del mayor lado y resolución 224 × 224. Se conserva sin reinterpretarlo como una nueva campaña de captura. Usa la API histórica `mediapipe.solutions` y OpenCV; no forma parte del entorno de entrenamiento ni se garantiza compatibilidad con las versiones actuales de MediaPipe. Tampoco establece correspondencia imagen por imagen con el CSV.

# Datos del experimento

`csv-angles-static/df.csv` es la copia empleada en el estudio, publicada por Cristhian Egoavil en [Kaggle](https://www.kaggle.com/datasets/cristhianegoavil/csv-angles-static) e incluida aquí para reproducirlo sin credenciales ni conexión a Kaggle.

- SHA-256: `021c16456ee1a9930721d34222461bc19da5d876f6e255a3392686e356482f1a`.
- 2500 registros, 16 características numéricas y columna objetivo `Letra`.
- 100 registros por clase: 24 letras estáticas y espacio. El espacio es una etiqueta literal ` `; debe preservarse al leer el CSV.
- Las columnas `1,2,3,5,6,7,9,10,11,13,14,15,17,18,19` representan 15 ángulos; `angulo`, la orientación planar.
- No incluye imágenes, identificadores de personas ni sesiones. No permite una prueba por participantes.

La copia del dataset `21-puntos-csv-estatico` tiene el mismo hash y no se duplica ni se suma al conjunto. No se ha demostrado identidad con el archivo histórico `df_final.csv` del modelo original de producción.

El entrenamiento verifica el hash antes de ejecutarse. Para usar otros datos debe definirse otro experimento, sin atribuirle las métricas publicadas.

# Señas web

Publicación: https://senas.crehlabs.com

React, TypeScript, Vite y Tailwind CSS. Versión web del traductor Android, sin login ni backend de inferencia.

## Ejecutar

```bash
npm ci
npm run dev
npm test
npm run build
```

La cámara requiere HTTPS o localhost. Los modelos y WebAssembly se sirven desde el propio sitio. MediaPipe Hand Landmarker funciona en CPU dentro de un Web Worker clásico; `scripts/build-worker.mjs` empaqueta ese trabajador y copia los archivos WASM de la versión instalada.

Se reutilizan `extra_trees.bin` y `hand_landmarker.task` de Android. Las 27 láminas se recuperaron de `CristhianRudolf7/TraductorEstatico/app/static/signos/`: las láminas de Android son máscaras blancas con transparencia para colorearlas con el tema; la web usa las ilustraciones originales completas. El clasificador JavaScript reproduce las 16 características, el redondeo al par y la conversión float32 de scikit-learn. `npm test` compara sus características, probabilidades y etiquetas con los 400 vectores de referencia de Android.

## Funciones

Tres interfaces: Traductor, Alfabeto y Texto a señas. La última permite escribir una frase y consultar su deletreo mediante las ilustraciones del alfabeto, con reproducción, pausa, navegación y tiempo por seña configurable. Conserva Ñ y espacios, normaliza vocales acentuadas y señala caracteres sin ilustración.

Cámara frontal/trasera, esqueleto de mano, letra y confianza, alternativas, estabilización (6 de 8 cuadros y confianza media ≥45 %), voz de letras y mensaje, edición/copia/borrado de frases y alfabeto con imágenes. Retira la mano para repetir una misma letra.

El modelo reconoce el alfabeto estático; J, Ñ y Z requieren movimiento y están disponibles solo como referencia. No interpreta frases completas de lengua de señas. La voz usa las voces en español disponibles en el navegador/dispositivo.

## Verificación

- `npm run test:text`: tildes, Ñ, Unicode, espacios y letras repetidas.
- `npm run test:ui:text`: tercera interfaz en escritorio/móvil, secuencia y reproducción/pausa.
- `npm test`: 400/400 casos equivalentes al modelo Python.
- `node scripts/browser-check.mjs`: controles, cámara simulada con carga real de MediaPipe CPU, 27 referencias y ausencia de desbordamiento en 390px.
- Las pruebas de navegador usan Chromium del sistema (`/usr/bin/chromium`); las capturas quedan en `test-results/`.
- `node scripts/permission-check.mjs`: permiso denegado con mensaje de error y opción de reintento.
- La cámara simulada comprueba carga y procesamiento; la precisión con personas reales debe verificarse con señas reales y buena luz.

## Publicación

Proyecto Vercel `senas-web`, equipo `cristhianr7s-projects`. Vercel compila `web/` con Vite desde el código de GitHub y publica `dist/` como archivos estáticos, junto con las cabeceras de `vercel.json`. No hay GPU alquilada, funciones de servidor ni servicios de inferencia externos.

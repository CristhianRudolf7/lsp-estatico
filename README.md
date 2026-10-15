# LSP estático — Señas

Traductor del alfabeto dactilológico estático de la lengua de señas peruana, disponible como aplicación web y aplicación Android. Sin login; el reconocimiento se ejecuta en el dispositivo.

**Web:** [senas.crehlabs.com](https://senas.crehlabs.com)  
**Android:** [Descargar APK desde Releases](https://github.com/CristhianRudolf7/lsp-estatico/releases/latest)

## Investigación y artículo científico

**Toda la investigación está detallada en el [artículo completo de CREH Labs (PDF)](docs/investigacion/articulo-creh-labs.pdf).** Allí se explican la adquisición de datos, las características geométricas de la mano, el entrenamiento y comparación de seis algoritmos de ML, el protocolo experimental, los resultados y sus limitaciones.

El [fuente LaTeX y los recursos de compilación](docs/investigacion/) están incluidos en este repositorio. Consulta también las [instrucciones de compilación y la cita bibliográfica/BibTeX](docs/investigacion/README.md#cómo-citar).

> Egoavil, C. (2026). *Algoritmos de ML para reconocer el alfabeto estático de la lengua de señas peruana*. CREH Labs. [Artículo](docs/investigacion/articulo-creh-labs.pdf).

La comparación experimental y las aplicaciones publicadas tienen alcances distintos: las métricas del artículo corresponden al conjunto evaluado; no constituyen una medición del reconocimiento con usuarios de la web o Android. Las aplicaciones conservan el modelo original descrito abajo.

## Contenido

| Ruta | Contenido |
|---|---|
| `docs/investigacion/` | Artículo CREH Labs en PDF, fuente LaTeX, plantilla, gráficos y cita bibliográfica. |
| `web/` | React, TypeScript, Vite y Tailwind CSS. Versión publicada en Vercel. |
| `app/` | Android con Kotlin, Jetpack Compose, CameraX y lectura por voz. |
| `tools/` | Modelo original `.pkl`, exportación del clasificador y preparación de láminas. |
| `gradle/`, `gradlew` | Herramientas de compilación de Android. |

## Algoritmo

MediaPipe identifica 21 puntos de una mano. Se calculan 15 ángulos de articulaciones y un ángulo de orientación; esas 16 características entran en un clasificador Extra Trees de 100 árboles. Reconoce 24 letras estáticas y espacio. J, Ñ y Z requieren movimiento y están disponibles como referencia.

El modelo proviene de [TraductorEstatico](https://github.com/CristhianRudolf7/TraductorEstatico), archivo `app/extra_trees_final.pkl`. Android y la web leen una exportación binaria del mismo modelo; no se reentrenó para estas aplicaciones.

El porcentaje mostrado es la confianza del clasificador, no una medición de precisión de una persona en tiempo real. La aplicación reconoce letras del alfabeto; no interpreta la gramática ni frases completas de lengua de señas.

## Web

```bash
cd web
npm ci
npm run dev
npm test
npm run build
```

La cámara requiere HTTPS o localhost. `npm run build` genera `web/dist/` y prepara el trabajador de MediaPipe y sus archivos WASM. Los modelos se sirven desde el propio sitio; los cuadros de cámara se procesan localmente. No requiere un servidor de inferencia ni GPU alquilada.

Ambas aplicaciones tienen tres pestañas: **Traductor**, **Alfabeto** y **Texto a señas**. En la tercera puedes escribir hasta 280 caracteres, consultar la secuencia completa y reproducirla con pausa, avance manual y velocidad configurable. Conserva los espacios y la Ñ, convierte vocales con tilde a su letra base e indica las letras con movimiento y los caracteres sin ilustración. Esta función representa el deletreo dactilológico de la frase, no una traducción de la gramática de la LSP. Funciona localmente y no requiere cámara.

La web incluye cambio de cámara, letra/confianza y alternativas, estabilización, voz, edición y copia de mensajes, y 27 ilustraciones del alfabeto. Retira la mano entre dos letras iguales para repetirlas.

Los detalles están en [web/README.md](web/README.md). La publicación actual se registra en `web/deployment.json`.

## Android

Requisitos: JDK 17, Android SDK con plataforma 36 y las dependencias de Gradle. Crea `local.properties` con `sdk.dir` apuntando a tu SDK local, o configura `ANDROID_HOME`.

```bash
./gradlew :app:testDebugUnitTest :app:assembleRelease
```

El APK queda en `app/build/outputs/apk/release/app-release.apk`. La configuración actual firma con una clave de depuración para instalación directa; una publicación en tienda requiere una clave de release propia.

La versión Android 1.1.0 añade la interfaz Texto a señas. Las láminas de Android son máscaras blancas con transparencia que la interfaz colorea; la web usa las ilustraciones originales completas.

## Verificación

- Web y Android: equivalencia de características, probabilidades y etiquetas con 400 vectores sintéticos del modelo Python.
- Web: interfaz de escritorio/móvil, controles del mensaje, alfabeto y cámara simulada con carga real de MediaPipe CPU.
- Dominio público: HTTPS, carga del motor y manejo de permiso de cámara denegado verificados.

Los 400 vectores comprueban equivalencia entre implementaciones; no representan una evaluación de precisión con 400 personas o fotografías reales. La experiencia con señas reales depende de la iluminación, la orientación y la calidad de la cámara.

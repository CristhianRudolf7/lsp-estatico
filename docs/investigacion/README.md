# Investigación · CREH Labs

**Algoritmos de ML para reconocer el alfabeto estático de la lengua de señas peruana**  
Cristhian Egoavil · 13 de octubre de 2026 · Español · Preprint sin revisión por pares declarada.

- [Leer el artículo completo en PDF](articulo-creh-labs.pdf).
- [Consultar el fuente LaTeX](article.tex).

El artículo detalla la adquisición histórica de imágenes, la representación de 21 puntos de la mano mediante 16 características, los seis algoritmos comparados, el protocolo de evaluación, las métricas, los costes computacionales, la discusión y las limitaciones. Es la referencia principal para comprender la investigación que acompaña a este proyecto.

La comparación usa 2500 registros y 25 clases; reserva 500 registros para prueba. Extra Trees obtiene el mejor desempeño observado en ese conjunto: 97,80 % de exactitud y 97,78 % de F1 macro. Estas métricas no acreditan generalización a personas nuevas ni representan una evaluación de las aplicaciones publicadas. Las aplicaciones conservan el modelo original; el modelo seleccionado en el estudio es un artefacto independiente.

## Archivos y compilación

Se incluyen la plantilla `creh-article.sty`, el isotipo de CREH Labs y los gráficos `comparison.pdf` y `confusion.pdf`, necesarios para compilar el fuente. El PDF publicado conserva el artículo inicial, sin incorporar el informe universitario ni el CV.

Requisitos: pdfLaTeX (TeX Live), soporte de español y los paquetes declarados en `creh-article.sty`, incluidos lmodern, microtype, fancyhdr, booktabs e hyperref.

```bash
cd docs/investigacion
mkdir -p build
pdflatex -interaction=nonstopmode -halt-on-error -output-directory=build article.tex
pdflatex -interaction=nonstopmode -halt-on-error -output-directory=build article.tex
```

El resultado es `build/article.pdf`. El PDF de consulta del repositorio se llama `articulo-creh-labs.pdf`.

## Cómo citar

Egoavil, C. (2026). *Algoritmos de ML para reconocer el alfabeto estático de la lengua de señas peruana*. CREH Labs. https://github.com/CristhianRudolf7/lsp-estatico/blob/main/docs/investigacion/articulo-creh-labs.pdf

```bibtex
@techreport{egoavil2026lsp,
  author      = {Egoavil, Cristhian},
  title       = {Algoritmos de ML para reconocer el alfabeto estático de la lengua de señas peruana},
  institution = {CREH Labs},
  year        = {2026},
  month       = oct,
  type        = {Preprint},
  url         = {https://github.com/CristhianRudolf7/lsp-estatico/blob/main/docs/investigacion/articulo-creh-labs.pdf}
}
```

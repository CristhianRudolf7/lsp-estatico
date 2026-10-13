import {build} from 'esbuild';
import {mkdir,copyFile,readdir} from 'node:fs/promises';
await mkdir('public/wasm',{recursive:true});
for(const name of await readdir('node_modules/@mediapipe/tasks-vision/wasm'))await copyFile(`node_modules/@mediapipe/tasks-vision/wasm/${name}`,`public/wasm/${name}`);
await build({entryPoints:['src/vision.worker.ts'],outfile:'public/vision-worker.js',bundle:true,format:'iife',platform:'browser',target:'es2022',minify:true});

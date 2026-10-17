import {readFileSync} from 'node:fs';
import assert from 'node:assert/strict';
import {fileURLToPath} from 'node:url';
import {resolve} from 'node:path';
const run=process.argv[2] ? resolve(process.argv[2]) : fileURLToPath(new URL('../../research/runs/latest/', import.meta.url));
import {ExtraTrees,features} from '../src/model.ts';
const b=readFileSync(resolve(run,'models/extra_trees_retrained.bin'));
const m=new ExtraTrees(b.buffer.slice(b.byteOffset,b.byteOffset+b.byteLength));
const data=JSON.parse(readFileSync(resolve(run,'results/parity_vectors.json'),'utf8'));
assert.deepEqual(m.classes,data.classes);
for(const c of data.cases){const f=features(c.landmarks);assert.ok(f);for(let i=0;i<16;i++)assert.ok(Math.abs(f[i]-c.features[i])<1e-9);const p=m.predict(f);for(let i=0;i<p.length;i++)assert.ok(Math.abs(p[i]-c.proba[i])<=1e-6);assert.equal(m.classes[p.indexOf(Math.max(...p))],c.label);}
console.log('Modelo reentrenado compatible con el lector web: 400/400 casos.');

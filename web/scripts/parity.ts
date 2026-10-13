import {readFileSync} from 'node:fs';
import assert from 'node:assert/strict';
import {ExtraTrees,features,Stabilizer} from '../src/model.ts';
const bytes=readFileSync('public/models/extra_trees.bin');const model=new ExtraTrees(bytes.buffer.slice(bytes.byteOffset,bytes.byteOffset+bytes.byteLength));
const data=JSON.parse(readFileSync('../app/src/test/resources/vectors.json','utf8'));
assert.deepEqual(model.classes,data.classes);let count=0;
for(const c of data.cases){const x=features(c.landmarks);if(c.features.length!==16){assert.equal(x,null);continue;}assert.ok(x);for(let k=0;k<16;k++)assert.ok(Math.abs(x[k]-c.features[k])<=1e-9,`Rasgo ${k}`);const p=model.predict(x);for(let i=0;i<p.length;i++)assert.ok(Math.abs(p[i]-c.proba[i])<=1e-6,`Probabilidad ${i}`);assert.equal(model.classes[p.indexOf(Math.max(...p))],c.label);count++;}
const s=new Stabilizer();for(let i=0;i<5;i++)assert.equal(s.push('A',0.9),null);assert.equal(s.push('A',0.9),'A');s.reset();assert.equal(s.push('B',0.9),null);
console.log(`Modelo web: ${count}/${data.cases.length} casos equivalentes a Python; estabilización verificada.`);

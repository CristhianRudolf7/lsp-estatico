export type Point = { x:number; y:number; z:number };
const evenRound = (n:number) => { const f=Math.floor(n); return n-f===0.5 ? (f%2===0?f:f+1) : Math.round(n); };
export function features(points: number[][]):number[]|null {
  if(points.length!==21)return null;
  const dist=(p:number[],q:number[])=>Math.hypot(p[0]-q[0],p[1]-q[1],p[2]-q[2]);
  const out:number[]=[];
  for(let i=1;i<21;i++){
    if([4,8,12,16,20].includes(i))continue;
    const prev=[5,9,13,17].includes(i)?points[0]:points[i-1];
    const a=dist(prev,points[i+1]),b=dist(prev,points[i]),c=dist(points[i+1],points[i]);
    if(a!==0&&b!==0&&c!==0){const cos=(b*b+c*c-a*a)/(2*b*c);out.push(cos>-1&&cos<1?evenRound(Math.acos(cos)*180/Math.PI):0);}
  }
  let angle=Math.atan2(points[0][1]-points[9][1],points[9][0]-points[0][0])*180/Math.PI;
  if(angle<0)angle+=360;out.push(angle);
  return out.length===16?out:null;
}
export function reference(points:Point[],w:number,h:number){return points.map(p=>[p.x*w/640,p.y*h/480,p.z*w/640]);}
type Tree={left:Int32Array;right:Int32Array;feature:Int16Array;threshold:Float64Array;proba:Float32Array};
export class ExtraTrees {
  classes:string[]=[];trees:Tree[]=[];nFeatures=0;
  constructor(buffer:ArrayBuffer){
    const v=new DataView(buffer);let o=0;
    const int=()=>{const n=v.getInt32(o,true);o+=4;return n;};
    if(new TextDecoder().decode(new Uint8Array(buffer,0,4))!=='ETRF')throw Error('Modelo inválido');o=4;
    if(int()!==1)throw Error('Versión de modelo inválida');
    this.nFeatures=int();const nc=int(),nt=int();
    for(let c=0;c<nc;c++){const len=v.getUint8(o++);this.classes.push(new TextDecoder().decode(new Uint8Array(buffer,o,len)));o+=len;}
    for(let t=0;t<nt;t++){
      const n=int(),l=int();const left=new Int32Array(n),right=new Int32Array(n),feature=new Int16Array(n),threshold=new Float64Array(n),proba=new Float32Array(l*nc);
      for(let i=0;i<n;i++)left[i]=int();for(let i=0;i<n;i++)right[i]=int();
      for(let i=0;i<n;i++){feature[i]=v.getInt16(o,true);o+=2;}
      for(let i=0;i<n;i++){threshold[i]=v.getFloat64(o,true);o+=8;}
      for(let i=0;i<proba.length;i++){proba[i]=v.getFloat32(o,true);o+=4;}
      this.trees.push({left,right,feature,threshold,proba});
    }
    if(o!==buffer.byteLength)throw Error('Tamaño de modelo inválido');
  }
  predict(x:number[]):number[]{
    if(x.length!==this.nFeatures)throw Error('Características inválidas');
    const xf=x.map(Math.fround),out=Array(this.classes.length).fill(0);
    for(const t of this.trees){let n=0;while(t.feature[n]>=0)n=xf[t.feature[n]]<=t.threshold[n]?t.left[n]:t.right[n];const offset=(-t.left[n]-1)*out.length;for(let c=0;c<out.length;c++)out[c]+=t.proba[offset+c];}
    return out.map(v=>v/this.trees.length);
  }
}
export class Stabilizer {
  history:{label:string;confidence:number}[]=[];
  reset(){this.history=[];}
  push(label:string,confidence:number){this.history.push({label,confidence});if(this.history.length>8)this.history.shift();const votes=this.history.filter(x=>x.label===label);return votes.length>=6&&votes.reduce((s,x)=>s+x.confidence,0)/votes.length>=0.45?label:null;}
}

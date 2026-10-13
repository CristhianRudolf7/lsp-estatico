import {useEffect,useRef,useState} from 'react';
import {sayLetter} from './alphabet';
import type {Point} from './model';
export type Guess={label:string;confidence:number};
export type Result={points:Point[]|null;guess:Guess|null;stable:string|null;alternatives:Guess[]};
const empty:Result={points:null,guess:null,stable:null,alternatives:[]};
const edges=[[0,1],[1,2],[2,3],[3,4],[0,5],[5,6],[6,7],[7,8],[5,9],[9,10],[10,11],[11,12],[9,13],[13,14],[14,15],[15,16],[13,17],[0,17],[17,18],[18,19],[19,20]];
export function useTranslator(){
  const video=useRef<HTMLVideoElement>(null),canvas=useRef<HTMLCanvasElement>(null);
  const bootTimer=useRef<ReturnType<typeof setTimeout>|null>(null);
  const stream=useRef<MediaStream|null>(null),worker=useRef<Worker|null>(null),raf=useRef(0),generation=useRef(0),voiceRef=useRef(true),lastLetter=useRef<string|null>(null);
  const [status,setStatus]=useState<'idle'|'loading'|'running'|'error'>('idle');const [error,setError]=useState('');const [front,setFront]=useState(true);const [voice,setVoice]=useState(true);const [result,setResult]=useState<Result>(empty);const [phrase,setPhrase]=useState('');
  function release(){if(bootTimer.current)clearTimeout(bootTimer.current);bootTimer.current=null;generation.current++;cancelAnimationFrame(raf.current);stream.current?.getTracks().forEach(t=>t.stop());stream.current=null;worker.current?.terminate();worker.current=null;if(video.current)video.current.srcObject=null;lastLetter.current=null;const c=canvas.current;c?.getContext('2d')?.clearRect(0,0,c.width,c.height);}
  useEffect(()=>()=>{release();if('speechSynthesis' in window)window.speechSynthesis.cancel();},[]);
  function stop(){release();setStatus('idle');setResult(empty);if('speechSynthesis' in window)window.speechSynthesis.cancel();}
  function draw(points:Point[]|null){const c=canvas.current,v=video.current;if(!c||!v)return;c.width=v.videoWidth;c.height=v.videoHeight;const ctx=c.getContext('2d');if(!ctx)return;ctx.clearRect(0,0,c.width,c.height);if(!points)return;ctx.lineWidth=3;ctx.strokeStyle='#b79aff';ctx.shadowColor='#9473ed';ctx.shadowBlur=8;for(const [a,b] of edges){ctx.beginPath();ctx.moveTo(points[a].x*c.width,points[a].y*c.height);ctx.lineTo(points[b].x*c.width,points[b].y*c.height);ctx.stroke();}for(const p of points){ctx.beginPath();ctx.arc(p.x*c.width,p.y*c.height,4,0,Math.PI*2);ctx.fillStyle='#81e8c1';ctx.fill();}}
  async function start(useFront=front){
    release();const id=generation.current;setStatus('loading');setError('');setResult(empty);setFront(useFront);
    try{
      if(!navigator.mediaDevices?.getUserMedia)throw Error('Abre esta página con HTTPS en un navegador compatible para usar la cámara.');
      const media=await navigator.mediaDevices.getUserMedia({audio:false,video:{facingMode:{ideal:useFront?'user':'environment'},width:{ideal:640},height:{ideal:480},aspectRatio:{ideal:4/3}}});
      if(id!==generation.current){media.getTracks().forEach(t=>t.stop());return;}stream.current=media;const v=video.current;if(!v)throw Error('No se pudo preparar la cámara');v.srcObject=media;await v.play();if(id!==generation.current)return;
      const w=new Worker('/vision-worker.js');worker.current=w;let busy=false,lastTime=0,lastVideo=-1;
      const frame=async(time:number)=>{
        if(id!==generation.current)return;
        raf.current=requestAnimationFrame(frame);
        if(busy||time-lastTime<85||v.readyState<2||v.currentTime===lastVideo||document.hidden)return;
        busy=true;lastTime=time;lastVideo=v.currentTime;
        try{const image=await createImageBitmap(v,{resizeWidth:640,resizeHeight:Math.round(640*v.videoHeight/v.videoWidth)});if(id!==generation.current){image.close();return;}w.postMessage({type:'frame',image,time},[image]);}catch(e){fail(e);}
      };
      const fail=(e:unknown)=>{if(id!==generation.current)return;release();setStatus('error');setResult(empty);setError(e instanceof Error?e.message:String(e));};
      bootTimer.current=setTimeout(()=>fail(Error('La carga tardó demasiado. Revisa tu conexión y vuelve a intentarlo.')),60000);
      w.onerror=()=>fail(Error('No se pudo iniciar el reconocimiento. Recarga la página e inténtalo otra vez.'));
      w.onmessage=(event)=>{
        if(id!==generation.current)return;const d=event.data;
        if(d.type==='ready'){if(bootTimer.current)clearTimeout(bootTimer.current);bootTimer.current=null;setStatus('running');raf.current=requestAnimationFrame(frame);}
        if(d.type==='error')fail(Error(d.message));
        if(d.type==='result'){busy=false;setResult(d);draw(d.points);if(!d.points)lastLetter.current=null;if(d.stable&&d.stable!==lastLetter.current){lastLetter.current=d.stable;setPhrase(p=>d.stable===' '?(p.endsWith(' ')?p:p+' '):p+d.stable);if(voiceRef.current)sayLetter(d.stable);}}
      };
      w.postMessage({type:'init',base:new URL('/',window.location.href).href});
    }catch(e){if(id!==generation.current)return;release();setStatus('error');setResult(empty);const name=e instanceof DOMException?e.name:'';setError(name==='NotAllowedError'?'El permiso de cámara está bloqueado. Permite el acceso desde el candado de tu navegador y vuelve a intentarlo.':name==='NotFoundError'?'No encontramos una cámara en este dispositivo. Conecta una cámara y vuelve a intentarlo.':name==='NotReadableError'?'Otra aplicación está usando la cámara. Ciérrala y vuelve a intentarlo.':e instanceof Error?e.message:String(e));}
  }
  function toggleVoice(){voiceRef.current=!voiceRef.current;setVoice(voiceRef.current);if(!voiceRef.current&&'speechSynthesis' in window)window.speechSynthesis.cancel();}
  function flip(){const next=!front;if(status==='running'||status==='loading')void start(next);else setFront(next);}
  return {video,canvas,status,error,front,voice,result,phrase,setPhrase,start,stop,flip,toggleVoice};
}

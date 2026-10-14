import {useEffect,useState} from 'react';
import {ChevronLeft,ChevronRight,Hand,Pause,Play,RotateCcw,Trash2} from 'lucide-react';
import {textSigns} from './textSigns';
export default function TextSigns(){
 const [text,setText]=useState('');
 const [index,setIndex]=useState(0);
 const [playing,setPlaying]=useState(false);
 const [delay,setDelay]=useState(1500);
 const tokens=textSigns(text),current=tokens[index];
 const unsupported=Array.from(new Set(tokens.filter(t=>!t.sign&&t.letter!==' ').map(t=>t.letter)));
 useEffect(()=>{
  if(!playing)return;
  const timer=window.setTimeout(()=>{
   if(index>=tokens.length-1)setPlaying(false);else setIndex(i=>i+1);
  },delay);
  return ()=>window.clearTimeout(timer);
 },[playing,index,tokens.length,delay]);
 function edit(value:string){setText(value);setIndex(0);setPlaying(false);}
 function step(next:number){setPlaying(false);setIndex(next);}
 return <section className="text-signs-page">
  <div className="section-heading"><div><p className="eyebrow">DEL TEXTO A TUS MANOS</p><h1>Escribe. Mira. <em>Deletrea.</em></h1><p className="muted">Escribe una frase y recorre sus señas en el alfabeto dactilológico peruano.</p></div></div>
  <div className="text-signs-workspace"><section className="phrase-panel">
   <label htmlFor="sign-text" className="eyebrow">TU FRASE</label>
   <textarea id="sign-text" maxLength={280} value={text} onChange={e=>edit(e.target.value)} placeholder="Por ejemplo: Hola, Perú"/>
   <div className="panel-title"><span className="character-count">{text.length}/280 caracteres</span><button className="secondary" disabled={!text} onClick={()=>edit('')}><Trash2 size={16}/> Limpiar</button></div>
   <p className="muted text-signs-explanation">Mostramos el deletreo letra por letra. La LSP tiene su propia gramática y esta guía no traduce frases completas a esa gramática. Las vocales con tilde usan su letra base y la Ñ conserva su seña.</p>
   {unsupported.length>0?<p className="alphabet-note" role="status">Sin seña disponible: {unsupported.join(' ')}. Se muestran como pausas de referencia.</p>:null}
   <label className="speed-control">Tiempo por seña<select value={delay} onChange={e=>setDelay(Number(e.target.value))}><option value={1000}>1 segundo</option><option value={1500}>1,5 segundos</option><option value={2500}>2,5 segundos</option></select></label>
  </section><section className="sign-player" aria-label="Reproductor de señas">
   <div className="panel-title"><span className="eyebrow">SEÑA ACTUAL</span><span className="character-count">{tokens.length?`${index+1} de ${tokens.length}`:'Escribe para empezar'}</span></div>
   <div className="sign-stage" key={index}>
    {current?.sign?<img src={`/signos/${current.sign.asset}.png`} alt={`Seña de la letra ${current.letter}`}/>:<div className="sign-placeholder"><Hand size={64}/><strong>{current?(current.letter===' '?'Espacio entre palabras':current.letter):'Tu frase, en señas'}</strong><span>{current?.letter===' '?'Haz una pausa antes de la siguiente palabra.':current?'Carácter sin seña en este alfabeto.':'Cada letra aparecerá aquí.'}</span></div>}
   </div>
   <p className="current-sign" aria-live={playing?'off':'polite'}>{current?.sign?`Letra ${current.letter}`:current?.letter===' '?'Espacio':current?'Pausa de referencia':'Listo para comenzar'}</p>
   {current?.sign?.dynamic?<p className="movement-note">{current.letter} requiere movimiento. Sigue la trayectoria de la ilustración.</p>:null}
   <div className="sign-controls"><button className="secondary" aria-label="Seña anterior" disabled={!current||index===0} onClick={()=>step(index-1)}><ChevronLeft size={20}/></button><button className="primary" disabled={!tokens.length} onClick={()=>{if(!playing&&index===tokens.length-1)setIndex(0);setPlaying(p=>!p);}}>{playing?<Pause size={18}/>:<Play size={18}/>} {playing?'Pausar':'Reproducir'}</button><button className="secondary" aria-label="Seña siguiente" disabled={!current||index===tokens.length-1} onClick={()=>step(index+1)}><ChevronRight size={20}/></button><button className="secondary" aria-label="Volver al inicio" disabled={!tokens.length} onClick={()=>step(0)}><RotateCcw size={17}/></button></div>
  </section></div>
  {tokens.length?<section className="phrase-sequence" aria-label="Secuencia completa"><h2>Tu frase, paso a paso</h2><div className="sequence-grid">{tokens.map((token,i)=><button key={i} className={`sequence-sign ${i===index?'sequence-active':''}`} aria-label={`Paso ${i+1}: ${token.letter===' '?'espacio':token.letter}`} aria-pressed={i===index} onClick={()=>step(i)}>{token.sign?<img loading="lazy" src={`/signos/${token.sign.asset}.png`} alt={`Seña de ${token.letter}`}/>:<span className="sequence-pause">{token.letter===' '?'␣':token.letter}</span>}<strong>{token.letter===' '?'Espacio':token.letter}</strong>{token.sign?.dynamic?<small>Movimiento</small>:null}</button>)}</div></section>:null}
 </section>;
}

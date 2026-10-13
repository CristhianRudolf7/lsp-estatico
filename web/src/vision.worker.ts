import { FilesetResolver, HandLandmarker } from '@mediapipe/tasks-vision';
import {ExtraTrees, features, reference, Stabilizer} from './model';
let detector:HandLandmarker;let model:ExtraTrees;const stable=new Stabilizer();
self.onmessage=async(event:MessageEvent)=>{
  const data=event.data;
  if(data.type==='init'){
    try{
      const [vision,buf]=await Promise.all([FilesetResolver.forVisionTasks(`${data.base}wasm`),fetch(`${data.base}models/extra_trees.bin`).then(r=>{if(!r.ok)throw Error('No se pudo descargar el modelo');return r.arrayBuffer();})]);
      model=new ExtraTrees(buf);
      detector=await HandLandmarker.createFromOptions(vision,{baseOptions:{modelAssetPath:`${data.base}models/hand_landmarker.task`,delegate:'CPU'},runningMode:'VIDEO',numHands:1,minHandDetectionConfidence:0.5,minHandPresenceConfidence:0.5,minTrackingConfidence:0.5});
      self.postMessage({type:'ready'});
    }catch(e){self.postMessage({type:'error',message:e instanceof Error?e.message:String(e)});}
  }
  if(data.type==='reset'){stable.reset();return;}
  if(data.type==='frame'){
    const image=data.image as ImageBitmap;
    try{
      const result=detector.detectForVideo(image,data.time);const points=result.landmarks[0];
      if(!points){stable.reset();self.postMessage({type:'result',points:null,guess:null,stable:null,alternatives:[]});return;}
      const x=features(reference(points,image.width,image.height));
      if(!x){stable.reset();self.postMessage({type:'result',points,guess:null,stable:null,alternatives:[]});return;}
      const proba=model.predict(x);const ranked=proba.map((confidence,i)=>({label:model.classes[i],confidence})).sort((a,b)=>b.confidence-a.confidence);
      self.postMessage({type:'result',points,guess:ranked[0],stable:stable.push(ranked[0].label,ranked[0].confidence),alternatives:ranked.slice(1,3).filter(x=>x.confidence>=0.05)});
    }catch(e){self.postMessage({type:'error',message:e instanceof Error?e.message:String(e)});}finally{image.close();}
  }
};

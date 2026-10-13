import {useEffect} from 'react';
import type {KeyboardEvent} from 'react';
export function useDialogFocus(open:boolean){
 useEffect(()=>{if(!open)return;const previous=document.activeElement as HTMLElement|null;const overflow=document.body.style.overflow;document.body.style.overflow='hidden';return()=>{document.body.style.overflow=overflow;previous?.focus();};},[open]);
}
export function dialogKeys(event:KeyboardEvent<HTMLDivElement>,close:()=>void){
 if(event.key==='Escape'){close();return;}
 if(event.key!=='Tab')return;
 const items=event.currentTarget.querySelectorAll<HTMLElement>('button:not(:disabled),a[href],input,textarea,[tabindex="0"]');
 const first=items[0],last=items[items.length-1];if(!first)return;
 if(event.shiftKey&&document.activeElement===first){event.preventDefault();last.focus();}
 else if(!event.shiftKey&&document.activeElement===last){event.preventDefault();first.focus();}
}

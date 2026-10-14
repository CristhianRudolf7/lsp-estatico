import {alphabet} from './alphabet';
const byLetter=new Map(alphabet.map(item=>[item.letter,item]));
const accents:Record<string,string>={'Á':'A','É':'E','Í':'I','Ó':'O','Ú':'U','Ü':'U'};
export function textSigns(text:string){
 return Array.from(text.normalize('NFC').toUpperCase(),char=>{
  const letter=accents[char]??char;
  return {letter:/\s/u.test(letter)?' ':letter,sign:byLetter.get(letter)};
 });
}

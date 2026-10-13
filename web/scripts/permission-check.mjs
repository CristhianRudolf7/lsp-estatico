import {chromium} from '@playwright/test';
const browser=await chromium.launch({executablePath:'/usr/bin/chromium',args:['--no-sandbox','--use-fake-device-for-media-stream']});
try{
const context=await browser.newContext();const page=await context.newPage();const cdp=await context.newCDPSession(page);
const origin=process.env.TEST_URL??'http://localhost:5173/';
await cdp.send('Browser.setPermission',{permission:{name:'camera'},setting:'denied',origin:new URL(origin).origin});
await page.goto(origin);await page.getByRole('button',{name:'Activar cámara',exact:true}).click();await page.getByText('El permiso de cámara está bloqueado.',{exact:false}).waitFor();await page.getByRole('button',{name:'Reintentar cámara'}).waitFor();console.log('Permiso denegado: error comprensible y reintento disponibles.');
}finally{await browser.close();}

export function statusLabel(status:string):string{if(status==='UP')return 'Backend conectado';if(status==='DOWN')return 'Backend indisponível';return 'Verificando backend';}

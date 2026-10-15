export type SystemInfo={name:string;version:string;status:string};
export async function fetchSystemInfo(signal?:AbortSignal):Promise<SystemInfo>{const response=await fetch('/api/v1/system/info',{signal});if(!response.ok)throw new Error(`Falha ao consultar backend: HTTP ${response.status}`);return response.json() as Promise<SystemInfo>;}

import { statusLabel } from './statusLabel';
function assertEqual(actual:string,expected:string):void{if(actual!==expected)throw new Error(`Expected ${expected}, got ${actual}`)}
assertEqual(statusLabel('UP'),'Backend conectado');assertEqual(statusLabel('DOWN'),'Backend indisponível');assertEqual(statusLabel('UNKNOWN'),'Verificando backend');console.log('STATUS_LABEL_TEST_OK');

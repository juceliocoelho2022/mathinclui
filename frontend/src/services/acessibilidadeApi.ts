import { api } from './authApi';

export type TamanhoFonte = 'PADRAO' | 'GRANDE' | 'EXTRA_GRANDE';
export type NivelApoio = 'PADRAO' | 'MODERADO' | 'AMPLIADO';

export type ConfiguracaoAcessibilidade = {
  tamanhoFonte: TamanhoFonte;
  leituraVoz: boolean;
  apoioVisual: boolean;
  dicasPassoAPasso: boolean;
  repeticaoGuiada: boolean;
  questoesPorTela: number;
  tempoAmpliado: boolean;
  nivelApoio: NivelApoio;
};

export type PreferenciaInterface = {
  tamanhoFonte: TamanhoFonte;
  leituraVoz: boolean;
  apoioVisual: boolean;
};

export type AdaptacaoPayload = {
  tamanhoFonteMinima: TamanhoFonte | null;
  leituraVozMinima: boolean | null;
  apoioVisualMinimo: boolean | null;
  dicasPassoAPasso: boolean | null;
  repeticaoGuiada: boolean | null;
  questoesPorTela: number | null;
  tempoAmpliado: boolean | null;
  nivelApoio: NivelApoio | null;
};

export type AdaptacaoResponse = AdaptacaoPayload & {
  atribuicaoId: number;
  alunoId: number | null;
};

export const adaptacaoVazia = (): AdaptacaoPayload => ({
  tamanhoFonteMinima: null,
  leituraVozMinima: null,
  apoioVisualMinimo: null,
  dicasPassoAPasso: null,
  repeticaoGuiada: null,
  questoesPorTela: null,
  tempoAmpliado: null,
  nivelApoio: null,
});

export function temAdaptacao(payload: AdaptacaoPayload) {
  return Object.values(payload).some((value) => value !== null);
}

export async function buscarAdaptacaoGeral(atribuicaoId: number) {
  return (await api.get<AdaptacaoResponse>(`/atribuicoes/${atribuicaoId}/adaptacao`)).data;
}

export async function salvarAdaptacaoGeral(atribuicaoId: number, payload: AdaptacaoPayload) {
  return (await api.put<AdaptacaoResponse>(`/atribuicoes/${atribuicaoId}/adaptacao`, payload)).data;
}

export async function buscarAdaptacaoIndividual(atribuicaoId: number, alunoId: number) {
  return (await api.get<AdaptacaoResponse>(`/atribuicoes/${atribuicaoId}/alunos/${alunoId}/adaptacao`)).data;
}

export async function salvarAdaptacaoIndividual(atribuicaoId: number, alunoId: number, payload: AdaptacaoPayload) {
  return (await api.put<AdaptacaoResponse>(`/atribuicoes/${atribuicaoId}/alunos/${alunoId}/adaptacao`, payload)).data;
}

export async function excluirAdaptacaoIndividual(atribuicaoId: number, alunoId: number) {
  await api.delete(`/atribuicoes/${atribuicaoId}/alunos/${alunoId}/adaptacao`);
}

export async function buscarPreferenciasInterface() {
  return (await api.get<PreferenciaInterface>('/aluno/preferencias-interface')).data;
}

export async function salvarPreferenciasInterface(payload: PreferenciaInterface) {
  return (await api.put<PreferenciaInterface>('/aluno/preferencias-interface', payload)).data;
}

export async function buscarAcessibilidadeAtribuicao(atribuicaoId: number) {
  return (await api.get<ConfiguracaoAcessibilidade>(`/aluno/atribuicoes/${atribuicaoId}/acessibilidade`)).data;
}

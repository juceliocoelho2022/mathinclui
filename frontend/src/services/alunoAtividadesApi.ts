import { api } from './authApi';
import type { PerfilPedagogico } from './gestaoEscolarApi';
import type { StatusTentativa, TipoQuestao } from './atividadesApi';

export type AlunoAlternativa = {
  id: number;
  texto: string;
  ordem: number;
};

export type AlunoQuestao = {
  id: number;
  enunciado: string;
  tipo: TipoQuestao;
  imagemUrl?: string | null;
  ordem: number;
  tentativasUsadas: number;
  limiteTentativas: number;
  dicasLiberadas: number;
  dicaAtual?: string | null;
  encerrada: boolean;
  alternativas: AlunoAlternativa[];
};

export type AlunoAtividadeResumo = {
  atribuicaoId: number;
  atividadeId: number;
  titulo: string;
  anoEscolar: number;
  conteudo: string;
  estado: 'PARA_FAZER' | 'EM_ANDAMENTO' | 'CONCLUIDA';
  dataInicio: string;
  dataLimite?: string | null;
  tentativaId?: number | null;
};

export type AlunoAtividadeDetalhe = {
  atribuicaoId: number;
  atividadeId: number;
  titulo: string;
  descricao?: string | null;
  anoEscolar: number;
  conteudo: string;
  estado: 'PARA_FAZER' | 'EM_ANDAMENTO' | 'CONCLUIDA';
  tentativaId?: number | null;
  perfilPedagogico?: PerfilPedagogico | null;
  questoes: AlunoQuestao[];
};

export type TentativaAluno = {
  id: number;
  atribuicaoId: number;
  status: StatusTentativa;
  iniciadaEm: string;
  finalizadaEm?: string | null;
};

export type RespostaQuestaoPayload = {
  resposta: string;
  tempoSegundos: number;
};

export type RespostaQuestaoResultado = {
  correta: boolean;
  encerrada: boolean;
  tentativasUsadas: number;
  limiteTentativas: number;
  dicasLiberadas: number;
  dica?: string | null;
  explicacao?: string | null;
  respostaCorreta?: string | null;
  atividadeFinalizada: boolean;
};

export type TentativaResultado = {
  id: number;
  status: StatusTentativa;
  quantidadeAcertos: number;
  quantidadeErros: number;
  percentual?: number | null;
  tempoTotalSegundos: number;
  finalizadaEm?: string | null;
};

export async function listarAtividadesAluno() {
  return (await api.get<AlunoAtividadeResumo[]>('/aluno/atividades')).data;
}

export async function detalharAtividadeAluno(atribuicaoId: number) {
  return (await api.get<AlunoAtividadeDetalhe>(`/aluno/atividades/${atribuicaoId}`)).data;
}

export async function iniciarAtividade(atribuicaoId: number) {
  return (await api.post<TentativaAluno>(`/aluno/atribuicoes/${atribuicaoId}/iniciar`)).data;
}

export async function responderQuestao(tentativaId: number, questaoId: number, payload: RespostaQuestaoPayload) {
  return (await api.post<RespostaQuestaoResultado>(
    `/aluno/tentativas/${tentativaId}/questoes/${questaoId}/responder`,
    payload,
  )).data;
}

export async function buscarResultadoAluno(tentativaId: number) {
  return (await api.get<TentativaResultado>(`/aluno/tentativas/${tentativaId}/resultado`)).data;
}

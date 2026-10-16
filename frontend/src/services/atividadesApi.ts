import { api } from './authApi';

export type DificuldadeAtividade = 'BASICA' | 'INTERMEDIARIA' | 'AVANCADA';
export type TipoOrigemAtividade = 'SISTEMA' | 'PROFESSOR';
export type TipoQuestao = 'MULTIPLA_ESCOLHA' | 'NUMERICA' | 'TEXTO_CURTO';
export type StatusAtribuicao = 'RASCUNHO' | 'ATIVA' | 'ENCERRADA' | 'CANCELADA';
export type StatusTentativa = 'EM_ANDAMENTO' | 'FINALIZADA';

export type Atividade = {
  id: number;
  professorId: number | null;
  titulo: string;
  descricao?: string | null;
  anoEscolar: number;
  conteudo: string;
  dificuldade: DificuldadeAtividade;
  tipoOrigem: TipoOrigemAtividade;
  publicada: boolean;
  limiteTentativas: number;
  criadoEm?: string;
};

export type AtividadePayload = {
  titulo: string;
  descricao?: string | null;
  anoEscolar: number;
  conteudo: string;
  dificuldade: DificuldadeAtividade;
  limiteTentativas?: number;
};

export type AlternativaProfessor = {
  id?: number;
  texto: string;
  correta: boolean;
  ordem: number;
};

export type QuestaoProfessor = {
  id: number;
  atividadeId: number;
  enunciado: string;
  tipo: TipoQuestao;
  respostaCorreta?: string | null;
  explicacao?: string | null;
  dica1?: string | null;
  dica2?: string | null;
  dica3?: string | null;
  imagemUrl?: string | null;
  ordem: number;
  limiteTentativas?: number | null;
  alternativas: AlternativaProfessor[];
};

export type QuestaoPayload = Omit<QuestaoProfessor, 'id' | 'atividadeId'>;

export type SalvarQuestaoBancoPayload = {
  anoEscolar: number;
  conteudo: string;
  dificuldade: DificuldadeAtividade;
  habilidadeIds: number[];
  tagIds: number[];
};

export type Atribuicao = {
  id: number;
  atividadeId: number;
  professorId: number;
  turmaId?: number | null;
  alunoId?: number | null;
  dataInicio: string;
  dataLimite?: string | null;
  status: StatusAtribuicao;
  criadoEm?: string;
};

export type AtribuicaoPayload = {
  atividadeId: number;
  turmaId?: number | null;
  alunoId?: number | null;
  dataInicio?: string | null;
  dataLimite?: string | null;
};

export type AlunoResultado = {
  alunoId: number;
  nome: string;
  status: StatusTentativa;
  acertos: number;
  erros: number;
  percentual?: number | null;
  tempoTotalSegundos: number;
  totalSubmissoes: number;
  totalDicas: number;
};

export type QuestaoResultado = {
  questaoId: number;
  enunciado: string;
  acertos: number;
  errosFinais: number;
  totalSubmissoes: number;
};

export type AtividadeResultados = {
  atribuicaoId: number;
  atividadeId: number;
  titulo: string;
  totalAlunos: number;
  participantes: number;
  concluidos: number;
  participacaoPercentual: number;
  mediaPercentual: number;
  alunos: AlunoResultado[];
  questoes: QuestaoResultado[];
};

function withDefaults(payload: AtividadePayload) {
  return { ...payload, limiteTentativas: payload.limiteTentativas ?? 3 };
}

export async function listarAtividades() { return (await api.get<Atividade[]>('/atividades')).data; }
export async function buscarAtividade(id: number) { return (await api.get<Atividade>(`/atividades/${id}`)).data; }
export async function criarAtividade(payload: AtividadePayload) { return (await api.post<Atividade>('/atividades', withDefaults(payload))).data; }
export async function atualizarAtividade(id: number, payload: AtividadePayload) { return (await api.put<Atividade>(`/atividades/${id}`, withDefaults(payload))).data; }
export async function excluirAtividade(id: number) { await api.delete(`/atividades/${id}`); }
export async function publicarAtividade(id: number) { return (await api.post<Atividade>(`/atividades/${id}/publicar`)).data; }
export async function listarQuestoes(atividadeId: number) { return (await api.get<QuestaoProfessor[]>(`/atividades/${atividadeId}/questoes`)).data; }
export async function criarQuestao(atividadeId: number, payload: QuestaoPayload) { return (await api.post<QuestaoProfessor>(`/atividades/${atividadeId}/questoes`, payload)).data; }
export async function atualizarQuestao(atividadeId: number, questaoId: number, payload: QuestaoPayload) { return (await api.put<QuestaoProfessor>(`/atividades/${atividadeId}/questoes/${questaoId}`, payload)).data; }
export async function excluirQuestao(atividadeId: number, questaoId: number) { await api.delete(`/atividades/${atividadeId}/questoes/${questaoId}`); }
export async function importarQuestoes(atividadeId: number, questoesModeloIds: number[]) {
  return (await api.post<QuestaoProfessor[]>(`/atividades/${atividadeId}/questoes/importar`, { questoesModeloIds })).data;
}
export async function salvarQuestaoNoBanco(atividadeId: number, questaoId: number, payload: SalvarQuestaoBancoPayload) {
  return (await api.post(`/atividades/${atividadeId}/questoes/${questaoId}/salvar-no-banco`, payload)).data;
}
export async function listarAtribuicoes() { return (await api.get<Atribuicao[]>('/atribuicoes')).data; }
export async function atribuirAtividade(payload: AtribuicaoPayload) { return (await api.post<Atribuicao>('/atribuicoes', payload)).data; }
export async function buscarResultados(atribuicaoId: number) { return (await api.get<AtividadeResultados>(`/atribuicoes/${atribuicaoId}/resultados`)).data; }

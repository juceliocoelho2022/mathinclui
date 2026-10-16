import { api } from './authApi';
import type { Atividade, DificuldadeAtividade, TipoQuestao } from './atividadesApi';

export type TipoOrigemBiblioteca = 'SISTEMA' | 'PROFESSOR';

export type HabilidadeBncc = {
  id: number;
  codigo: string;
  anoEscolar: number;
  unidadeTematica: string;
  objetoConhecimento: string;
  descricao: string;
};

export type TagProfessor = { id: number; nome: string };

export type ClassificacaoAtividade = {
  habilidades: HabilidadeBncc[];
  tags: TagProfessor[];
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type BibliotecaAtividade = {
  id: number;
  titulo: string;
  anoEscolar: number;
  conteudo: string;
  dificuldade: DificuldadeAtividade;
  tipoOrigem: TipoOrigemBiblioteca;
  publicada: boolean;
  quantidadeQuestoes: number;
  habilidades: HabilidadeBncc[];
  tags: TagProfessor[];
};

export type AlternativaModelo = { id: number; texto: string; correta: boolean; ordem: number };

export type QuestaoModelo = {
  id: number;
  professorId: number | null;
  tipoOrigem: TipoOrigemBiblioteca;
  enunciado: string;
  tipo: TipoQuestao;
  respostaCorreta: string | null;
  explicacao: string | null;
  dica1: string | null;
  dica2: string | null;
  dica3: string | null;
  imagemUrl: string | null;
  limiteTentativas: number | null;
  anoEscolar: number;
  conteudo: string;
  dificuldade: DificuldadeAtividade;
  origemModeloId: number | null;
  copiadoEm: string | null;
  alternativas: AlternativaModelo[];
  habilidadeIds: number[];
  tagIds: number[];
};

export type BibliotecaAtividadeParams = {
  origem?: TipoOrigemBiblioteca;
  ano?: number;
  unidadeTematica?: string;
  objetoConhecimento?: string;
  habilidade?: string;
  conteudo?: string;
  dificuldade?: DificuldadeAtividade;
  tag?: string;
  busca?: string;
  page?: number;
  size?: number;
};

export type BibliotecaQuestaoParams = {
  origem?: TipoOrigemBiblioteca;
  ano?: number;
  conteudo?: string;
  dificuldade?: DificuldadeAtividade;
  tipo?: TipoQuestao;
  habilidade?: string;
  tag?: string;
  busca?: string;
  page?: number;
  size?: number;
};

function compact<T extends Record<string, unknown>>(params: T) {
  return Object.fromEntries(Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''));
}

export async function listarBibliotecaAtividades(params: BibliotecaAtividadeParams = {}) {
  return (await api.get<PageResponse<BibliotecaAtividade>>('/biblioteca/atividades', { params: compact(params) })).data;
}

export async function copiarAtividadeBiblioteca(id: number) {
  return (await api.post<Atividade>(`/biblioteca/atividades/${id}/copiar`)).data;
}

export async function listarBibliotecaQuestoes(params: BibliotecaQuestaoParams = {}) {
  return (await api.get<PageResponse<QuestaoModelo>>('/biblioteca/questoes', { params: compact(params) })).data;
}

export async function copiarQuestaoBiblioteca(id: number) {
  return (await api.post<QuestaoModelo>(`/biblioteca/questoes/${id}/copiar`)).data;
}

export async function listarHabilidadesBncc(ano?: number) {
  return (await api.get<HabilidadeBncc[]>('/biblioteca/bncc', { params: compact({ ano }) })).data;
}

export async function listarTagsProfessor() {
  return (await api.get<TagProfessor[]>('/biblioteca/tags')).data;
}

export async function buscarClassificacaoAtividade(atividadeId: number) {
  return (await api.get<ClassificacaoAtividade>(`/atividades/${atividadeId}/classificacao`)).data;
}

export async function atualizarClassificacaoAtividade(atividadeId: number, payload: { habilidadeIds: number[]; tagIds: number[] }) {
  return (await api.put<ClassificacaoAtividade>(`/atividades/${atividadeId}/classificacao`, payload)).data;
}

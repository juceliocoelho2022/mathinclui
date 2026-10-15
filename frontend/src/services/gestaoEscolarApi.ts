import { api } from './authApi';

export type Turma = {
  id: number;
  nome: string;
  anoEscolar: number;
  codigo: string;
  ativa: boolean;
  criadoEm?: string;
};

export type Aluno = {
  id: number;
  turmaId: number;
  nome: string;
  username: string;
  ativo: boolean;
  criadoEm?: string;
};

export type PerfilPedagogico = {
  id: number;
  alunoId: number;
  tamanhoFonte: 'PADRAO' | 'GRANDE' | 'EXTRA_GRANDE';
  leituraVoz: boolean;
  apoioVisual: boolean;
  dicasPassoAPasso: boolean;
  repeticaoGuiada: boolean;
  questoesPorTela: number;
  tempoAmpliado: boolean;
  nivelApoio: 'PADRAO' | 'MODERADO' | 'AMPLIADO';
};

export async function listarTurmas() {
  return (await api.get<Turma[]>('/turmas')).data;
}

export async function criarTurma(payload: { nome: string; anoEscolar: number; codigo: string }) {
  return (await api.post<Turma>('/turmas', payload)).data;
}

export async function atualizarTurma(id: number, payload: { nome: string; anoEscolar: number; codigo: string }) {
  return (await api.put<Turma>(`/turmas/${id}`, payload)).data;
}

export async function desativarTurma(id: number) {
  await api.delete(`/turmas/${id}`);
}

export async function listarAlunos(turmaId: number) {
  return (await api.get<Aluno[]>(`/turmas/${turmaId}/alunos`)).data;
}

export async function criarAluno(turmaId: number, payload: { nome: string; username: string; pin: string }) {
  return (await api.post<Aluno>(`/turmas/${turmaId}/alunos`, payload)).data;
}

export async function atualizarAluno(id: number, payload: { nome: string; username: string }) {
  return (await api.put<Aluno>(`/alunos/${id}`, payload)).data;
}

export async function redefinirPin(id: number, pin: string) {
  await api.patch(`/alunos/${id}/pin`, { pin });
}

export async function buscarPerfilPedagogico(alunoId: number) {
  return (await api.get<PerfilPedagogico>(`/alunos/${alunoId}/perfil-pedagogico`)).data;
}

export async function atualizarPerfilPedagogico(alunoId: number, payload: Omit<PerfilPedagogico, 'id' | 'alunoId'>) {
  return (await api.put<PerfilPedagogico>(`/alunos/${alunoId}/perfil-pedagogico`, payload)).data;
}

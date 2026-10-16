import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const mocks = vi.hoisted(() => ({
  fetchSession: vi.fn(),
  getAccessToken: vi.fn(() => 'token'),
  listarAtividadesAluno: vi.fn(),
}));
vi.mock('../../services/authApi', () => ({ fetchSession: mocks.fetchSession, getAccessToken: mocks.getAccessToken }));
vi.mock('../../services/alunoAtividadesApi', async () => ({
  ...(await vi.importActual<typeof import('../../services/alunoAtividadesApi')>('../../services/alunoAtividadesApi')),
  listarAtividadesAluno: mocks.listarAtividadesAluno,
}));

import { AlunoAtividadesPage } from './AlunoAtividadesPage';

describe('AlunoAtividadesPage', () => {
  beforeEach(() => vi.clearAllMocks());

  it('agrupa atividades por estado e oferece retomada', async () => {
    mocks.fetchSession.mockResolvedValue({ id: 2, nome: 'Ana', login: 'ana', tipo: 'ALUNO', role: 'ROLE_ALUNO' });
    mocks.listarAtividadesAluno.mockResolvedValue([
      { atribuicaoId: 1, atividadeId: 10, titulo: 'Frações', anoEscolar: 7, conteudo: 'FRACOES', estado: 'PARA_FAZER', dataInicio: '2026-10-16T00:00:00Z' },
      { atribuicaoId: 2, atividadeId: 11, titulo: 'Porcentagem', anoEscolar: 7, conteudo: 'PORCENTAGEM', estado: 'EM_ANDAMENTO', dataInicio: '2026-10-16T00:00:00Z', tentativaId: 20 },
      { atribuicaoId: 3, atividadeId: 12, titulo: 'Razões', anoEscolar: 7, conteudo: 'RAZOES', estado: 'CONCLUIDA', dataInicio: '2026-10-16T00:00:00Z', tentativaId: 21 },
    ]);
    render(<MemoryRouter><AlunoAtividadesPage /></MemoryRouter>);
    expect(await screen.findByRole('heading', { name: /para fazer/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /em andamento/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /concluídas/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /continuar porcentagem/i })).toBeInTheDocument();
  });
});

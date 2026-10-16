import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const mocks = vi.hoisted(() => ({ fetchSession: vi.fn(), getAccessToken: vi.fn(() => 'token'), buscarResultado: vi.fn() }));
vi.mock('../../services/authApi', () => ({ fetchSession: mocks.fetchSession, getAccessToken: mocks.getAccessToken }));
vi.mock('../../services/alunoAtividadesApi', async () => ({
  ...(await vi.importActual<typeof import('../../services/alunoAtividadesApi')>('../../services/alunoAtividadesApi')),
  buscarResultadoAluno: mocks.buscarResultado,
}));

import { ResultadoAlunoPage } from './ResultadoAlunoPage';

describe('ResultadoAlunoPage', () => {
  beforeEach(() => vi.clearAllMocks());
  it('mostra o resultado individual sem ranking', async () => {
    mocks.fetchSession.mockResolvedValue({ id: 2, nome: 'Ana', login: 'ana', tipo: 'ALUNO', role: 'ROLE_ALUNO' });
    mocks.buscarResultado.mockResolvedValue({ id: 10, status: 'FINALIZADA', quantidadeAcertos: 3, quantidadeErros: 1, percentual: 75, tempoTotalSegundos: 120, finalizadaEm: '2026-10-16T00:00:00Z' });
    render(<MemoryRouter initialEntries={['/app/tentativas/10/resultado']}><Routes><Route path="/app/tentativas/:tentativaId/resultado" element={<ResultadoAlunoPage />} /></Routes></MemoryRouter>);
    expect(await screen.findByText('75%')).toBeInTheDocument();
    expect(screen.getByText(/3 acerto/i)).toBeInTheDocument();
    expect(screen.queryByText(/ranking/i)).not.toBeInTheDocument();
  });
});

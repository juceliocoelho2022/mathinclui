import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const mocks = vi.hoisted(() => ({
  fetchSession: vi.fn(),
  getAccessToken: vi.fn(() => 'token'),
  listarAtividades: vi.fn(),
  listarAtribuicoes: vi.fn(),
}));

vi.mock('../../services/authApi', () => ({
  fetchSession: mocks.fetchSession,
  getAccessToken: mocks.getAccessToken,
}));
vi.mock('../../services/atividadesApi', async () => {
  const actual = await vi.importActual<typeof import('../../services/atividadesApi')>('../../services/atividadesApi');
  return { ...actual, listarAtividades: mocks.listarAtividades, listarAtribuicoes: mocks.listarAtribuicoes };
});

import { AtividadesProfessorPage } from './AtividadesProfessorPage';

describe('AtividadesProfessorPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mocks.getAccessToken.mockReturnValue('token');
    mocks.listarAtividades.mockResolvedValue([]);
    mocks.listarAtribuicoes.mockResolvedValue([]);
  });

  it('mostra o workspace para professor autenticado', async () => {
    mocks.fetchSession.mockResolvedValue({ id: 1, nome: 'Prof', login: 'prof@x', tipo: 'PROFESSOR', role: 'ROLE_PROFESSOR' });
    render(<MemoryRouter><AtividadesProfessorPage /></MemoryRouter>);
    expect(await screen.findByRole('heading', { name: /minhas atividades/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /nova atividade/i })).toBeInTheDocument();
  });

  it('redireciona aluno para a área autenticada', async () => {
    mocks.fetchSession.mockResolvedValue({ id: 2, nome: 'Aluno', login: 'aluno', tipo: 'ALUNO', role: 'ROLE_ALUNO' });
    render(
      <MemoryRouter initialEntries={['/app/atividades']}>
        <Routes>
          <Route path="/app/atividades" element={<AtividadesProfessorPage />} />
          <Route path="/app" element={<p>Área autenticada</p>} />
        </Routes>
      </MemoryRouter>,
    );
    await waitFor(() => expect(screen.getByText('Área autenticada')).toBeInTheDocument());
  });
});

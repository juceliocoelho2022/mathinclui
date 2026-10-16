import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const mocks = vi.hoisted(() => ({
  fetchSession: vi.fn(),
  getAccessToken: vi.fn(() => 'token'),
  buscarPreferencias: vi.fn(),
  salvarPreferencias: vi.fn(),
}));

vi.mock('../../services/authApi', () => ({ fetchSession: mocks.fetchSession, getAccessToken: mocks.getAccessToken }));
vi.mock('../../services/acessibilidadeApi', async () => {
  const actual = await vi.importActual<typeof import('../../services/acessibilidadeApi')>('../../services/acessibilidadeApi');
  return { ...actual, buscarPreferenciasInterface: mocks.buscarPreferencias, salvarPreferenciasInterface: mocks.salvarPreferencias };
});

import { MinhasPreferenciasPage } from './MinhasPreferenciasPage';

describe('MinhasPreferenciasPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mocks.fetchSession.mockResolvedValue({ id: 2, nome: 'Ana', login: 'ana', tipo: 'ALUNO', role: 'ROLE_ALUNO' });
    mocks.buscarPreferencias.mockResolvedValue({ tamanhoFonte: 'GRANDE', leituraVoz: true, apoioVisual: false });
    mocks.salvarPreferencias.mockResolvedValue({ tamanhoFonte: 'EXTRA_GRANDE', leituraVoz: true, apoioVisual: true });
  });

  it('carrega preferências persistidas e salva alterações do aluno', async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={['/app/minhas-preferencias']}>
        <Routes>
          <Route path="/app/minhas-preferencias" element={<MinhasPreferenciasPage />} />
          <Route path="/app" element={<p>Área autenticada</p>} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByLabelText(/tamanho do texto/i)).toHaveValue('GRANDE');
    expect(screen.getByRole('checkbox', { name: /leitura em voz/i })).toBeChecked();
    await user.selectOptions(screen.getByLabelText(/tamanho do texto/i), 'EXTRA_GRANDE');
    await user.click(screen.getByRole('checkbox', { name: /apoio visual/i }));
    await user.click(screen.getByRole('button', { name: /salvar preferências/i }));

    await waitFor(() => expect(mocks.salvarPreferencias).toHaveBeenCalledWith({
      tamanhoFonte: 'EXTRA_GRANDE', leituraVoz: true, apoioVisual: true,
    }));
    expect(await screen.findByText(/preferências salvas/i)).toBeInTheDocument();
  });
});

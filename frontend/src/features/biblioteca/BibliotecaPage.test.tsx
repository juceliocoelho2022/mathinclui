import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const mocks = vi.hoisted(() => ({
  fetchSession: vi.fn(),
  getAccessToken: vi.fn(() => 'token'),
  listarAtividades: vi.fn(),
  listarQuestoes: vi.fn(),
  copiarAtividade: vi.fn(),
}));

vi.mock('../../services/authApi', () => ({ fetchSession: mocks.fetchSession, getAccessToken: mocks.getAccessToken }));
vi.mock('../../services/bibliotecaApi', () => ({
  listarBibliotecaAtividades: mocks.listarAtividades,
  listarBibliotecaQuestoes: mocks.listarQuestoes,
  copiarAtividadeBiblioteca: mocks.copiarAtividade,
}));

import { BibliotecaPage } from './BibliotecaPage';

const pagina = (content: unknown[] = []) => ({ content, page: 0, size: 20, totalElements: content.length, totalPages: content.length ? 1 : 0 });
const oficial = {
  id: 10, titulo: 'Frações equivalentes', anoEscolar: 6, conteudo: 'FRACOES', dificuldade: 'BASICA',
  tipoOrigem: 'SISTEMA', publicada: true, quantidadeQuestoes: 1,
  habilidades: [{ id: 1, codigo: 'EF06MA07', anoEscolar: 6, unidadeTematica: 'Números', objetoConhecimento: 'Frações', descricao: 'Comparar frações' }], tags: [],
};

function renderPage(entry = '/app/biblioteca') {
  return render(
    <MemoryRouter initialEntries={[entry]}>
      <Routes>
        <Route path="/app/biblioteca" element={<BibliotecaPage />} />
        <Route path="/app/atividades" element={<p data-testid="editor-destino">Editor destino</p>} />
        <Route path="/app" element={<p>Área autenticada</p>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('BibliotecaPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mocks.fetchSession.mockResolvedValue({ id: 1, nome: 'Prof', login: 'prof@x', tipo: 'PROFESSOR', role: 'ROLE_PROFESSOR' });
    mocks.listarAtividades.mockResolvedValue(pagina([oficial]));
    mocks.listarQuestoes.mockResolvedValue(pagina([]));
    mocks.copiarAtividade.mockResolvedValue({ ...oficial, id: 77, professorId: 1, tipoOrigem: 'PROFESSOR', publicada: false, limiteTentativas: 3 });
  });

  it('aplica filtros e paginação na consulta de atividades', async () => {
    const user = userEvent.setup();
    mocks.listarAtividades
      .mockResolvedValueOnce({ ...pagina([oficial]), totalPages: 2, totalElements: 21 })
      .mockResolvedValueOnce({ ...pagina([oficial]), page: 0, totalPages: 2, totalElements: 21 })
      .mockResolvedValueOnce({ ...pagina([oficial]), page: 1, totalPages: 2, totalElements: 21 });
    renderPage();

    expect(await screen.findByText('Frações equivalentes')).toBeInTheDocument();
    await user.selectOptions(screen.getByLabelText(/ano escolar/i), '6');
    await user.selectOptions(screen.getByLabelText(/dificuldade/i), 'BASICA');
    await user.type(screen.getByLabelText(/buscar/i), 'fração');
    await user.click(screen.getByRole('button', { name: /aplicar filtros/i }));

    await waitFor(() => expect(mocks.listarAtividades).toHaveBeenCalledWith(expect.objectContaining({ ano: 6, dificuldade: 'BASICA', busca: 'fração', page: 0, size: 20 })));
    await user.click(screen.getByRole('button', { name: /próxima página/i }));
    await waitFor(() => expect(mocks.listarAtividades).toHaveBeenLastCalledWith(expect.objectContaining({ page: 1 })));
  });

  it('não oferece editar em conteúdo oficial e copia apenas uma vez antes de encaminhar ao editor', async () => {
    let resolveCopy!: (value: unknown) => void;
    mocks.copiarAtividade.mockReturnValue(new Promise((resolve) => { resolveCopy = resolve; }));
    const user = userEvent.setup();
    renderPage();

    expect(await screen.findByText('Frações equivalentes')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /^editar$/i })).not.toBeInTheDocument();
    const usar = screen.getByRole('button', { name: /usar atividade/i });
    await user.click(usar);
    await user.click(usar);
    expect(mocks.copiarAtividade).toHaveBeenCalledTimes(1);
    expect(usar).toBeDisabled();

    resolveCopy({ ...oficial, id: 77, professorId: 1, tipoOrigem: 'PROFESSOR', publicada: false, limiteTentativas: 3 });
    expect(await screen.findByTestId('editor-destino')).toBeInTheDocument();
  });

  it('troca entre Atividades, Questões e Minha biblioteca e trata vazio/erro', async () => {
    const user = userEvent.setup();
    renderPage();
    await screen.findByText('Frações equivalentes');

    await user.click(screen.getByRole('button', { name: /^questões$/i }));
    expect(await screen.findByText(/nenhuma questão encontrada/i)).toBeInTheDocument();
    expect(mocks.listarQuestoes).toHaveBeenCalled();

    mocks.listarAtividades.mockRejectedValueOnce(new Error('boom'));
    await user.click(screen.getByRole('button', { name: /minha biblioteca/i }));
    expect(await screen.findByRole('alert')).toHaveTextContent(/não foi possível carregar/i);
  });
});

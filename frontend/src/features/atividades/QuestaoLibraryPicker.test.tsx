import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const mocks = vi.hoisted(() => ({
  listarBibliotecaQuestoes: vi.fn(),
  importarQuestoes: vi.fn(),
}));

vi.mock('../../services/bibliotecaApi', async () => {
  const actual = await vi.importActual<typeof import('../../services/bibliotecaApi')>('../../services/bibliotecaApi');
  return { ...actual, listarBibliotecaQuestoes: mocks.listarBibliotecaQuestoes };
});
vi.mock('../../services/atividadesApi', async () => {
  const actual = await vi.importActual<typeof import('../../services/atividadesApi')>('../../services/atividadesApi');
  return { ...actual, importarQuestoes: mocks.importarQuestoes };
});

import { QuestaoLibraryPicker } from './QuestaoLibraryPicker';

const modelo = (id: number, enunciado: string) => ({
  id, professorId: null, tipoOrigem: 'SISTEMA' as const, enunciado, tipo: 'NUMERICA' as const,
  respostaCorreta: '1', explicacao: null, dica1: null, dica2: null, dica3: null, imagemUrl: null,
  limiteTentativas: 3, anoEscolar: 6, conteudo: 'FRACOES', dificuldade: 'BASICA' as const,
  origemModeloId: null, copiadoEm: null, alternativas: [], habilidadeIds: [1], tagIds: [],
});

describe('QuestaoLibraryPicker', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mocks.listarBibliotecaQuestoes.mockResolvedValue({
      content: [modelo(11, 'Qual é 1/2 de 10?'), modelo(12, 'Qual é 1/4 de 20?')],
      page: 0, size: 20, totalElements: 2, totalPages: 1,
    });
    mocks.importarQuestoes.mockResolvedValue([{ id: 31 }, { id: 32 }]);
  });

  it('seleciona vários modelos e importa uma única vez', async () => {
    const user = userEvent.setup();
    const onImported = vi.fn();
    render(<QuestaoLibraryPicker atividadeId={7} onImported={onImported} onClose={() => undefined} />);

    await user.click(await screen.findByRole('checkbox', { name: /qual é 1\/2 de 10/i }));
    await user.click(screen.getByRole('checkbox', { name: /qual é 1\/4 de 20/i }));
    await user.click(screen.getByRole('button', { name: /adicionar 2 questões/i }));

    expect(mocks.importarQuestoes).toHaveBeenCalledTimes(1);
    expect(mocks.importarQuestoes).toHaveBeenCalledWith(7, [11, 12]);
    expect(onImported).toHaveBeenCalledTimes(1);
  });
});

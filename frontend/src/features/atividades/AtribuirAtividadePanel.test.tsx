import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const mocks = vi.hoisted(() => ({
  atribuirAtividade: vi.fn(),
  listarTurmas: vi.fn(),
  listarAlunos: vi.fn(),
}));
vi.mock('../../services/atividadesApi', async () => {
  const actual = await vi.importActual<typeof import('../../services/atividadesApi')>('../../services/atividadesApi');
  return { ...actual, atribuirAtividade: mocks.atribuirAtividade };
});
vi.mock('../../services/gestaoEscolarApi', async () => {
  const actual = await vi.importActual<typeof import('../../services/gestaoEscolarApi')>('../../services/gestaoEscolarApi');
  return { ...actual, listarTurmas: mocks.listarTurmas, listarAlunos: mocks.listarAlunos };
});

import { AtribuirAtividadePanel } from './AtribuirAtividadePanel';

describe('AtribuirAtividadePanel', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mocks.listarTurmas.mockResolvedValue([{ id: 2, nome: '7A', anoEscolar: 7, codigo: '7A', ativa: true }]);
    mocks.listarAlunos.mockResolvedValue([]);
    mocks.atribuirAtividade.mockResolvedValue({ id: 9 });
  });

  it('envia somente turma quando destino é turma', async () => {
    const user = userEvent.setup();
    const atividade = { id: 7, professorId: 1, titulo: 'Frações', anoEscolar: 7, conteudo: 'FRACOES', dificuldade: 'BASICA' as const, tipoOrigem: 'PROFESSOR' as const, publicada: true, limiteTentativas: 3 };
    render(<AtribuirAtividadePanel atividade={atividade} onDone={() => undefined} onCancel={() => undefined} />);
    await screen.findByRole('option', { name: /7A/i });
    await user.selectOptions(screen.getByLabelText(/destino/i), 'TURMA');
    await user.selectOptions(screen.getByLabelText(/turma/i), '2');
    await user.click(screen.getByRole('button', { name: /atribuir atividade/i }));
    expect(mocks.atribuirAtividade).toHaveBeenCalledWith(expect.objectContaining({ atividadeId: 7, turmaId: 2 }));
    expect(mocks.atribuirAtividade.mock.calls[0][0].alunoId).toBeUndefined();
  });
});

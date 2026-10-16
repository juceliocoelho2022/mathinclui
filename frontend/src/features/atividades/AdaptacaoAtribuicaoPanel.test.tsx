import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const mocks = vi.hoisted(() => ({ listarAlunos: vi.fn() }));
vi.mock('../../services/gestaoEscolarApi', async () => {
  const actual = await vi.importActual<typeof import('../../services/gestaoEscolarApi')>('../../services/gestaoEscolarApi');
  return { ...actual, listarAlunos: mocks.listarAlunos };
});

import { AdaptacaoAtribuicaoPanel } from './AdaptacaoAtribuicaoPanel';

describe('AdaptacaoAtribuicaoPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mocks.listarAlunos.mockResolvedValue([{ id: 21, turmaId: 2, nome: 'Ana', username: 'ana', ativo: true }]);
  });

  it('começa herdando todas as configurações e publica somente os apoios escolhidos', async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    render(<AdaptacaoAtribuicaoPanel destino="TURMA" turmaId={2} onChange={onChange} />);

    expect(screen.getByLabelText(/fonte mínima/i)).toHaveValue('');
    expect(screen.getByLabelText(/nível de apoio/i)).toHaveValue('');
    await user.selectOptions(screen.getByLabelText(/fonte mínima/i), 'GRANDE');
    await user.selectOptions(screen.getByLabelText(/nível de apoio/i), 'MODERADO');
    await user.click(screen.getByRole('checkbox', { name: /apoio visual mínimo/i }));

    await waitFor(() => expect(onChange).toHaveBeenLastCalledWith(expect.objectContaining({
      geral: expect.objectContaining({ tamanhoFonteMinima: 'GRANDE', apoioVisualMinimo: true, nivelApoio: 'MODERADO' }),
    })));
  });

  it('oferece personalização individual apenas para atribuição de turma', async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    const { rerender } = render(<AdaptacaoAtribuicaoPanel destino="TURMA" turmaId={2} onChange={onChange} />);

    await user.click(screen.getByRole('button', { name: /personalizações individuais/i }));
    expect(await screen.findByRole('option', { name: /Ana/i })).toBeInTheDocument();
    await user.selectOptions(screen.getByLabelText(/aluno para personalizar/i), '21');
    await user.selectOptions(screen.getByLabelText(/nível individual/i), 'AMPLIADO');
    await waitFor(() => expect(onChange).toHaveBeenLastCalledWith(expect.objectContaining({
      individuais: [{ alunoId: 21, adaptacao: expect.objectContaining({ nivelApoio: 'AMPLIADO' }) }],
    })));

    rerender(<AdaptacaoAtribuicaoPanel destino="ALUNO" turmaId={2} onChange={onChange} />);
    expect(screen.queryByRole('button', { name: /personalizações individuais/i })).not.toBeInTheDocument();
  });
});

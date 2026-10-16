import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const mocks = vi.hoisted(() => ({
  atribuirAtividade: vi.fn(),
  listarTurmas: vi.fn(),
  listarAlunos: vi.fn(),
  salvarAdaptacaoGeral: vi.fn(),
  salvarAdaptacaoIndividual: vi.fn(),
}));
vi.mock('../../services/atividadesApi', async () => {
  const actual = await vi.importActual<typeof import('../../services/atividadesApi')>('../../services/atividadesApi');
  return { ...actual, atribuirAtividade: mocks.atribuirAtividade };
});
vi.mock('../../services/gestaoEscolarApi', async () => {
  const actual = await vi.importActual<typeof import('../../services/gestaoEscolarApi')>('../../services/gestaoEscolarApi');
  return { ...actual, listarTurmas: mocks.listarTurmas, listarAlunos: mocks.listarAlunos };
});
vi.mock('../../services/acessibilidadeApi', async () => {
  const actual = await vi.importActual<typeof import('../../services/acessibilidadeApi')>('../../services/acessibilidadeApi');
  return { ...actual, salvarAdaptacaoGeral: mocks.salvarAdaptacaoGeral, salvarAdaptacaoIndividual: mocks.salvarAdaptacaoIndividual };
});

import { AtribuirAtividadePanel } from './AtribuirAtividadePanel';

const atividade = { id: 7, professorId: 1, titulo: 'Frações', anoEscolar: 7, conteudo: 'FRACOES', dificuldade: 'BASICA' as const, tipoOrigem: 'PROFESSOR' as const, publicada: true, limiteTentativas: 3 };

describe('AtribuirAtividadePanel', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mocks.listarTurmas.mockResolvedValue([{ id: 2, nome: '7A', anoEscolar: 7, codigo: '7A', ativa: true }]);
    mocks.listarAlunos.mockResolvedValue([{ id: 21, turmaId: 2, nome: 'Ana', username: 'ana', ativo: true }]);
    mocks.atribuirAtividade.mockResolvedValue({ id: 9, atividadeId: 7, turmaId: 2, alunoId: null });
    mocks.salvarAdaptacaoGeral.mockResolvedValue({ atribuicaoId: 9 });
    mocks.salvarAdaptacaoIndividual.mockResolvedValue({ atribuicaoId: 9, alunoId: 21 });
  });

  it('cria a atribuição primeiro e depois salva a adaptação geral', async () => {
    const user = userEvent.setup();
    render(<AtribuirAtividadePanel atividade={atividade} onDone={() => undefined} onCancel={() => undefined} />);
    await screen.findByRole('option', { name: /7A/i });
    await user.selectOptions(screen.getByLabelText(/destino/i), 'TURMA');
    await user.selectOptions(screen.getByLabelText(/^turma$/i), '2');
    await user.selectOptions(screen.getByLabelText(/fonte mínima/i), 'GRANDE');
    await user.click(screen.getByRole('button', { name: /atribuir atividade/i }));

    await waitFor(() => expect(mocks.atribuirAtividade).toHaveBeenCalledTimes(1));
    expect(mocks.atribuirAtividade).toHaveBeenCalledWith(expect.objectContaining({ atividadeId: 7, turmaId: 2 }));
    expect(mocks.salvarAdaptacaoGeral).toHaveBeenCalledWith(9, expect.objectContaining({ tamanhoFonteMinima: 'GRANDE' }));
    expect(mocks.atribuirAtividade.mock.invocationCallOrder[0]).toBeLessThan(mocks.salvarAdaptacaoGeral.mock.invocationCallOrder[0]);
  });

  it('salva personalização individual para aluno da turma depois da atribuição', async () => {
    const user = userEvent.setup();
    render(<AtribuirAtividadePanel atividade={atividade} onDone={() => undefined} onCancel={() => undefined} />);
    await screen.findByRole('option', { name: /7A/i });
    await user.click(screen.getByRole('button', { name: /personalizações individuais/i }));
    await user.selectOptions(await screen.findByLabelText(/aluno para personalizar/i), '21');
    await user.selectOptions(screen.getByLabelText(/nível individual/i), 'AMPLIADO');
    await user.click(screen.getByRole('button', { name: /atribuir atividade/i }));

    await waitFor(() => expect(mocks.salvarAdaptacaoIndividual).toHaveBeenCalledWith(9, 21, expect.objectContaining({ nivelApoio: 'AMPLIADO' })));
  });

  it('em destino aluno não oferece personalização individual', async () => {
    const user = userEvent.setup();
    render(<AtribuirAtividadePanel atividade={atividade} onDone={() => undefined} onCancel={() => undefined} />);
    await screen.findByRole('option', { name: /7A/i });
    await user.selectOptions(screen.getByLabelText(/destino/i), 'ALUNO');
    expect(screen.queryByRole('button', { name: /personalizações individuais/i })).not.toBeInTheDocument();
  });

  it('se adaptação falha após criar atribuição, informa falha parcial sem criar outra atribuição', async () => {
    const user = userEvent.setup();
    mocks.salvarAdaptacaoGeral.mockRejectedValueOnce(new Error('falhou adaptação'));
    render(<AtribuirAtividadePanel atividade={atividade} onDone={() => undefined} onCancel={() => undefined} />);
    await screen.findByRole('option', { name: /7A/i });
    await user.selectOptions(screen.getByLabelText(/fonte mínima/i), 'GRANDE');
    await user.click(screen.getByRole('button', { name: /atribuir atividade/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent(/atribuição foi criada.*adaptação não foi salva/i);
    expect(mocks.atribuirAtividade).toHaveBeenCalledTimes(1);
    await user.click(screen.getByRole('button', { name: /tentar salvar adaptação novamente/i }));
    await waitFor(() => expect(mocks.salvarAdaptacaoGeral).toHaveBeenCalledTimes(2));
    expect(mocks.atribuirAtividade).toHaveBeenCalledTimes(1);
  });
});

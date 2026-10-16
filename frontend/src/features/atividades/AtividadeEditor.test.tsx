import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const mocks = vi.hoisted(() => ({
  criarAtividade: vi.fn(),
  atualizarAtividade: vi.fn(),
  listarQuestoes: vi.fn(),
  criarQuestao: vi.fn(),
  atualizarQuestao: vi.fn(),
  excluirQuestao: vi.fn(),
  publicarAtividade: vi.fn(),
}));
vi.mock('../../services/atividadesApi', async () => {
  const actual = await vi.importActual<typeof import('../../services/atividadesApi')>('../../services/atividadesApi');
  return { ...actual, ...mocks };
});

import { AtividadeEditor } from './AtividadeEditor';

describe('AtividadeEditor', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mocks.listarQuestoes.mockResolvedValue([]);
  });

  it('cria atividade com limite padrão de três tentativas', async () => {
    const user = userEvent.setup();
    const onSaved = vi.fn();
    mocks.criarAtividade.mockResolvedValue({ id: 7, titulo: 'Frações', anoEscolar: 7, conteudo: 'FRACOES', dificuldade: 'BASICA', tipoOrigem: 'PROFESSOR', publicada: false, limiteTentativas: 3, professorId: 1 });
    render(<AtividadeEditor atividade={null} onSaved={onSaved} onCancel={() => undefined} />);

    await user.type(screen.getByLabelText(/título/i), 'Frações');
    await user.selectOptions(screen.getByLabelText(/ano escolar/i), '7');
    await user.type(screen.getByLabelText(/conteúdo/i), 'FRACOES');
    await user.click(screen.getByRole('button', { name: /salvar atividade/i }));

    expect(mocks.criarAtividade).toHaveBeenCalledWith(expect.objectContaining({
      titulo: 'Frações', anoEscolar: 7, conteudo: 'FRACOES', dificuldade: 'BASICA', limiteTentativas: 3,
    }));
    expect(onSaved).toHaveBeenCalled();
  });

  it('editor de múltipla escolha oferece alternativas e resposta correta', async () => {
    const user = userEvent.setup();
    const atividade = { id: 7, professorId: 1, titulo: 'Frações', anoEscolar: 7, conteudo: 'FRACOES', dificuldade: 'BASICA' as const, tipoOrigem: 'PROFESSOR' as const, publicada: false, limiteTentativas: 3 };
    render(<AtividadeEditor atividade={atividade} onSaved={() => undefined} onCancel={() => undefined} />);
    await screen.findByText(/questões da atividade/i);
    await user.click(screen.getByRole('button', { name: /adicionar questão/i }));
    await user.selectOptions(screen.getByLabelText(/tipo de questão/i), 'MULTIPLA_ESCOLHA');
    expect(screen.getAllByLabelText(/alternativa/i).length).toBeGreaterThanOrEqual(2);
    expect(screen.getByText(/marque a alternativa correta/i)).toBeInTheDocument();
  });
});

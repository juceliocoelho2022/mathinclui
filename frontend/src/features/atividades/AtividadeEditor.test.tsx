import { render, screen, waitFor } from '@testing-library/react';
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
  salvarQuestaoNoBanco: vi.fn(),
  buscarClassificacaoAtividade: vi.fn(),
  atualizarClassificacaoAtividade: vi.fn(),
  listarHabilidadesBncc: vi.fn(),
  listarTagsProfessor: vi.fn(),
}));
vi.mock('../../services/atividadesApi', async () => {
  const actual = await vi.importActual<typeof import('../../services/atividadesApi')>('../../services/atividadesApi');
  return { ...actual, criarAtividade: mocks.criarAtividade, atualizarAtividade: mocks.atualizarAtividade,
    listarQuestoes: mocks.listarQuestoes, criarQuestao: mocks.criarQuestao, atualizarQuestao: mocks.atualizarQuestao,
    excluirQuestao: mocks.excluirQuestao, publicarAtividade: mocks.publicarAtividade, salvarQuestaoNoBanco: mocks.salvarQuestaoNoBanco };
});
vi.mock('../../services/bibliotecaApi', async () => {
  const actual = await vi.importActual<typeof import('../../services/bibliotecaApi')>('../../services/bibliotecaApi');
  return { ...actual, buscarClassificacaoAtividade: mocks.buscarClassificacaoAtividade,
    atualizarClassificacaoAtividade: mocks.atualizarClassificacaoAtividade,
    listarHabilidadesBncc: mocks.listarHabilidadesBncc, listarTagsProfessor: mocks.listarTagsProfessor };
});

import { AtividadeEditor } from './AtividadeEditor';

const atividade = { id: 7, professorId: 1, titulo: 'Frações', anoEscolar: 6, conteudo: 'FRACOES', dificuldade: 'BASICA' as const, tipoOrigem: 'PROFESSOR' as const, publicada: false, limiteTentativas: 3 };
const habilidade = { id: 1, codigo: 'EF06MA07', anoEscolar: 6, unidadeTematica: 'Números', objetoConhecimento: 'Frações', descricao: 'Comparar frações' };
const tag = { id: 2, nome: 'Revisão' };

describe('AtividadeEditor', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mocks.listarQuestoes.mockResolvedValue([]);
    mocks.buscarClassificacaoAtividade.mockResolvedValue({ habilidades: [], tags: [] });
    mocks.listarHabilidadesBncc.mockResolvedValue([habilidade]);
    mocks.listarTagsProfessor.mockResolvedValue([tag]);
    mocks.atualizarClassificacaoAtividade.mockResolvedValue({ habilidades: [habilidade], tags: [tag] });
    mocks.salvarQuestaoNoBanco.mockResolvedValue({ id: 100 });
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
    render(<AtividadeEditor atividade={atividade} onSaved={() => undefined} onCancel={() => undefined} />);
    await screen.findByText(/questões da atividade/i);
    await user.click(screen.getByRole('button', { name: /adicionar questão/i }));
    await user.selectOptions(screen.getByLabelText(/tipo de questão/i), 'MULTIPLA_ESCOLHA');
    expect(screen.getAllByLabelText(/alternativa/i).length).toBeGreaterThanOrEqual(2);
    expect(screen.getByText(/marque a alternativa correta/i)).toBeInTheDocument();
  });

  it('salva classificação curricular em chamada separada do payload base da atividade', async () => {
    const user = userEvent.setup();
    render(<AtividadeEditor atividade={atividade} onSaved={() => undefined} onCancel={() => undefined} />);

    await user.click(await screen.findByRole('checkbox', { name: /EF06MA07/i }));
    await user.click(screen.getByRole('checkbox', { name: /Revisão/i }));
    await user.click(screen.getByRole('button', { name: /salvar classificação/i }));

    expect(mocks.atualizarClassificacaoAtividade).toHaveBeenCalledWith(7, { habilidadeIds: [1], tagIds: [2] });
    expect(mocks.atualizarAtividade).not.toHaveBeenCalled();
  });

  it('salva questão concreta no banco reutilizável usando metadados curriculares da atividade', async () => {
    const user = userEvent.setup();
    mocks.buscarClassificacaoAtividade.mockResolvedValue({ habilidades: [habilidade], tags: [tag] });
    mocks.listarQuestoes.mockResolvedValue([{ id: 20, atividadeId: 7, enunciado: 'Qual é 1/2 de 10?', tipo: 'NUMERICA', respostaCorreta: '5', explicacao: null, dica1: null, dica2: null, dica3: null, imagemUrl: null, ordem: 1, limiteTentativas: 3, alternativas: [] }]);
    render(<AtividadeEditor atividade={atividade} onSaved={() => undefined} onCancel={() => undefined} />);

    await user.click(await screen.findByRole('button', { name: /salvar no banco/i }));
    await waitFor(() => expect(mocks.salvarQuestaoNoBanco).toHaveBeenCalledWith(7, 20, {
      anoEscolar: 6,
      conteudo: 'FRACOES',
      dificuldade: 'BASICA',
      habilidadeIds: [1],
      tagIds: [2],
    }));
  });
});

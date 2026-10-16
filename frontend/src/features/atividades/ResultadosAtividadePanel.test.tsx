import { render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const mocks = vi.hoisted(() => ({ buscarResultados: vi.fn() }));
vi.mock('../../services/atividadesApi', async () => {
  const actual = await vi.importActual<typeof import('../../services/atividadesApi')>('../../services/atividadesApi');
  return { ...actual, buscarResultados: mocks.buscarResultados };
});

import { ResultadosAtividadePanel } from './ResultadosAtividadePanel';

describe('ResultadosAtividadePanel', () => {
  beforeEach(() => vi.clearAllMocks());

  it('mostra participação, média e dificuldades sem ranking', async () => {
    mocks.buscarResultados.mockResolvedValue({
      atribuicaoId: 9, atividadeId: 7, titulo: 'Frações', totalAlunos: 10, participantes: 8, concluidos: 6,
      participacaoPercentual: 80, mediaPercentual: 75,
      alunos: [{ alunoId: 1, nome: 'Ana', status: 'FINALIZADA', acertos: 3, erros: 1, percentual: 75, tempoTotalSegundos: 120, totalSubmissoes: 5, totalDicas: 2 }],
      questoes: [{ questaoId: 3, enunciado: 'Metade de 10', acertos: 6, errosFinais: 2, totalSubmissoes: 12 }],
    });
    const atividade = { id: 7, professorId: 1, titulo: 'Frações', anoEscolar: 7, conteudo: 'FRACOES', dificuldade: 'BASICA' as const, tipoOrigem: 'PROFESSOR' as const, publicada: true, limiteTentativas: 3 };
    const atribuicoes = [{ id: 9, atividadeId: 7, professorId: 1, turmaId: 2, dataInicio: '2026-10-16T00:00:00Z', status: 'ATIVA' as const }];
    render(<ResultadosAtividadePanel atividade={atividade} atribuicoes={atribuicoes} onClose={() => undefined} />);
    expect(await screen.findByText('80%')).toBeInTheDocument();
    expect(screen.getByText('75%')).toBeInTheDocument();
    expect(screen.getByText('Ana')).toBeInTheDocument();
    expect(screen.queryByText(/ranking/i)).not.toBeInTheDocument();
  });
});

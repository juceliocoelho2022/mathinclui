import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { QuestaoCard } from './QuestaoCard';

describe('QuestaoCard', () => {
  it('aplica fonte pedagógica e não recebe campo de gabarito na questão', () => {
    const questao = { id: 1, enunciado: 'Quanto é 2 + 2?', tipo: 'NUMERICA' as const, ordem: 1, tentativasUsadas: 0, limiteTentativas: 3, dicasLiberadas: 0, encerrada: false, alternativas: [] };
    render(<QuestaoCard questao={questao} perfil={{ tamanhoFonte: 'EXTRA_GRANDE', leituraVoz: false, apoioVisual: true, dicasPassoAPasso: true, repeticaoGuiada: false, questoesPorTela: 1, tempoAmpliado: false, nivelApoio: 'AMPLIADO', id: 1, alunoId: 2 }} resposta="" onResposta={() => undefined} onResponder={vi.fn()} />);
    expect(screen.getByTestId('questao-card')).toHaveClass('font-extra-grande');
    expect(JSON.stringify(questao)).not.toContain('respostaCorreta');
  });
});

import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { QuestaoCard } from './QuestaoCard';

const questao = { id: 1, enunciado: 'Quanto é 2 + 2?', tipo: 'NUMERICA' as const, ordem: 1, tentativasUsadas: 0, limiteTentativas: 3, dicasLiberadas: 0, encerrada: false, alternativas: [] };
const acessibilidade = { tamanhoFonte: 'EXTRA_GRANDE' as const, leituraVoz: true, apoioVisual: true, dicasPassoAPasso: true, repeticaoGuiada: false, questoesPorTela: 1, tempoAmpliado: false, nivelApoio: 'AMPLIADO' as const };

describe('QuestaoCard', () => {
  it('aplica acessibilidade efetiva e não recebe campo de gabarito na questão', () => {
    render(<QuestaoCard questao={questao} acessibilidade={acessibilidade} resposta="" onResposta={() => undefined} onResponder={vi.fn()} />);
    expect(screen.getByTestId('questao-card')).toHaveClass('font-extra-grande');
    expect(screen.getByTestId('questao-card')).toHaveClass('with-visual-support');
    expect(JSON.stringify(questao)).not.toContain('respostaCorreta');
  });

  it('continua funcional sem speechSynthesis no navegador', async () => {
    const original = window.speechSynthesis;
    Object.defineProperty(window, 'speechSynthesis', { configurable: true, value: undefined });
    const user = userEvent.setup();
    const onResponder = vi.fn();
    render(<QuestaoCard questao={questao} acessibilidade={acessibilidade} resposta="4" onResposta={() => undefined} onResponder={onResponder} />);

    await user.click(screen.getByRole('button', { name: /ouvir enunciado/i }));
    await user.click(screen.getByRole('button', { name: /responder/i }));
    expect(onResponder).toHaveBeenCalledTimes(1);
    Object.defineProperty(window, 'speechSynthesis', { configurable: true, value: original });
  });

  it('apresenta repetição guiada em retry sem revelar resposta correta', () => {
    const guided = { ...acessibilidade, repeticaoGuiada: true };
    render(<QuestaoCard questao={{ ...questao, dicaAtual: 'Some duas unidades a 2.' }} acessibilidade={guided} resposta="3" onResposta={() => undefined} onResponder={vi.fn()} feedback={{ correta: false, encerrada: false, tentativasUsadas: 1, limiteTentativas: 3, dicasLiberadas: 1, dica: 'Some duas unidades a 2.', explicacao: null, respostaCorreta: null, atividadeFinalizada: false }} />);
    expect(screen.getByText(/vamos por partes/i)).toBeInTheDocument();
    expect(screen.getByText('Some duas unidades a 2.')).toBeInTheDocument();
    expect(screen.queryByText(/resposta correta/i)).not.toBeInTheDocument();
  });
});

import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { AccessibilityToolbar } from './AccessibilityToolbar';

const efetiva = {
  tamanhoFonte: 'GRANDE' as const,
  leituraVoz: true,
  apoioVisual: true,
  dicasPassoAPasso: true,
  repeticaoGuiada: false,
  questoesPorTela: 1,
  tempoAmpliado: false,
  nivelApoio: 'MODERADO' as const,
};

const preferencia = { tamanhoFonte: 'PADRAO' as const, leituraVoz: false, apoioVisual: false };

describe('AccessibilityToolbar', () => {
  it('permite aumentar a fonte mas não reduzir abaixo do mínimo efetivo', async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    render(<AccessibilityToolbar efetiva={efetiva} preferencia={preferencia} onChange={onChange} />);

    const fonte = screen.getByLabelText(/tamanho do texto/i);
    expect(fonte).toHaveValue('GRANDE');
    expect(screen.queryByRole('option', { name: /^padrão$/i })).toBeDisabled();
    await user.selectOptions(fonte, 'EXTRA_GRANDE');
    expect(onChange).toHaveBeenCalledWith(expect.objectContaining({ tamanhoFonte: 'EXTRA_GRANDE' }));
  });

  it('mantém leitura e apoio visual obrigatórios ligados e explica a restrição', () => {
    const onChange = vi.fn();
    render(<AccessibilityToolbar efetiva={efetiva} preferencia={preferencia} onChange={onChange} />);

    expect(screen.getByRole('checkbox', { name: /leitura em voz/i })).toBeChecked();
    expect(screen.getByRole('checkbox', { name: /leitura em voz/i })).toBeDisabled();
    expect(screen.getByRole('checkbox', { name: /apoio visual/i })).toBeChecked();
    expect(screen.getByRole('checkbox', { name: /apoio visual/i })).toBeDisabled();
    expect(screen.getByText(/apoio definido para esta atividade/i)).toBeInTheDocument();
  });
});

import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const mocks = vi.hoisted(() => ({
  fetchSession: vi.fn(),
  getAccessToken: vi.fn(() => 'token'),
  detalhar: vi.fn(),
  iniciar: vi.fn(),
  responder: vi.fn(),
}));
vi.mock('../../services/authApi', () => ({ fetchSession: mocks.fetchSession, getAccessToken: mocks.getAccessToken }));
vi.mock('../../services/alunoAtividadesApi', async () => ({
  ...(await vi.importActual<typeof import('../../services/alunoAtividadesApi')>('../../services/alunoAtividadesApi')),
  detalharAtividadeAluno: mocks.detalhar,
  iniciarAtividade: mocks.iniciar,
  responderQuestao: mocks.responder,
}));

import { ResolverAtividadePage } from './ResolverAtividadePage';

describe('ResolverAtividadePage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mocks.fetchSession.mockResolvedValue({ id: 2, nome: 'Ana', login: 'ana', tipo: 'ALUNO', role: 'ROLE_ALUNO' });
    mocks.detalhar.mockResolvedValue({
      atribuicaoId: 3, atividadeId: 7, titulo: 'Frações', anoEscolar: 7, conteudo: 'FRACOES', estado: 'PARA_FAZER', tentativaId: null,
      perfilPedagogico: { id: 1, alunoId: 2, tamanhoFonte: 'GRANDE', leituraVoz: false, apoioVisual: true, dicasPassoAPasso: true, repeticaoGuiada: false, questoesPorTela: 1, tempoAmpliado: false, nivelApoio: 'MODERADO' },
      questoes: [{ id: 20, enunciado: 'Qual é a metade de 10?', tipo: 'NUMERICA', ordem: 1, tentativasUsadas: 0, limiteTentativas: 2, dicasLiberadas: 0, encerrada: false, alternativas: [] }],
    });
    mocks.iniciar.mockResolvedValue({ id: 10, atribuicaoId: 3, status: 'EM_ANDAMENTO', iniciadaEm: '2026-10-16T00:00:00Z' });
  });

  it('não revela resposta antes do limite e revela ao encerrar', async () => {
    mocks.responder
      .mockResolvedValueOnce({ correta: false, encerrada: false, tentativasUsadas: 1, limiteTentativas: 2, dicasLiberadas: 1, dica: 'Divida por 2', explicacao: null, respostaCorreta: null, atividadeFinalizada: false })
      .mockResolvedValueOnce({ correta: false, encerrada: true, tentativasUsadas: 2, limiteTentativas: 2, dicasLiberadas: 1, dica: null, explicacao: 'Metade significa dividir por 2.', respostaCorreta: '5', atividadeFinalizada: true });
    const user = userEvent.setup();
    render(<MemoryRouter initialEntries={['/app/atividades-aluno/3']}><Routes><Route path="/app/atividades-aluno/:atribuicaoId" element={<ResolverAtividadePage />} /></Routes></MemoryRouter>);
    const input = await screen.findByLabelText(/sua resposta/i);
    await user.type(input, '4');
    await user.click(screen.getByRole('button', { name: /responder/i }));
    expect(await screen.findByText('Divida por 2')).toBeInTheDocument();
    expect(screen.queryByText(/resposta correta: 5/i)).not.toBeInTheDocument();
    await user.clear(input);
    await user.type(input, '4');
    await user.click(screen.getByRole('button', { name: /responder/i }));
    expect(await screen.findByText(/metade significa dividir por 2/i)).toBeInTheDocument();
    expect(screen.getByText(/resposta correta: 5/i)).toBeInTheDocument();
  });
});

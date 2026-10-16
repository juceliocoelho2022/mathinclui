import { beforeEach, describe, expect, it, vi } from 'vitest';

const mocks = vi.hoisted(() => ({ get: vi.fn(), post: vi.fn() }));

vi.mock('./authApi', () => ({ api: { get: mocks.get, post: mocks.post } }));

import { iniciarAtividade, listarAtividadesAluno, responderQuestao } from './alunoAtividadesApi';

describe('alunoAtividadesApi', () => {
  beforeEach(() => vi.clearAllMocks());

  it('usa apenas os endpoints protegidos do aluno', async () => {
    mocks.get.mockResolvedValueOnce({ data: [] });
    mocks.post.mockResolvedValue({ data: { id: 10 } });

    await listarAtividadesAluno();
    await iniciarAtividade(3);
    await responderQuestao(10, 20, { resposta: '5', tempoSegundos: 4 });

    expect(mocks.get).toHaveBeenCalledWith('/aluno/atividades');
    expect(mocks.post).toHaveBeenCalledWith('/aluno/atribuicoes/3/iniciar');
    expect(mocks.post).toHaveBeenCalledWith('/aluno/tentativas/10/questoes/20/responder', { resposta: '5', tempoSegundos: 4 });
  });
});

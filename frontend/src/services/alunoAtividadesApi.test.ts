import { beforeEach, describe, expect, it, vi } from 'vitest';

const get = vi.fn();
const post = vi.fn();
vi.mock('./authApi', () => ({ api: { get, post } }));

import { iniciarAtividade, listarAtividadesAluno, responderQuestao } from './alunoAtividadesApi';

describe('alunoAtividadesApi', () => {
  beforeEach(() => vi.clearAllMocks());

  it('usa apenas os endpoints protegidos do aluno', async () => {
    get.mockResolvedValueOnce({ data: [] });
    post.mockResolvedValue({ data: { id: 10 } });

    await listarAtividadesAluno();
    await iniciarAtividade(3);
    await responderQuestao(10, 20, { resposta: '5', tempoSegundos: 4 });

    expect(get).toHaveBeenCalledWith('/aluno/atividades');
    expect(post).toHaveBeenCalledWith('/aluno/atribuicoes/3/iniciar');
    expect(post).toHaveBeenCalledWith('/aluno/tentativas/10/questoes/20/responder', { resposta: '5', tempoSegundos: 4 });
  });
});

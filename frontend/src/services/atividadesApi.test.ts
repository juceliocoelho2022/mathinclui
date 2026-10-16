import { beforeEach, describe, expect, it, vi } from 'vitest';

const mocks = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  del: vi.fn(),
}));

vi.mock('./authApi', () => ({
  api: { get: mocks.get, post: mocks.post, put: mocks.put, delete: mocks.del },
}));

import { atribuirAtividade, criarAtividade, listarAtividades, publicarAtividade } from './atividadesApi';

describe('atividadesApi', () => {
  beforeEach(() => vi.clearAllMocks());

  it('usa os endpoints administrativos da v0.4', async () => {
    mocks.get.mockResolvedValueOnce({ data: [] });
    mocks.post.mockResolvedValue({ data: { id: 1 } });

    await listarAtividades();
    await criarAtividade({ titulo: 'Frações', anoEscolar: 7, conteudo: 'FRACOES', dificuldade: 'BASICA', limiteTentativas: 3 });
    await publicarAtividade(1);
    await atribuirAtividade({ atividadeId: 1, turmaId: 2 });

    expect(mocks.get).toHaveBeenCalledWith('/atividades');
    expect(mocks.post).toHaveBeenCalledWith('/atividades', expect.objectContaining({ limiteTentativas: 3 }));
    expect(mocks.post).toHaveBeenCalledWith('/atividades/1/publicar');
    expect(mocks.post).toHaveBeenCalledWith('/atribuicoes', expect.objectContaining({ turmaId: 2 }));
  });
});

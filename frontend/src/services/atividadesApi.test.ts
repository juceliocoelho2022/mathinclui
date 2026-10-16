import { beforeEach, describe, expect, it, vi } from 'vitest';

const get = vi.fn();
const post = vi.fn();
const put = vi.fn();
const del = vi.fn();

vi.mock('./authApi', () => ({ api: { get, post, put, delete: del } }));

import { atribuirAtividade, criarAtividade, listarAtividades, publicarAtividade } from './atividadesApi';

describe('atividadesApi', () => {
  beforeEach(() => vi.clearAllMocks());

  it('usa os endpoints administrativos da v0.4', async () => {
    get.mockResolvedValueOnce({ data: [] });
    post.mockResolvedValue({ data: { id: 1 } });

    await listarAtividades();
    await criarAtividade({ titulo: 'Frações', anoEscolar: 7, conteudo: 'FRACOES', dificuldade: 'BASICA', limiteTentativas: 3 });
    await publicarAtividade(1);
    await atribuirAtividade({ atividadeId: 1, turmaId: 2 });

    expect(get).toHaveBeenCalledWith('/atividades');
    expect(post).toHaveBeenCalledWith('/atividades', expect.objectContaining({ limiteTentativas: 3 }));
    expect(post).toHaveBeenCalledWith('/atividades/1/publicar');
    expect(post).toHaveBeenCalledWith('/atribuicoes', expect.objectContaining({ turmaId: 2 }));
  });
});

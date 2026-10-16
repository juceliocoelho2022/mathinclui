import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fetchSession, getAccessToken } from '../../services/authApi';
import {
  copiarAtividadeBiblioteca,
  listarBibliotecaAtividades,
  listarBibliotecaQuestoes,
  type BibliotecaAtividade,
  type BibliotecaAtividadeParams,
  type PageResponse,
  type QuestaoModelo,
} from '../../services/bibliotecaApi';
import { AtividadeBibliotecaCard } from './AtividadeBibliotecaCard';
import { BibliotecaFilters, type BibliotecaFilterValues } from './BibliotecaFilters';

type View = 'ATIVIDADES' | 'QUESTOES' | 'MINHA';

const emptyFilters: BibliotecaFilterValues = { ano: '', dificuldade: '', busca: '' };

export function BibliotecaPage() {
  const navigate = useNavigate();
  const [authorized, setAuthorized] = useState(false);
  const [view, setView] = useState<View>('ATIVIDADES');
  const [draftFilters, setDraftFilters] = useState<BibliotecaFilterValues>(emptyFilters);
  const [filters, setFilters] = useState<BibliotecaFilterValues>(emptyFilters);
  const [page, setPage] = useState(0);
  const [atividades, setAtividades] = useState<PageResponse<BibliotecaAtividade> | null>(null);
  const [questoes, setQuestoes] = useState<PageResponse<QuestaoModelo> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [copyingId, setCopyingId] = useState<number | null>(null);

  useEffect(() => {
    if (!getAccessToken()) {
      navigate('/entrar/professor');
      return;
    }
    fetchSession()
      .then((session) => {
        if (session.tipo !== 'PROFESSOR') {
          navigate('/app');
          return;
        }
        setAuthorized(true);
      })
      .catch(() => navigate('/entrar/professor'));
  }, [navigate]);

  const params = useCallback((): BibliotecaAtividadeParams => ({
    ...(filters.ano ? { ano: Number(filters.ano) } : {}),
    ...(filters.dificuldade ? { dificuldade: filters.dificuldade } : {}),
    ...(filters.busca.trim() ? { busca: filters.busca.trim() } : {}),
    ...(view === 'MINHA' ? { origem: 'PROFESSOR' as const } : {}),
    page,
    size: 20,
  }), [filters, page, view]);

  useEffect(() => {
    if (!authorized) return;
    let active = true;
    setLoading(true);
    setError('');

    const request = view === 'QUESTOES'
      ? listarBibliotecaQuestoes({
          ...(filters.ano ? { ano: Number(filters.ano) } : {}),
          ...(filters.dificuldade ? { dificuldade: filters.dificuldade } : {}),
          ...(filters.busca.trim() ? { busca: filters.busca.trim() } : {}),
          page,
          size: 20,
        }).then((result) => { if (active) setQuestoes(result); })
      : listarBibliotecaAtividades(params()).then((result) => { if (active) setAtividades(result); });

    request
      .catch(() => { if (active) setError('Não foi possível carregar a biblioteca.'); })
      .finally(() => { if (active) setLoading(false); });

    return () => { active = false; };
  }, [authorized, view, page, filters, params]);

  function mudarView(next: View) {
    setView(next);
    setPage(0);
    setError('');
  }

  function aplicarFiltros() {
    setPage(0);
    setFilters({ ...draftFilters });
  }

  async function usarAtividade(id: number) {
    if (copyingId !== null) return;
    setCopyingId(id);
    setError('');
    try {
      const copia = await copiarAtividadeBiblioteca(id);
      navigate(`/app/atividades?editar=${copia.id}`);
    } catch {
      setError('Não foi possível copiar a atividade.');
      setCopyingId(null);
    }
  }

  const currentPage = view === 'QUESTOES' ? questoes : atividades;

  return (
    <main className="page-shell library-shell">
      <section className="session-card">
        <div className="session-header">
          <div>
            <p className="card-kicker">v0.5 • Professor</p>
            <h1>Biblioteca Pedagógica</h1>
            <p>Encontre atividades e questões, filtre por contexto pedagógico e crie cópias independentes para suas turmas.</p>
          </div>
          <Link className="secondary-button link-button" to="/app">Voltar ao painel</Link>
        </div>

        <nav className="library-tabs" aria-label="Visões da biblioteca">
          <button type="button" className={view === 'ATIVIDADES' ? 'active' : ''} onClick={() => mudarView('ATIVIDADES')}>Atividades</button>
          <button type="button" className={view === 'QUESTOES' ? 'active' : ''} onClick={() => mudarView('QUESTOES')}>Questões</button>
          <button type="button" className={view === 'MINHA' ? 'active' : ''} onClick={() => mudarView('MINHA')}>Minha biblioteca</button>
        </nav>

        <BibliotecaFilters value={draftFilters} onChange={setDraftFilters} onApply={aplicarFiltros} />

        {error && <p className="form-message error" role="alert">{error}</p>}
        {loading && <p>Carregando biblioteca…</p>}

        {!loading && !error && view !== 'QUESTOES' && (
          <div className="library-grid">
            {atividades?.content.length ? atividades.content.map((atividade) => (
              <AtividadeBibliotecaCard
                key={atividade.id}
                atividade={atividade}
                copying={copyingId === atividade.id}
                onUse={usarAtividade}
              />
            )) : <article className="card empty-state"><h2>Nenhuma atividade encontrada</h2><p>Ajuste os filtros ou consulte outra visão da biblioteca.</p></article>}
          </div>
        )}

        {!loading && !error && view === 'QUESTOES' && (
          <div className="library-grid">
            {questoes?.content.length ? questoes.content.map((questao) => (
              <article className="card library-card" key={questao.id}>
                <div>
                  <p className="card-kicker">{questao.tipoOrigem === 'SISTEMA' ? 'Questão oficial' : 'Minha questão'}</p>
                  <h2>{questao.enunciado}</h2>
                  <p>{questao.anoEscolar}º ano • {questao.conteudo} • {questao.dificuldade}</p>
                  <p>{questao.tipo}</p>
                </div>
              </article>
            )) : <article className="card empty-state"><h2>Nenhuma questão encontrada</h2><p>Ajuste os filtros para buscar outros modelos reutilizáveis.</p></article>}
          </div>
        )}

        {!loading && !error && currentPage && currentPage.totalPages > 1 && (
          <div className="library-pagination" aria-label="Paginação da biblioteca">
            <button className="secondary-button" type="button" disabled={page === 0} onClick={() => setPage((value) => Math.max(0, value - 1))}>Página anterior</button>
            <span>Página {page + 1} de {currentPage.totalPages}</span>
            <button className="secondary-button" type="button" disabled={page + 1 >= currentPage.totalPages} onClick={() => setPage((value) => value + 1)}>Próxima página</button>
          </div>
        )}
      </section>
    </main>
  );
}

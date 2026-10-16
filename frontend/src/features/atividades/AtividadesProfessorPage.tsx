import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fetchSession, getAccessToken, type SessionUser } from '../../services/authApi';
import {
  listarAtividades,
  listarAtribuicoes,
  type Atividade,
  type Atribuicao,
} from '../../services/atividadesApi';
import { AtividadeEditor } from './AtividadeEditor';
import { AtribuirAtividadePanel } from './AtribuirAtividadePanel';
import { ResultadosAtividadePanel } from './ResultadosAtividadePanel';

type ViewMode = 'LISTA' | 'EDITOR' | 'ATRIBUIR' | 'RESULTADOS';

export function AtividadesProfessorPage() {
  const navigate = useNavigate();
  const [user, setUser] = useState<SessionUser | null>(null);
  const [atividades, setAtividades] = useState<Atividade[]>([]);
  const [atribuicoes, setAtribuicoes] = useState<Atribuicao[]>([]);
  const [selecionada, setSelecionada] = useState<Atividade | null>(null);
  const [mode, setMode] = useState<ViewMode>('LISTA');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!getAccessToken()) {
      navigate('/entrar/professor');
      return;
    }
    fetchSession()
      .then(async (session) => {
        if (session.tipo !== 'PROFESSOR') {
          navigate('/app');
          return;
        }
        setUser(session);
        await carregar();
      })
      .catch(() => navigate('/entrar/professor'))
      .finally(() => setLoading(false));
  }, [navigate]);

  async function carregar() {
    const [items, assignments] = await Promise.all([listarAtividades(), listarAtribuicoes()]);
    setAtividades(items);
    setAtribuicoes(assignments);
  }

  async function voltarLista(texto?: string) {
    if (texto) setMessage(texto);
    setSelecionada(null);
    setMode('LISTA');
    await carregar();
  }

  if (loading) return <main className="page-shell"><p>Carregando atividades…</p></main>;
  if (!user) return null;

  if (mode === 'EDITOR') {
    return (
      <main className="page-shell activity-shell">
        <AtividadeEditor
          atividade={selecionada}
          onSaved={(atividade) => {
            setSelecionada(atividade);
            setMessage('Atividade salva.');
          }}
          onCancel={() => void voltarLista()}
        />
      </main>
    );
  }

  if (mode === 'ATRIBUIR' && selecionada) {
    return (
      <main className="page-shell activity-shell">
        <AtribuirAtividadePanel
          atividade={selecionada}
          onDone={() => void voltarLista('Atividade atribuída com sucesso.')}
          onCancel={() => void voltarLista()}
        />
      </main>
    );
  }

  if (mode === 'RESULTADOS' && selecionada) {
    return (
      <main className="page-shell activity-shell">
        <ResultadosAtividadePanel
          atividade={selecionada}
          atribuicoes={atribuicoes}
          onClose={() => void voltarLista()}
        />
      </main>
    );
  }

  return (
    <main className="page-shell activity-shell">
      <section className="session-card">
        <div className="session-header">
          <div>
            <p className="card-kicker">v0.4 • Professor</p>
            <h1>Minhas Atividades</h1>
            <p>Crie, publique, atribua e acompanhe atividades de Matemática.</p>
          </div>
          <button className="primary-button" onClick={() => { setSelecionada(null); setMode('EDITOR'); }}>
            Nova atividade
          </button>
        </div>

        {message && <p className="form-message success" role="status">{message}</p>}

        <div className="activity-list">
          {atividades.length === 0 ? (
            <article className="card empty-state">
              <h2>Nenhuma atividade ainda</h2>
              <p>Comece criando uma atividade e depois adicione as questões.</p>
            </article>
          ) : atividades.map((atividade) => {
            const qtdAtribuicoes = atribuicoes.filter((a) => a.atividadeId === atividade.id).length;
            return (
              <article className="card activity-card" key={atividade.id}>
                <div>
                  <p className="card-kicker">{atividade.tipoOrigem === 'SISTEMA' ? 'Banco MathInclui' : 'Minha atividade'}</p>
                  <h2>{atividade.titulo}</h2>
                  <p>{atividade.anoEscolar}º ano • {atividade.conteudo} • {atividade.dificuldade}</p>
                  <p><strong>{atividade.publicada ? 'Publicada' : 'Rascunho'}</strong> • {atividade.limiteTentativas} tentativa(s) padrão • {qtdAtribuicoes} atribuição(ões)</p>
                </div>
                <div className="activity-actions">
                  {atividade.tipoOrigem === 'PROFESSOR' && (
                    <button className="secondary-button" onClick={() => { setSelecionada(atividade); setMode('EDITOR'); }}>
                      Editar
                    </button>
                  )}
                  {atividade.publicada && (
                    <button className="primary-button" onClick={() => { setSelecionada(atividade); setMode('ATRIBUIR'); }}>
                      Atribuir
                    </button>
                  )}
                  <button className="secondary-button" onClick={() => { setSelecionada(atividade); setMode('RESULTADOS'); }}>
                    Resultados
                  </button>
                </div>
              </article>
            );
          })}
        </div>
        <Link className="text-link" to="/app">Voltar ao painel</Link>
      </section>
    </main>
  );
}

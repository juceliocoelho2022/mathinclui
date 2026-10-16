import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fetchSession, getAccessToken } from '../../services/authApi';
import { listarAtividadesAluno, type AlunoAtividadeResumo } from '../../services/alunoAtividadesApi';

export function AlunoAtividadesPage() {
  const navigate = useNavigate();
  const [atividades, setAtividades] = useState<AlunoAtividadeResumo[]>([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!getAccessToken()) {
      navigate('/entrar/aluno');
      return;
    }
    fetchSession()
      .then(async (session) => {
        if (session.tipo !== 'ALUNO') {
          navigate('/app');
          return;
        }
        setAtividades(await listarAtividadesAluno());
      })
      .catch(() => {
        setMessage('Não foi possível carregar suas atividades.');
        navigate('/entrar/aluno');
      })
      .finally(() => setLoading(false));
  }, [navigate]);

  const grupos = useMemo(() => ({
    PARA_FAZER: atividades.filter((a) => a.estado === 'PARA_FAZER'),
    EM_ANDAMENTO: atividades.filter((a) => a.estado === 'EM_ANDAMENTO'),
    CONCLUIDA: atividades.filter((a) => a.estado === 'CONCLUIDA'),
  }), [atividades]);

  if (loading) return <main className="page-shell"><p>Carregando suas atividades…</p></main>;

  return (
    <main className="page-shell student-activities-shell">
      <section className="session-card">
        <div className="session-header">
          <div>
            <p className="card-kicker">v0.4 • Área do aluno</p>
            <h1>Minhas Atividades</h1>
            <p>Faça uma questão de cada vez, use as dicas e acompanhe seu próprio progresso.</p>
          </div>
          <Link className="secondary-button link-button" to="/app">Painel</Link>
        </div>
        {message && <p className="form-message error">{message}</p>}
        <Grupo titulo="Para fazer" items={grupos.PARA_FAZER} />
        <Grupo titulo="Em andamento" items={grupos.EM_ANDAMENTO} />
        <Grupo titulo="Concluídas" items={grupos.CONCLUIDA} />
      </section>
    </main>
  );
}

function Grupo({ titulo, items }: { titulo: string; items: AlunoAtividadeResumo[] }) {
  return (
    <section className="student-activity-group">
      <h2>{titulo}</h2>
      {items.length === 0 ? <p className="muted-copy">Nenhuma atividade nesta etapa.</p> : (
        <div className="activity-list">
          {items.map((atividade) => {
            const concluida = atividade.estado === 'CONCLUIDA' && atividade.tentativaId;
            const href = concluida
              ? `/app/tentativas/${atividade.tentativaId}/resultado`
              : `/app/atividades-aluno/${atividade.atribuicaoId}`;
            const verbo = atividade.estado === 'EM_ANDAMENTO' ? 'Continuar' : atividade.estado === 'CONCLUIDA' ? 'Ver resultado' : 'Iniciar';
            return (
              <article className="card activity-card" key={atividade.atribuicaoId}>
                <div>
                  <p className="card-kicker">{atividade.anoEscolar}º ano • {atividade.conteudo}</p>
                  <h3>{atividade.titulo}</h3>
                  {atividade.dataLimite && <small>Prazo: {new Date(atividade.dataLimite).toLocaleString('pt-BR')}</small>}
                </div>
                <Link className="primary-button link-button" aria-label={`${verbo} ${atividade.titulo}`} to={href}>{verbo}</Link>
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
}

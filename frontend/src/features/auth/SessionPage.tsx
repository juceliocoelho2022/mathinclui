import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fetchSession, getAccessToken, logout, type SessionUser } from '../../services/authApi';

export function SessionPage() {
  const navigate = useNavigate();
  const [user, setUser] = useState<SessionUser | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!getAccessToken()) {
      navigate('/');
      return;
    }
    fetchSession()
      .then(setUser)
      .catch(() => navigate('/'))
      .finally(() => setLoading(false));
  }, [navigate]);

  async function signOut() {
    await logout();
    navigate('/');
  }

  if (loading) return <main className="page-shell"><p>Carregando sua sessão…</p></main>;
  if (!user) return null;

  return (
    <main className="page-shell">
      <section className="session-card">
        <div className="session-header">
          <div>
            <p className="card-kicker">SESSÃO AUTENTICADA • v0.4</p>
            <h1>Olá, {user.nome} 👋</h1>
            <p>{user.tipo === 'PROFESSOR' ? 'Perfil de professor' : 'Perfil de aluno'} • {user.login}</p>
          </div>
          <button className="secondary-button" onClick={signOut}>Sair</button>
        </div>

        <div className="session-grid">
          <article className="card">
            <p className="card-kicker">Segurança</p>
            <h2>Acesso validado</h2>
            <p>JWT de acesso, refresh token com rotação e perfil <strong>{user.role}</strong>.</p>
          </article>
          {user.tipo === 'PROFESSOR' ? (
            <>
              <article className="card">
                <p className="card-kicker">Gestão escolar</p>
                <h2>Turmas, alunos e perfil pedagógico</h2>
                <p>Cadastre suas turmas e alunos e configure apoios pedagógicos individuais.</p>
                <Link className="primary-button link-button" to="/app/gestao">Abrir gestão escolar</Link>
              </article>
              <article className="card">
                <p className="card-kicker">Atividades</p>
                <h2>Criar, atribuir e acompanhar</h2>
                <p>Monte questões, publique para turmas ou alunos e consulte os resultados de aprendizagem.</p>
                <Link className="primary-button link-button" to="/app/atividades">Abrir atividades</Link>
              </article>
              <article className="card">
                <p className="card-kicker">Biblioteca • v0.5</p>
                <h2>Reutilizar conteúdos pedagógicos</h2>
                <p>Consulte atividades e questões oficiais, filtre por contexto pedagógico e crie cópias editáveis.</p>
                <Link className="primary-button link-button" to="/app/biblioteca">Abrir biblioteca</Link>
              </article>
            </>
          ) : (
            <article className="card">
              <p className="card-kicker">Aprendizagem</p>
              <h2>Suas atividades de Matemática</h2>
              <p>Resolva uma questão por vez, use dicas quando precisar e retome de onde parou.</p>
              <Link className="primary-button link-button" to="/app/atividades-aluno">Abrir minhas atividades</Link>
            </article>
          )}
        </div>
        <Link className="text-link" to="/">Voltar ao início</Link>
      </section>
    </main>
  );
}

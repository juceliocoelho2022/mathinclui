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

  if (loading) {
    return <main className="page-shell"><p>Carregando sua sessão…</p></main>;
  }

  if (!user) return null;

  return (
    <main className="page-shell">
      <section className="session-card">
        <div className="session-header">
          <div>
            <p className="card-kicker">SESSÃO AUTENTICADA • v0.3</p>
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
          <article className="card">
            <p className="card-kicker">Gestão escolar</p>
            <h2>Turmas, alunos e perfil pedagógico</h2>
            {user.tipo === 'PROFESSOR' ? (
              <>
                <p>Cadastre suas turmas e alunos e configure apoios pedagógicos individuais.</p>
                <Link className="primary-button link-button" to="/app/gestao">Abrir gestão escolar</Link>
              </>
            ) : (
              <p>Seu professor gerencia suas configurações pedagógicas de apoio.</p>
            )}
          </article>
        </div>
        <Link className="text-link" to="/">Voltar ao início</Link>
      </section>
    </main>
  );
}

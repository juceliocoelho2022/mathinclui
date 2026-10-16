import { useEffect, useState } from 'react';
import { Link, Route, Routes } from 'react-router-dom';
import { AuthPage } from './features/auth/AuthPage';
import { SessionPage } from './features/auth/SessionPage';
import { GestaoEscolarPage } from './features/gestao/GestaoEscolarPage';
import { AtividadesProfessorPage } from './features/atividades/AtividadesProfessorPage';
import { AlunoAtividadesPage } from './features/aluno/AlunoAtividadesPage';
import { ResolverAtividadePage } from './features/aluno/ResolverAtividadePage';
import { ResultadoAlunoPage } from './features/aluno/ResultadoAlunoPage';
import { fetchSystemInfo, type SystemInfo } from './services/systemApi';
import { statusLabel } from './utils/statusLabel';
import './styles.css';
import './v04.css';

const principles = [
  'Apoio visual e linguagem direta',
  'Aprendizagem por diferentes caminhos',
  'Progresso individual, sem ranking competitivo',
  'Tecnologia a serviço da inclusão pedagógica',
];

function HomePage() {
  const [systemInfo, setSystemInfo] = useState<SystemInfo | null>(null);
  const [backendStatus, setBackendStatus] = useState('UNKNOWN');

  useEffect(() => {
    const controller = new AbortController();
    fetchSystemInfo(controller.signal)
      .then((info) => {
        setSystemInfo(info);
        setBackendStatus(info.status);
      })
      .catch(() => setBackendStatus('DOWN'));
    return () => controller.abort();
  }, []);

  return (
    <main className="page-shell">
      <section className="hero" aria-labelledby="page-title">
        <div className="brand-mark" aria-hidden="true">∑</div>
        <div>
          <p className="eyebrow">MATEMÁTICA • INCLUSÃO • TECNOLOGIA</p>
          <h1 id="page-title">MathInclui</h1>
          <p className="subtitle">Matemática por diferentes caminhos.</p>
          <p className="description">
            Uma plataforma para professores criarem experiências de Matemática mais acessíveis,
            com dicas, retentativas e acompanhamento do progresso individual.
          </p>
          <div className="hero-actions">
            <Link className="primary-button link-button" to="/entrar/professor">Entrar como professor</Link>
            <Link className="secondary-button link-button light" to="/entrar/aluno">Entrar como aluno</Link>
          </div>
        </div>
      </section>

      <section className="grid" aria-label="Visão da versão 0.4">
        <article className="card status-card">
          <p className="card-kicker">v0.4 • Atividades e progresso</p>
          <h2>Do planejamento ao acompanhamento</h2>
          <p>Professor cria e atribui atividades; aluno resolve com apoio progressivo; resultados ficam disponíveis para acompanhamento.</p>
          <div className={`status status-${backendStatus.toLowerCase()}`} role="status" aria-live="polite">
            <span className="status-dot" aria-hidden="true" />
            <span>{statusLabel(backendStatus)}</span>
          </div>
          {systemInfo && <small>{systemInfo.name} API • versão {systemInfo.version}</small>}
        </article>

        <article className="card">
          <p className="card-kicker">Princípios</p>
          <h2>Inclusão sem rotular</h2>
          <ul>{principles.map((item) => <li key={item}>{item}</li>)}</ul>
        </article>
      </section>

      <section className="next-steps" aria-labelledby="next-title">
        <p className="card-kicker">Versão atual</p>
        <h2 id="next-title">Atividades, tentativas, dicas e resultados</h2>
        <p>Na v0.4, o ciclo pedagógico conecta professor e aluno sem expor gabaritos antes da hora e sem competição entre estudantes.</p>
      </section>
    </main>
  );
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/entrar/professor" element={<AuthPage mode="professor-login" />} />
      <Route path="/entrar/aluno" element={<AuthPage mode="aluno-login" />} />
      <Route path="/cadastro/professor" element={<AuthPage mode="professor-register" />} />
      <Route path="/recuperar-senha" element={<AuthPage mode="forgot" />} />
      <Route path="/redefinir-senha" element={<AuthPage mode="reset" />} />
      <Route path="/app" element={<SessionPage />} />
      <Route path="/app/gestao" element={<GestaoEscolarPage />} />
      <Route path="/app/atividades" element={<AtividadesProfessorPage />} />
      <Route path="/app/atividades-aluno" element={<AlunoAtividadesPage />} />
      <Route path="/app/atividades-aluno/:atribuicaoId" element={<ResolverAtividadePage />} />
      <Route path="/app/tentativas/:tentativaId/resultado" element={<ResultadoAlunoPage />} />
      <Route path="*" element={<HomePage />} />
    </Routes>
  );
}

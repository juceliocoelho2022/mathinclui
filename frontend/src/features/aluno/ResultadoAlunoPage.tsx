import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { fetchSession, getAccessToken } from '../../services/authApi';
import { buscarResultadoAluno, type TentativaResultado } from '../../services/alunoAtividadesApi';

export function ResultadoAlunoPage() {
  const navigate = useNavigate();
  const { tentativaId } = useParams();
  const id = Number(tentativaId);
  const [resultado, setResultado] = useState<TentativaResultado | null>(null);
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!getAccessToken() || !Number.isFinite(id)) {
      navigate('/entrar/aluno');
      return;
    }
    fetchSession()
      .then(async (session) => {
        if (session.tipo !== 'ALUNO') {
          navigate('/app');
          return;
        }
        setResultado(await buscarResultadoAluno(id));
      })
      .catch(() => setMessage('Não foi possível carregar o resultado.'));
  }, [id, navigate]);

  if (!resultado) return <main className="page-shell"><p>{message || 'Carregando resultado…'}</p></main>;

  return (
    <main className="page-shell result-student-shell">
      <section className="session-card student-result-card">
        <p className="card-kicker">Seu progresso</p>
        <h1>Atividade concluída</h1>
        <p className="result-score">{Math.round(resultado.percentual ?? 0)}%</p>
        <p><strong>{resultado.quantidadeAcertos} acerto(s)</strong> • {resultado.quantidadeErros} questão(ões) para revisar</p>
        <p>Tempo registrado: {resultado.tempoTotalSegundos} segundo(s).</p>
        <p>Use este resultado para perceber o que já domina e o que merece outra explicação.</p>
        <Link className="primary-button link-button" to="/app/atividades-aluno">Voltar às minhas atividades</Link>
      </section>
    </main>
  );
}

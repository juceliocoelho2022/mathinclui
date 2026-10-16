import { useEffect, useMemo, useState } from 'react';
import { buscarResultados, type Atividade, type AtividadeResultados, type Atribuicao } from '../../services/atividadesApi';

type Props = { atividade: Atividade; atribuicoes: Atribuicao[]; onClose: () => void };

export function ResultadosAtividadePanel({ atividade, atribuicoes, onClose }: Props) {
  const disponiveis = useMemo(() => atribuicoes.filter((a) => a.atividadeId === atividade.id), [atribuicoes, atividade.id]);
  const [atribuicaoId, setAtribuicaoId] = useState<number | null>(disponiveis[0]?.id ?? null);
  const [resultado, setResultado] = useState<AtividadeResultados | null>(null);
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!atribuicaoId) {
      setResultado(null);
      return;
    }
    buscarResultados(atribuicaoId).then(setResultado).catch(() => setMessage('Não foi possível carregar os resultados.'));
  }, [atribuicaoId]);

  const pct = (value?: number | null) => `${Math.round(value ?? 0)}%`;

  return (
    <section className="session-card results-panel">
      <div className="session-header">
        <div><p className="card-kicker">Resultados</p><h1>{atividade.titulo}</h1><p>Acompanhe participação e dificuldades sem comparação competitiva entre alunos.</p></div>
        <button className="secondary-button" type="button" onClick={onClose}>Voltar</button>
      </div>

      {disponiveis.length === 0 ? <p className="form-message">A atividade ainda não possui atribuições.</p> : (
        <label className="stacked-label">Atribuição
          <select value={atribuicaoId ?? ''} onChange={(e) => setAtribuicaoId(Number(e.target.value))}>
            {disponiveis.map((a) => <option value={a.id} key={a.id}>#{a.id} • {a.turmaId ? `Turma ${a.turmaId}` : `Aluno ${a.alunoId}`}</option>)}
          </select>
        </label>
      )}
      {message && <p className="form-message" role="status">{message}</p>}

      {resultado && (
        <>
          <div className="metrics-grid">
            <article className="metric-card"><span>Participação</span><strong>{pct(resultado.participacaoPercentual)}</strong><small>{resultado.participantes} de {resultado.totalAlunos}</small></article>
            <article className="metric-card"><span>Média de acertos</span><strong>{pct(resultado.mediaPercentual)}</strong><small>{resultado.concluidos} concluída(s)</small></article>
          </div>
          <section className="results-section">
            <h2>Por aluno</h2>
            <div className="table-scroll"><table><thead><tr><th>Aluno</th><th>Status</th><th>Acertos</th><th>Tentativas</th><th>Dicas</th><th>Tempo</th></tr></thead><tbody>
              {resultado.alunos.map((aluno) => <tr key={aluno.alunoId}><td>{aluno.nome}</td><td>{aluno.status}</td><td>{pct(aluno.percentual)} de acertos</td><td>{aluno.totalSubmissoes}</td><td>{aluno.totalDicas}</td><td>{aluno.tempoTotalSegundos}s</td></tr>)}
            </tbody></table></div>
          </section>
          <section className="results-section">
            <h2>Questões que pedem atenção</h2>
            <div className="question-results">
              {resultado.questoes.map((q) => <article className="question-summary" key={q.questaoId}><strong>{q.enunciado}</strong><span>{q.acertos} acerto(s) • {q.errosFinais} erro(s) final(is) • {q.totalSubmissoes} envio(s)</span></article>)}
            </div>
          </section>
        </>
      )}
    </section>
  );
}

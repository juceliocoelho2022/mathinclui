import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { fetchSession, getAccessToken } from '../../services/authApi';
import {
  detalharAtividadeAluno,
  iniciarAtividade,
  responderQuestao,
  type AlunoAtividadeDetalhe,
  type RespostaQuestaoResultado,
} from '../../services/alunoAtividadesApi';
import { QuestaoCard } from './QuestaoCard';

export function ResolverAtividadePage() {
  const navigate = useNavigate();
  const { atribuicaoId } = useParams();
  const id = Number(atribuicaoId);
  const [atividade, setAtividade] = useState<AlunoAtividadeDetalhe | null>(null);
  const [tentativaId, setTentativaId] = useState<number | null>(null);
  const [indice, setIndice] = useState(0);
  const [resposta, setResposta] = useState('');
  const [feedback, setFeedback] = useState<RespostaQuestaoResultado | null>(null);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState('');
  const [inicioQuestao, setInicioQuestao] = useState(() => Date.now());

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
        const detalhe = await detalharAtividadeAluno(id);
        setAtividade(detalhe);
        const primeiraAberta = detalhe.questoes.findIndex((q) => !q.encerrada);
        setIndice(primeiraAberta >= 0 ? primeiraAberta : 0);
        if (detalhe.tentativaId) {
          setTentativaId(detalhe.tentativaId);
        } else if (detalhe.estado !== 'CONCLUIDA') {
          const tentativa = await iniciarAtividade(id);
          setTentativaId(tentativa.id);
        }
      })
      .catch(() => setMessage('Não foi possível abrir esta atividade.'));
  }, [id, navigate]);

  const questao = atividade?.questoes[indice] ?? null;
  const total = atividade?.questoes.length ?? 0;
  const fim = useMemo(() => Boolean(feedback?.atividadeFinalizada || atividade?.estado === 'CONCLUIDA'), [feedback, atividade?.estado]);

  async function enviar() {
    if (!questao || !tentativaId || !resposta.trim() || busy) return;
    setBusy(true);
    try {
      const segundos = Math.max(0, Math.round((Date.now() - inicioQuestao) / 1000));
      const result = await responderQuestao(tentativaId, questao.id, { resposta, tempoSegundos: segundos });
      setFeedback(result);
      if (!result.encerrada) {
        setAtividade((prev) => prev ? {
          ...prev,
          questoes: prev.questoes.map((q) => q.id === questao.id
            ? { ...q, tentativasUsadas: result.tentativasUsadas, dicasLiberadas: result.dicasLiberadas, dicaAtual: result.dica }
            : q),
        } : prev);
      }
    } catch {
      setMessage('Não foi possível registrar sua resposta. Tente novamente.');
    } finally {
      setBusy(false);
    }
  }

  function proxima() {
    if (!atividade) return;
    const proximoIndice = atividade.questoes.findIndex((q, idx) => idx > indice && !q.encerrada);
    const fallback = indice + 1 < atividade.questoes.length ? indice + 1 : indice;
    setIndice(proximoIndice >= 0 ? proximoIndice : fallback);
    setResposta('');
    setFeedback(null);
    setInicioQuestao(Date.now());
  }

  if (!atividade || !questao) {
    return <main className="page-shell"><p>{message || 'Preparando atividade…'}</p></main>;
  }

  return (
    <main className="page-shell solver-shell">
      <section className="session-card">
        <div className="solver-header">
          <div><p className="card-kicker">{atividade.conteudo}</p><h1>{atividade.titulo}</h1></div>
          <span className="progress-pill">Questão {indice + 1} de {total}</span>
        </div>
        {message && <p className="form-message error">{message}</p>}
        <div className="progress-track" aria-hidden="true"><span style={{ width: `${((indice + 1) / total) * 100}%` }} /></div>
        <QuestaoCard
          questao={questao}
          perfil={atividade.perfilPedagogico}
          resposta={resposta}
          onResposta={setResposta}
          onResponder={enviar}
          feedback={feedback}
          busy={busy}
        />
        <div className="solver-actions">
          {feedback?.encerrada && !fim && indice + 1 < total && <button className="primary-button" onClick={proxima}>Próxima questão</button>}
          {fim && tentativaId && <Link className="primary-button link-button" to={`/app/tentativas/${tentativaId}/resultado`}>Ver meu resultado</Link>}
          <Link className="text-link" to="/app/atividades-aluno">Voltar às atividades</Link>
        </div>
      </section>
    </main>
  );
}

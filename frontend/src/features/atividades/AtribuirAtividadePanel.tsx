import { FormEvent, useEffect, useState } from 'react';
import { atribuirAtividade, type Atividade } from '../../services/atividadesApi';
import { listarAlunos, listarTurmas, type Aluno, type Turma } from '../../services/gestaoEscolarApi';
import { salvarAdaptacaoGeral, salvarAdaptacaoIndividual, temAdaptacao } from '../../services/acessibilidadeApi';
import { AdaptacaoAtribuicaoPanel, type AdaptacaoAtribuicaoDraft } from './AdaptacaoAtribuicaoPanel';

type Props = { atividade: Atividade; onDone: () => void; onCancel: () => void };
type Destino = 'TURMA' | 'ALUNO';

const planoVazio: AdaptacaoAtribuicaoDraft = {
  geral: {
    tamanhoFonteMinima: null,
    leituraVozMinima: null,
    apoioVisualMinimo: null,
    dicasPassoAPasso: null,
    repeticaoGuiada: null,
    questoesPorTela: null,
    tempoAmpliado: null,
    nivelApoio: null,
  },
  individuais: [],
};

export function AtribuirAtividadePanel({ atividade, onDone, onCancel }: Props) {
  const [destino, setDestino] = useState<Destino>('TURMA');
  const [turmas, setTurmas] = useState<Turma[]>([]);
  const [turmaId, setTurmaId] = useState<number | null>(null);
  const [alunos, setAlunos] = useState<Aluno[]>([]);
  const [alunoId, setAlunoId] = useState<number | null>(null);
  const [dataInicio, setDataInicio] = useState('');
  const [dataLimite, setDataLimite] = useState('');
  const [adaptacoes, setAdaptacoes] = useState<AdaptacaoAtribuicaoDraft>(planoVazio);
  const [message, setMessage] = useState('');
  const [partialError, setPartialError] = useState('');
  const [createdAssignmentId, setCreatedAssignmentId] = useState<number | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    listarTurmas().then((items) => {
      setTurmas(items);
      if (items.length) setTurmaId(items[0].id);
    }).catch(() => setMessage('Não foi possível carregar as turmas.'));
  }, []);

  useEffect(() => {
    if (destino !== 'ALUNO' || !turmaId) {
      setAlunos([]);
      setAlunoId(null);
      return;
    }
    listarAlunos(turmaId).then((items) => {
      setAlunos(items);
      setAlunoId(items[0]?.id ?? null);
    }).catch(() => setMessage('Não foi possível carregar os alunos.'));
  }, [destino, turmaId]);

  async function salvarPlano(atribuicaoId: number) {
    if (temAdaptacao(adaptacoes.geral)) {
      await salvarAdaptacaoGeral(atribuicaoId, adaptacoes.geral);
    }
    if (destino === 'TURMA') {
      for (const item of adaptacoes.individuais) {
        if (temAdaptacao(item.adaptacao)) {
          await salvarAdaptacaoIndividual(atribuicaoId, item.alunoId, item.adaptacao);
        }
      }
    }
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (busy || createdAssignmentId) return;
    if (destino === 'TURMA' && !turmaId) return setMessage('Selecione uma turma.');
    if (destino === 'ALUNO' && !alunoId) return setMessage('Selecione um aluno.');
    setBusy(true);
    setMessage('');
    setPartialError('');
    try {
      const created = await atribuirAtividade({
        atividadeId: atividade.id,
        ...(destino === 'TURMA' ? { turmaId: turmaId! } : { alunoId: alunoId! }),
        ...(dataInicio ? { dataInicio: new Date(dataInicio).toISOString() } : {}),
        ...(dataLimite ? { dataLimite: new Date(dataLimite).toISOString() } : {}),
      });
      try {
        await salvarPlano(created.id);
        onDone();
      } catch {
        setCreatedAssignmentId(created.id);
        setPartialError('A atribuição foi criada, mas a adaptação não foi salva. Você pode tentar salvar a adaptação novamente sem duplicar a atribuição.');
      }
    } catch {
      setMessage('Não foi possível atribuir a atividade. Verifique destino e datas.');
    } finally {
      setBusy(false);
    }
  }

  async function retryAdaptacao() {
    if (!createdAssignmentId || busy) return;
    setBusy(true);
    setPartialError('');
    try {
      await salvarPlano(createdAssignmentId);
      onDone();
    } catch {
      setPartialError('A atribuição foi criada, mas a adaptação não foi salva. Tente novamente mais tarde.');
    } finally {
      setBusy(false);
    }
  }

  function mudarDestino(next: Destino) {
    setDestino(next);
    if (next === 'ALUNO') {
      setAdaptacoes((current) => ({ ...current, individuais: [] }));
    }
  }

  return (
    <section className="session-card assignment-panel">
      <div className="session-header">
        <div><p className="card-kicker">Atribuição</p><h1>{atividade.titulo}</h1><p>Envie para uma turma inteira ou para um aluno específico e defina apoios adicionais quando necessário.</p></div>
        <button className="secondary-button" type="button" onClick={onCancel}>Voltar</button>
      </div>
      {message && <p className="form-message" role="status">{message}</p>}
      {partialError && (
        <div className="partial-save-error">
          <p className="form-message error" role="alert">{partialError}</p>
          <button className="secondary-button" type="button" disabled={busy} onClick={() => void retryAdaptacao()}>Tentar salvar adaptação novamente</button>
        </div>
      )}
      <form className="auth-form" onSubmit={submit}>
        <label>Destino
          <select value={destino} onChange={(e) => mudarDestino(e.target.value as Destino)} disabled={Boolean(createdAssignmentId)}>
            <option value="TURMA">Turma inteira</option><option value="ALUNO">Aluno específico</option>
          </select>
        </label>
        <label>{destino === 'ALUNO' ? 'Turma para localizar o aluno' : 'Turma'}
          <select value={turmaId ?? ''} onChange={(e) => setTurmaId(Number(e.target.value))} required disabled={Boolean(createdAssignmentId)}>
            <option value="" disabled>Selecione</option>
            {turmas.map((turma) => <option value={turma.id} key={turma.id}>{turma.nome} • {turma.anoEscolar}º ano</option>)}
          </select>
        </label>
        {destino === 'ALUNO' && (
          <label>Aluno
            <select value={alunoId ?? ''} onChange={(e) => setAlunoId(Number(e.target.value))} required disabled={Boolean(createdAssignmentId)}>
              <option value="" disabled>Selecione</option>
              {alunos.map((aluno) => <option value={aluno.id} key={aluno.id}>{aluno.nome}</option>)}
            </select>
          </label>
        )}
        <div className="form-row">
          <label>Início (opcional)<input type="datetime-local" value={dataInicio} onChange={(e) => setDataInicio(e.target.value)} disabled={Boolean(createdAssignmentId)} /></label>
          <label>Prazo (opcional)<input type="datetime-local" value={dataLimite} onChange={(e) => setDataLimite(e.target.value)} disabled={Boolean(createdAssignmentId)} /></label>
        </div>

        <AdaptacaoAtribuicaoPanel destino={destino} turmaId={turmaId} onChange={setAdaptacoes} />

        {!createdAssignmentId && <button className="primary-button" disabled={busy}>{busy ? 'Atribuindo…' : 'Atribuir atividade'}</button>}
      </form>
    </section>
  );
}

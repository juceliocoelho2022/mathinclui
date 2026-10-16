import { FormEvent, useEffect, useState } from 'react';
import { atribuirAtividade, type Atividade } from '../../services/atividadesApi';
import { listarAlunos, listarTurmas, type Aluno, type Turma } from '../../services/gestaoEscolarApi';

type Props = { atividade: Atividade; onDone: () => void; onCancel: () => void };
type Destino = 'TURMA' | 'ALUNO';

export function AtribuirAtividadePanel({ atividade, onDone, onCancel }: Props) {
  const [destino, setDestino] = useState<Destino>('TURMA');
  const [turmas, setTurmas] = useState<Turma[]>([]);
  const [turmaId, setTurmaId] = useState<number | null>(null);
  const [alunos, setAlunos] = useState<Aluno[]>([]);
  const [alunoId, setAlunoId] = useState<number | null>(null);
  const [dataInicio, setDataInicio] = useState('');
  const [dataLimite, setDataLimite] = useState('');
  const [message, setMessage] = useState('');

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
    });
  }, [destino, turmaId]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (destino === 'TURMA' && !turmaId) return setMessage('Selecione uma turma.');
    if (destino === 'ALUNO' && !alunoId) return setMessage('Selecione um aluno.');
    try {
      await atribuirAtividade({
        atividadeId: atividade.id,
        ...(destino === 'TURMA' ? { turmaId: turmaId! } : { alunoId: alunoId! }),
        ...(dataInicio ? { dataInicio: new Date(dataInicio).toISOString() } : {}),
        ...(dataLimite ? { dataLimite: new Date(dataLimite).toISOString() } : {}),
      });
      onDone();
    } catch {
      setMessage('Não foi possível atribuir a atividade. Verifique destino e datas.');
    }
  }

  return (
    <section className="session-card assignment-panel">
      <div className="session-header">
        <div><p className="card-kicker">Atribuição</p><h1>{atividade.titulo}</h1><p>Envie para uma turma inteira ou para um aluno específico.</p></div>
        <button className="secondary-button" type="button" onClick={onCancel}>Voltar</button>
      </div>
      {message && <p className="form-message" role="status">{message}</p>}
      <form className="auth-form" onSubmit={submit}>
        <label>Destino
          <select value={destino} onChange={(e) => setDestino(e.target.value as Destino)}>
            <option value="TURMA">Turma inteira</option><option value="ALUNO">Aluno específico</option>
          </select>
        </label>
        <label>{destino === 'ALUNO' ? 'Turma para localizar o aluno' : 'Turma'}
          <select value={turmaId ?? ''} onChange={(e) => setTurmaId(Number(e.target.value))} required>
            <option value="" disabled>Selecione</option>
            {turmas.map((turma) => <option value={turma.id} key={turma.id}>{turma.nome} • {turma.anoEscolar}º ano</option>)}
          </select>
        </label>
        {destino === 'ALUNO' && (
          <label>Aluno
            <select value={alunoId ?? ''} onChange={(e) => setAlunoId(Number(e.target.value))} required>
              <option value="" disabled>Selecione</option>
              {alunos.map((aluno) => <option value={aluno.id} key={aluno.id}>{aluno.nome}</option>)}
            </select>
          </label>
        )}
        <div className="form-row">
          <label>Início (opcional)<input type="datetime-local" value={dataInicio} onChange={(e) => setDataInicio(e.target.value)} /></label>
          <label>Prazo (opcional)<input type="datetime-local" value={dataLimite} onChange={(e) => setDataLimite(e.target.value)} /></label>
        </div>
        <button className="primary-button">Atribuir atividade</button>
      </form>
    </section>
  );
}

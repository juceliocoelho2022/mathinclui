import { FormEvent, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fetchSession, getAccessToken, type SessionUser } from '../../services/authApi';
import {
  buscarPerfilPedagogico,
  criarAluno,
  criarTurma,
  listarAlunos,
  listarTurmas,
  redefinirPin,
  atualizarPerfilPedagogico,
  type Aluno,
  type PerfilPedagogico,
  type Turma,
} from '../../services/gestaoEscolarApi';

const defaultProfile: Omit<PerfilPedagogico, 'id' | 'alunoId'> = {
  tamanhoFonte: 'PADRAO',
  leituraVoz: false,
  apoioVisual: false,
  dicasPassoAPasso: false,
  repeticaoGuiada: false,
  questoesPorTela: 1,
  tempoAmpliado: false,
  nivelApoio: 'PADRAO',
};

export function GestaoEscolarPage() {
  const navigate = useNavigate();
  const [user, setUser] = useState<SessionUser | null>(null);
  const [turmas, setTurmas] = useState<Turma[]>([]);
  const [turmaId, setTurmaId] = useState<number | null>(null);
  const [alunos, setAlunos] = useState<Aluno[]>([]);
  const [alunoId, setAlunoId] = useState<number | null>(null);
  const [perfil, setPerfil] = useState(defaultProfile);
  const [nomeTurma, setNomeTurma] = useState('');
  const [anoEscolar, setAnoEscolar] = useState(6);
  const [codigoTurma, setCodigoTurma] = useState('');
  const [nomeAluno, setNomeAluno] = useState('');
  const [username, setUsername] = useState('');
  const [pin, setPin] = useState('');
  const [novoPin, setNovoPin] = useState('');
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!getAccessToken()) {
      navigate('/entrar/professor');
      return;
    }
    fetchSession().then((session) => {
      if (session.tipo !== 'PROFESSOR') {
        navigate('/app');
        return;
      }
      setUser(session);
      carregarTurmas();
    }).catch(() => navigate('/entrar/professor'));
  }, [navigate]);

  async function carregarTurmas() {
    const data = await listarTurmas();
    setTurmas(data);
    if (data.length && turmaId === null) setTurmaId(data[0].id);
  }

  useEffect(() => {
    if (!turmaId) {
      setAlunos([]);
      return;
    }
    listarAlunos(turmaId).then(setAlunos);
    setAlunoId(null);
  }, [turmaId]);

  useEffect(() => {
    if (!alunoId) {
      setPerfil(defaultProfile);
      return;
    }
    buscarPerfilPedagogico(alunoId).then((data) => {
      const { id: _id, alunoId: _alunoId, ...settings } = data;
      setPerfil(settings);
    });
  }, [alunoId]);

  async function submitTurma(event: FormEvent) {
    event.preventDefault();
    const criada = await criarTurma({ nome: nomeTurma, anoEscolar, codigo: codigoTurma });
    setNomeTurma('');
    setCodigoTurma('');
    await carregarTurmas();
    setTurmaId(criada.id);
    setMessage('Turma criada com sucesso.');
  }

  async function submitAluno(event: FormEvent) {
    event.preventDefault();
    if (!turmaId) return;
    const criado = await criarAluno(turmaId, { nome: nomeAluno, username, pin });
    setNomeAluno('');
    setUsername('');
    setPin('');
    setAlunos(await listarAlunos(turmaId));
    setAlunoId(criado.id);
    setMessage('Aluno cadastrado com perfil pedagógico padrão.');
  }

  async function saveProfile(event: FormEvent) {
    event.preventDefault();
    if (!alunoId) return;
    await atualizarPerfilPedagogico(alunoId, perfil);
    setMessage('Perfil pedagógico atualizado.');
  }

  async function changePin(event: FormEvent) {
    event.preventDefault();
    if (!alunoId) return;
    await redefinirPin(alunoId, novoPin);
    setNovoPin('');
    setMessage('PIN redefinido com sucesso.');
  }

  const selectedStudent = alunos.find((item) => item.id === alunoId);

  return (
    <main className="page-shell">
      <section className="session-card">
        <div className="session-header">
          <div>
            <p className="card-kicker">GESTÃO ESCOLAR • v0.3</p>
            <h1>Turmas e alunos</h1>
            <p>{user ? `Professor: ${user.nome}` : 'Carregando professor…'}</p>
          </div>
          <Link className="secondary-button link-button" to="/app">Voltar ao painel</Link>
        </div>

        {message && <div className="form-message success" role="status">{message}</div>}

        <div className="management-grid">
          <section className="card">
            <p className="card-kicker">Turmas</p>
            <h2>Minhas turmas</h2>
            <form className="auth-form compact-form" onSubmit={submitTurma}>
              <label>Nome da turma<input value={nomeTurma} onChange={(e) => setNomeTurma(e.target.value)} required /></label>
              <label>Ano escolar
                <select value={anoEscolar} onChange={(e) => setAnoEscolar(Number(e.target.value))}>
                  <option value={6}>6º ano</option><option value={7}>7º ano</option><option value={8}>8º ano</option>
                </select>
              </label>
              <label>Código<input value={codigoTurma} onChange={(e) => setCodigoTurma(e.target.value)} required pattern="[A-Za-z0-9._-]{2,32}" /></label>
              <button className="primary-button" type="submit">Criar turma</button>
            </form>
            <label className="stacked-label">Turma selecionada
              <select value={turmaId ?? ''} onChange={(e) => setTurmaId(Number(e.target.value))}>
                <option value="" disabled>Selecione</option>
                {turmas.map((turma) => <option key={turma.id} value={turma.id}>{turma.nome} • {turma.anoEscolar}º</option>)}
              </select>
            </label>
          </section>

          <section className="card">
            <p className="card-kicker">Alunos</p>
            <h2>Cadastro individual</h2>
            <form className="auth-form compact-form" onSubmit={submitAluno}>
              <label>Nome do aluno<input value={nomeAluno} onChange={(e) => setNomeAluno(e.target.value)} required disabled={!turmaId} /></label>
              <label>Usuário<input value={username} onChange={(e) => setUsername(e.target.value)} required disabled={!turmaId} /></label>
              <label>PIN de 4 a 6 dígitos<input value={pin} onChange={(e) => setPin(e.target.value.replace(/\D/g, '').slice(0, 6))} required pattern="[0-9]{4,6}" disabled={!turmaId} /></label>
              <button className="primary-button" type="submit" disabled={!turmaId}>Cadastrar aluno</button>
            </form>
            <label className="stacked-label">Aluno selecionado
              <select value={alunoId ?? ''} onChange={(e) => setAlunoId(Number(e.target.value))} disabled={!alunos.length}>
                <option value="" disabled>Selecione</option>
                {alunos.map((aluno) => <option key={aluno.id} value={aluno.id}>{aluno.nome} • @{aluno.username}</option>)}
              </select>
            </label>
          </section>
        </div>

        <section className="card profile-card">
          <p className="card-kicker">Perfil pedagógico</p>
          <h2>{selectedStudent ? `Apoios de ${selectedStudent.nome}` : 'Selecione um aluno'}</h2>
          <p>Configure apoios pedagógicos sem registrar diagnóstico ou laudo.</p>
          <form className="profile-form" onSubmit={saveProfile}>
            <label>Tamanho da fonte
              <select value={perfil.tamanhoFonte} onChange={(e) => setPerfil({ ...perfil, tamanhoFonte: e.target.value as PerfilPedagogico['tamanhoFonte'] })} disabled={!alunoId}>
                <option value="PADRAO">Padrão</option><option value="GRANDE">Grande</option><option value="EXTRA_GRANDE">Extra grande</option>
              </select>
            </label>
            <label>Nível de apoio
              <select value={perfil.nivelApoio} onChange={(e) => setPerfil({ ...perfil, nivelApoio: e.target.value as PerfilPedagogico['nivelApoio'] })} disabled={!alunoId}>
                <option value="PADRAO">Padrão</option><option value="MODERADO">Moderado</option><option value="AMPLIADO">Ampliado</option>
              </select>
            </label>
            <label>Questões por tela<input type="number" min={1} max={10} value={perfil.questoesPorTela} onChange={(e) => setPerfil({ ...perfil, questoesPorTela: Number(e.target.value) })} disabled={!alunoId} /></label>
            <label className="check-row"><input type="checkbox" checked={perfil.leituraVoz} onChange={(e) => setPerfil({ ...perfil, leituraVoz: e.target.checked })} disabled={!alunoId} />Leitura em voz alta</label>
            <label className="check-row"><input type="checkbox" checked={perfil.apoioVisual} onChange={(e) => setPerfil({ ...perfil, apoioVisual: e.target.checked })} disabled={!alunoId} />Apoio visual</label>
            <label className="check-row"><input type="checkbox" checked={perfil.dicasPassoAPasso} onChange={(e) => setPerfil({ ...perfil, dicasPassoAPasso: e.target.checked })} disabled={!alunoId} />Dicas passo a passo</label>
            <label className="check-row"><input type="checkbox" checked={perfil.repeticaoGuiada} onChange={(e) => setPerfil({ ...perfil, repeticaoGuiada: e.target.checked })} disabled={!alunoId} />Repetição guiada</label>
            <label className="check-row"><input type="checkbox" checked={perfil.tempoAmpliado} onChange={(e) => setPerfil({ ...perfil, tempoAmpliado: e.target.checked })} disabled={!alunoId} />Tempo ampliado</label>
            <button className="primary-button" type="submit" disabled={!alunoId}>Salvar perfil pedagógico</button>
          </form>

          <form className="pin-form" onSubmit={changePin}>
            <label>Novo PIN<input value={novoPin} onChange={(e) => setNovoPin(e.target.value.replace(/\D/g, '').slice(0, 6))} pattern="[0-9]{4,6}" required disabled={!alunoId} /></label>
            <button className="secondary-button" type="submit" disabled={!alunoId}>Redefinir PIN</button>
          </form>
        </section>
      </section>
    </main>
  );
}

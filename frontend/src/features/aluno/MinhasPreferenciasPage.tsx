import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fetchSession, getAccessToken } from '../../services/authApi';
import {
  buscarPreferenciasInterface,
  salvarPreferenciasInterface,
  type PreferenciaInterface,
  type TamanhoFonte,
} from '../../services/acessibilidadeApi';

const defaultPreferences: PreferenciaInterface = {
  tamanhoFonte: 'PADRAO',
  leituraVoz: false,
  apoioVisual: false,
};

export function MinhasPreferenciasPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState<PreferenciaInterface>(defaultPreferences);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
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
        setForm(await buscarPreferenciasInterface());
      })
      .catch(() => setMessage('Não foi possível carregar suas preferências.'))
      .finally(() => setLoading(false));
  }, [navigate]);

  async function salvar() {
    if (busy) return;
    setBusy(true);
    setMessage('');
    try {
      setForm(await salvarPreferenciasInterface(form));
      setMessage('Preferências salvas.');
    } catch {
      setMessage('Não foi possível salvar suas preferências.');
    } finally {
      setBusy(false);
    }
  }

  if (loading) return <main className="page-shell"><p>Carregando preferências…</p></main>;

  return (
    <main className="page-shell preferences-shell">
      <section className="session-card preferences-card">
        <div className="session-header">
          <div>
            <p className="card-kicker">Acessibilidade • v0.5</p>
            <h1>Minhas preferências</h1>
            <p>Ajuste como você prefere usar o MathInclui. Algumas atividades podem manter apoios mínimos definidos pelo professor.</p>
          </div>
          <Link className="secondary-button link-button" to="/app">Voltar ao painel</Link>
        </div>

        {message && <p className="form-message" role="status">{message}</p>}

        <div className="auth-form preferences-form">
          <label>Tamanho do texto
            <select value={form.tamanhoFonte} onChange={(e) => setForm((current) => ({ ...current, tamanhoFonte: e.target.value as TamanhoFonte }))}>
              <option value="PADRAO">Padrão</option>
              <option value="GRANDE">Grande</option>
              <option value="EXTRA_GRANDE">Extra grande</option>
            </select>
          </label>
          <label className="check-row"><input type="checkbox" checked={form.leituraVoz} onChange={(e) => setForm((current) => ({ ...current, leituraVoz: e.target.checked }))} />Leitura em voz</label>
          <label className="check-row"><input type="checkbox" checked={form.apoioVisual} onChange={(e) => setForm((current) => ({ ...current, apoioVisual: e.target.checked }))} />Apoio visual</label>
          <button className="primary-button" type="button" disabled={busy} onClick={() => void salvar()}>{busy ? 'Salvando…' : 'Salvar preferências'}</button>
        </div>
      </section>
    </main>
  );
}

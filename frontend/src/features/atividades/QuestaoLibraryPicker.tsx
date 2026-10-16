import { useEffect, useState } from 'react';
import { importarQuestoes } from '../../services/atividadesApi';
import { listarBibliotecaQuestoes, type QuestaoModelo } from '../../services/bibliotecaApi';

type Props = {
  atividadeId: number;
  onImported: () => void | Promise<void>;
  onClose: () => void;
};

export function QuestaoLibraryPicker({ atividadeId, onImported, onClose }: Props) {
  const [items, setItems] = useState<QuestaoModelo[]>([]);
  const [selected, setSelected] = useState<number[]>([]);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    listarBibliotecaQuestoes({ page: 0, size: 20 })
      .then((result) => setItems(result.content))
      .catch(() => setMessage('Não foi possível carregar o banco de questões.'))
      .finally(() => setLoading(false));
  }, []);

  function toggle(id: number) {
    setSelected((current) => current.includes(id) ? current.filter((value) => value !== id) : [...current, id]);
  }

  async function importar() {
    if (!selected.length || busy) return;
    setBusy(true);
    setMessage('');
    try {
      await importarQuestoes(atividadeId, selected);
      await onImported();
      onClose();
    } catch {
      setMessage('Não foi possível importar as questões selecionadas.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="question-library-picker" aria-labelledby="picker-title">
      <div className="section-heading">
        <div><p className="card-kicker">Banco pedagógico</p><h3 id="picker-title">Adicionar questões da biblioteca</h3></div>
        <button className="secondary-button" type="button" onClick={onClose}>Fechar</button>
      </div>
      {message && <p className="form-message error" role="alert">{message}</p>}
      {loading ? <p>Carregando questões…</p> : items.length === 0 ? <p>Nenhuma questão reutilizável encontrada.</p> : (
        <div className="question-library-list">
          {items.map((item) => (
            <label className="question-library-option" key={item.id}>
              <input type="checkbox" checked={selected.includes(item.id)} onChange={() => toggle(item.id)} />
              <span><strong>{item.enunciado}</strong><small>{item.anoEscolar}º ano • {item.conteudo} • {item.tipo}</small></span>
            </label>
          ))}
        </div>
      )}
      <button className="primary-button" type="button" disabled={!selected.length || busy} onClick={() => void importar()}>
        {busy ? 'Importando…' : `Adicionar ${selected.length} ${selected.length === 1 ? 'questão' : 'questões'}`}
      </button>
    </section>
  );
}

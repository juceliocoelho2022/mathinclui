import { useEffect, useState } from 'react';
import {
  atualizarClassificacaoAtividade,
  buscarClassificacaoAtividade,
  listarHabilidadesBncc,
  listarTagsProfessor,
  type HabilidadeBncc,
  type TagProfessor,
} from '../../services/bibliotecaApi';

export type ClassificacaoSelection = { habilidadeIds: number[]; tagIds: number[] };

type Props = {
  atividadeId: number;
  anoEscolar: number;
  onSelectionChange?: (value: ClassificacaoSelection) => void;
};

export function ClassificacaoCurricularFields({ atividadeId, anoEscolar, onSelectionChange }: Props) {
  const [habilidades, setHabilidades] = useState<HabilidadeBncc[]>([]);
  const [tags, setTags] = useState<TagProfessor[]>([]);
  const [selected, setSelected] = useState<ClassificacaoSelection>({ habilidadeIds: [], tagIds: [] });
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    Promise.all([buscarClassificacaoAtividade(atividadeId), listarHabilidadesBncc(anoEscolar), listarTagsProfessor()])
      .then(([current, skills, personalTags]) => {
        setHabilidades(skills);
        setTags(personalTags);
        const next = { habilidadeIds: current.habilidades.map((item) => item.id), tagIds: current.tags.map((item) => item.id) };
        setSelected(next);
        onSelectionChange?.(next);
      })
      .catch(() => setMessage('Não foi possível carregar a classificação curricular.'));
  }, [atividadeId, anoEscolar, onSelectionChange]);

  function toggle(kind: 'habilidadeIds' | 'tagIds', id: number) {
    const values = selected[kind];
    const next = { ...selected, [kind]: values.includes(id) ? values.filter((value) => value !== id) : [...values, id] };
    setSelected(next);
    onSelectionChange?.(next);
  }

  async function salvar() {
    setBusy(true);
    setMessage('');
    try {
      await atualizarClassificacaoAtividade(atividadeId, selected);
      setMessage('Classificação salva.');
    } catch {
      setMessage('Não foi possível salvar a classificação.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="curriculum-fields" aria-labelledby="curriculo-title">
      <div className="section-heading">
        <div><p className="card-kicker">Currículo e organização</p><h2 id="curriculo-title">BNCC e tags</h2></div>
        <button className="secondary-button" type="button" disabled={busy} onClick={() => void salvar()}>{busy ? 'Salvando…' : 'Salvar classificação'}</button>
      </div>
      {message && <p className="form-message" role="status">{message}</p>}
      <fieldset className="curriculum-options">
        <legend>Habilidades BNCC do {anoEscolar}º ano</legend>
        {habilidades.length === 0 ? <p className="muted-copy">Nenhuma habilidade disponível para este ano.</p> : habilidades.map((item) => (
          <label className="check-row" key={item.id}>
            <input type="checkbox" checked={selected.habilidadeIds.includes(item.id)} onChange={() => toggle('habilidadeIds', item.id)} />
            {item.codigo} — {item.descricao}
          </label>
        ))}
      </fieldset>
      <fieldset className="curriculum-options">
        <legend>Tags pessoais</legend>
        {tags.length === 0 ? <p className="muted-copy">Nenhuma tag cadastrada.</p> : tags.map((item) => (
          <label className="check-row" key={item.id}>
            <input type="checkbox" checked={selected.tagIds.includes(item.id)} onChange={() => toggle('tagIds', item.id)} />
            {item.nome}
          </label>
        ))}
      </fieldset>
    </section>
  );
}

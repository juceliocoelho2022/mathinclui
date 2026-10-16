import type { DificuldadeAtividade } from '../../services/atividadesApi';

export type BibliotecaFilterValues = {
  ano: '' | '6' | '7' | '8';
  dificuldade: '' | DificuldadeAtividade;
  busca: string;
};

type Props = {
  value: BibliotecaFilterValues;
  onChange: (value: BibliotecaFilterValues) => void;
  onApply: () => void;
};

export function BibliotecaFilters({ value, onChange, onApply }: Props) {
  return (
    <section className="library-filters" aria-label="Filtros da biblioteca">
      <label>Ano escolar
        <select value={value.ano} onChange={(e) => onChange({ ...value, ano: e.target.value as BibliotecaFilterValues['ano'] })}>
          <option value="">Todos</option>
          <option value="6">6º ano</option>
          <option value="7">7º ano</option>
          <option value="8">8º ano</option>
        </select>
      </label>
      <label>Dificuldade
        <select value={value.dificuldade} onChange={(e) => onChange({ ...value, dificuldade: e.target.value as BibliotecaFilterValues['dificuldade'] })}>
          <option value="">Todas</option>
          <option value="BASICA">Básica</option>
          <option value="INTERMEDIARIA">Intermediária</option>
          <option value="AVANCADA">Avançada</option>
        </select>
      </label>
      <label>Buscar
        <input value={value.busca} onChange={(e) => onChange({ ...value, busca: e.target.value })} placeholder="Título, conteúdo ou habilidade" />
      </label>
      <button className="primary-button" type="button" onClick={onApply}>Aplicar filtros</button>
    </section>
  );
}

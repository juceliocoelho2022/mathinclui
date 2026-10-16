import { Link } from 'react-router-dom';
import type { BibliotecaAtividade } from '../../services/bibliotecaApi';

type Props = {
  atividade: BibliotecaAtividade;
  copying?: boolean;
  onUse: (id: number) => void | Promise<void>;
};

export function AtividadeBibliotecaCard({ atividade, copying = false, onUse }: Props) {
  return (
    <article className="card library-card">
      <div>
        <p className="card-kicker">{atividade.tipoOrigem === 'SISTEMA' ? 'Oficial MathInclui' : 'Minha biblioteca'}</p>
        <h2>{atividade.titulo}</h2>
        <p>{atividade.anoEscolar}º ano • {atividade.conteudo} • {atividade.dificuldade}</p>
        <p>{atividade.quantidadeQuestoes} questão(ões)</p>
        {atividade.habilidades.length > 0 && (
          <div className="library-badges" aria-label="Habilidades BNCC">
            {atividade.habilidades.map((h) => <span className="library-badge" key={h.id}>{h.codigo}</span>)}
          </div>
        )}
      </div>
      <div className="activity-actions">
        {atividade.tipoOrigem === 'SISTEMA' ? (
          <button className="primary-button" type="button" disabled={copying} onClick={() => void onUse(atividade.id)}>
            {copying ? 'Copiando…' : 'Usar atividade'}
          </button>
        ) : (
          <Link className="secondary-button link-button" to={`/app/atividades?editar=${atividade.id}`}>Editar</Link>
        )}
      </div>
    </article>
  );
}

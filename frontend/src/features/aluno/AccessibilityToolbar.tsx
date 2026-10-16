import type { ConfiguracaoAcessibilidade, PreferenciaInterface, TamanhoFonte } from '../../services/acessibilidadeApi';

type Props = {
  efetiva: ConfiguracaoAcessibilidade;
  preferencia: PreferenciaInterface;
  onChange: (value: PreferenciaInterface) => void | Promise<void>;
  disabled?: boolean;
};

const fontRank: Record<TamanhoFonte, number> = { PADRAO: 0, GRANDE: 1, EXTRA_GRANDE: 2 };

function maxFonte(a: TamanhoFonte, b: TamanhoFonte): TamanhoFonte {
  return fontRank[a] >= fontRank[b] ? a : b;
}

export function AccessibilityToolbar({ efetiva, preferencia, onChange, disabled = false }: Props) {
  const fonteVisivel = maxFonte(efetiva.tamanhoFonte, preferencia.tamanhoFonte);
  const leituraObrigatoria = efetiva.leituraVoz && !preferencia.leituraVoz;
  const visualObrigatorio = efetiva.apoioVisual && !preferencia.apoioVisual;

  function emitir(next: Partial<PreferenciaInterface>) {
    void onChange({ ...preferencia, ...next });
  }

  return (
    <section className="accessibility-toolbar" aria-label="Acessibilidade da atividade">
      <label>Tamanho do texto
        <select value={fonteVisivel} disabled={disabled} onChange={(e) => emitir({ tamanhoFonte: e.target.value as TamanhoFonte })}>
          <option value="PADRAO" disabled={fontRank.PADRAO < fontRank[efetiva.tamanhoFonte]}>Padrão</option>
          <option value="GRANDE" disabled={fontRank.GRANDE < fontRank[efetiva.tamanhoFonte]}>Grande</option>
          <option value="EXTRA_GRANDE">Extra grande</option>
        </select>
      </label>

      <label className="check-row">
        <input
          type="checkbox"
          checked={leituraObrigatoria || preferencia.leituraVoz}
          disabled={disabled || leituraObrigatoria}
          onChange={(e) => emitir({ leituraVoz: e.target.checked })}
        />
        Leitura em voz
      </label>

      <label className="check-row">
        <input
          type="checkbox"
          checked={visualObrigatorio || preferencia.apoioVisual}
          disabled={disabled || visualObrigatorio}
          onChange={(e) => emitir({ apoioVisual: e.target.checked })}
        />
        Apoio visual
      </label>

      {(leituraObrigatoria || visualObrigatorio) && (
        <small className="accessibility-note">Apoio definido para esta atividade.</small>
      )}
    </section>
  );
}

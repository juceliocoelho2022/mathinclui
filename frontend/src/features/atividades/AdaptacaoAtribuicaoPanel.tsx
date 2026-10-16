import { useEffect, useState } from 'react';
import { listarAlunos, type Aluno } from '../../services/gestaoEscolarApi';
import {
  adaptacaoVazia,
  type AdaptacaoPayload,
  type NivelApoio,
  type TamanhoFonte,
} from '../../services/acessibilidadeApi';

type Destino = 'TURMA' | 'ALUNO';
export type AdaptacaoIndividualDraft = { alunoId: number; adaptacao: AdaptacaoPayload };
export type AdaptacaoAtribuicaoDraft = { geral: AdaptacaoPayload; individuais: AdaptacaoIndividualDraft[] };

type Props = {
  destino: Destino;
  turmaId: number | null;
  onChange: (value: AdaptacaoAtribuicaoDraft) => void;
};

export function AdaptacaoAtribuicaoPanel({ destino, turmaId, onChange }: Props) {
  const [geral, setGeral] = useState<AdaptacaoPayload>(adaptacaoVazia);
  const [individuais, setIndividuais] = useState<AdaptacaoIndividualDraft[]>([]);
  const [showIndividuals, setShowIndividuals] = useState(false);
  const [alunos, setAlunos] = useState<Aluno[]>([]);
  const [selectedAlunoId, setSelectedAlunoId] = useState<number | null>(null);
  const [message, setMessage] = useState('');

  useEffect(() => {
    onChange({ geral, individuais: destino === 'TURMA' ? individuais : [] });
  }, []); // estado inicial herdado

  useEffect(() => {
    if (!showIndividuals || destino !== 'TURMA' || !turmaId) return;
    listarAlunos(turmaId)
      .then(setAlunos)
      .catch(() => setMessage('Não foi possível carregar os alunos para personalização.'));
  }, [showIndividuals, destino, turmaId]);

  function publish(nextGeral = geral, nextIndividuais = individuais) {
    onChange({ geral: nextGeral, individuais: destino === 'TURMA' ? nextIndividuais : [] });
  }

  function changeGeral<K extends keyof AdaptacaoPayload>(key: K, value: AdaptacaoPayload[K]) {
    const next = { ...geral, [key]: value };
    setGeral(next);
    publish(next, individuais);
  }

  function currentIndividual() {
    if (!selectedAlunoId) return adaptacaoVazia();
    return individuais.find((item) => item.alunoId === selectedAlunoId)?.adaptacao ?? adaptacaoVazia();
  }

  function changeIndividual<K extends keyof AdaptacaoPayload>(key: K, value: AdaptacaoPayload[K]) {
    if (!selectedAlunoId) return;
    const current = currentIndividual();
    const nextAdaptacao = { ...current, [key]: value };
    const next = individuais.some((item) => item.alunoId === selectedAlunoId)
      ? individuais.map((item) => item.alunoId === selectedAlunoId ? { ...item, adaptacao: nextAdaptacao } : item)
      : [...individuais, { alunoId: selectedAlunoId, adaptacao: nextAdaptacao }];
    setIndividuais(next);
    publish(geral, next);
  }

  const individual = currentIndividual();

  return (
    <section className="assignment-adaptation" aria-labelledby="adaptacao-title">
      <div>
        <p className="card-kicker">Acessibilidade • v0.5</p>
        <h2 id="adaptacao-title">Apoios desta atividade</h2>
        <p className="muted-copy">Campos em “Herdar” mantêm o perfil pedagógico do aluno. Configurações adicionais nunca reduzem o apoio existente.</p>
      </div>

      <div className="form-row">
        <label>Fonte mínima
          <select value={geral.tamanhoFonteMinima ?? ''} onChange={(e) => changeGeral('tamanhoFonteMinima', (e.target.value || null) as TamanhoFonte | null)}>
            <option value="">Herdar</option>
            <option value="PADRAO">Padrão</option>
            <option value="GRANDE">Grande</option>
            <option value="EXTRA_GRANDE">Extra grande</option>
          </select>
        </label>
        <label>Nível de apoio
          <select value={geral.nivelApoio ?? ''} onChange={(e) => changeGeral('nivelApoio', (e.target.value || null) as NivelApoio | null)}>
            <option value="">Herdar</option>
            <option value="PADRAO">Padrão</option>
            <option value="MODERADO">Moderado</option>
            <option value="AMPLIADO">Ampliado</option>
          </select>
        </label>
      </div>

      <div className="adaptation-checks">
        <label className="check-row"><input type="checkbox" checked={geral.leituraVozMinima === true} onChange={(e) => changeGeral('leituraVozMinima', e.target.checked ? true : null)} />Leitura em voz mínima</label>
        <label className="check-row"><input type="checkbox" checked={geral.apoioVisualMinimo === true} onChange={(e) => changeGeral('apoioVisualMinimo', e.target.checked ? true : null)} />Apoio visual mínimo</label>
        <label className="check-row"><input type="checkbox" checked={geral.dicasPassoAPasso === true} onChange={(e) => changeGeral('dicasPassoAPasso', e.target.checked ? true : null)} />Dicas passo a passo</label>
        <label className="check-row"><input type="checkbox" checked={geral.repeticaoGuiada === true} onChange={(e) => changeGeral('repeticaoGuiada', e.target.checked ? true : null)} />Repetição guiada</label>
        <label className="check-row"><input type="checkbox" checked={geral.tempoAmpliado === true} onChange={(e) => changeGeral('tempoAmpliado', e.target.checked ? true : null)} />Tempo ampliado</label>
      </div>
      <label>Questões por tela
        <input type="number" min={1} max={10} value={geral.questoesPorTela ?? ''} placeholder="Herdar" onChange={(e) => changeGeral('questoesPorTela', e.target.value ? Number(e.target.value) : null)} />
      </label>

      {destino === 'TURMA' && (
        <div className="individual-adaptation">
          <button className="secondary-button" type="button" onClick={() => setShowIndividuals((value) => !value)}>Personalizações individuais</button>
          {showIndividuals && (
            <div className="individual-fields">
              {message && <p className="form-message error" role="alert">{message}</p>}
              <label>Aluno para personalizar
                <select value={selectedAlunoId ?? ''} onChange={(e) => setSelectedAlunoId(e.target.value ? Number(e.target.value) : null)}>
                  <option value="">Selecione</option>
                  {alunos.map((aluno) => <option key={aluno.id} value={aluno.id}>{aluno.nome}</option>)}
                </select>
              </label>
              {selectedAlunoId && (
                <div className="form-row">
                  <label>Fonte mínima individual
                    <select value={individual.tamanhoFonteMinima ?? ''} onChange={(e) => changeIndividual('tamanhoFonteMinima', (e.target.value || null) as TamanhoFonte | null)}>
                      <option value="">Herdar</option><option value="PADRAO">Padrão</option><option value="GRANDE">Grande</option><option value="EXTRA_GRANDE">Extra grande</option>
                    </select>
                  </label>
                  <label>Nível individual
                    <select value={individual.nivelApoio ?? ''} onChange={(e) => changeIndividual('nivelApoio', (e.target.value || null) as NivelApoio | null)}>
                      <option value="">Herdar</option><option value="PADRAO">Padrão</option><option value="MODERADO">Moderado</option><option value="AMPLIADO">Ampliado</option>
                    </select>
                  </label>
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </section>
  );
}

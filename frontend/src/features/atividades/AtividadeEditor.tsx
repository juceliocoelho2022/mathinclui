import { FormEvent, useCallback, useEffect, useMemo, useState } from 'react';
import {
  atualizarAtividade,
  atualizarQuestao,
  criarAtividade,
  criarQuestao,
  excluirQuestao,
  listarQuestoes,
  publicarAtividade,
  salvarQuestaoNoBanco,
  type AlternativaProfessor,
  type Atividade,
  type DificuldadeAtividade,
  type QuestaoPayload,
  type QuestaoProfessor,
  type TipoQuestao,
} from '../../services/atividadesApi';
import { ClassificacaoCurricularFields, type ClassificacaoSelection } from './ClassificacaoCurricularFields';
import { QuestaoLibraryPicker } from './QuestaoLibraryPicker';

type Props = {
  atividade: Atividade | null;
  onSaved: (atividade: Atividade) => void;
  onCancel: () => void;
};

type AlternativaDraft = AlternativaProfessor & { localId: number };

const novasAlternativas = (): AlternativaDraft[] => [
  { localId: 1, texto: '', correta: true, ordem: 1 },
  { localId: 2, texto: '', correta: false, ordem: 2 },
];

export function AtividadeEditor({ atividade, onSaved, onCancel }: Props) {
  const [current, setCurrent] = useState<Atividade | null>(atividade);
  const [titulo, setTitulo] = useState(atividade?.titulo ?? '');
  const [descricao, setDescricao] = useState(atividade?.descricao ?? '');
  const [anoEscolar, setAnoEscolar] = useState(atividade?.anoEscolar ?? 6);
  const [conteudo, setConteudo] = useState(atividade?.conteudo ?? '');
  const [dificuldade, setDificuldade] = useState<DificuldadeAtividade>(atividade?.dificuldade ?? 'BASICA');
  const [limiteTentativas, setLimiteTentativas] = useState(atividade?.limiteTentativas ?? 3);
  const [questoes, setQuestoes] = useState<QuestaoProfessor[]>([]);
  const [editingQuestao, setEditingQuestao] = useState<QuestaoProfessor | null>(null);
  const [showQuestao, setShowQuestao] = useState(false);
  const [showLibrary, setShowLibrary] = useState(false);
  const [classificacao, setClassificacao] = useState<ClassificacaoSelection>({ habilidadeIds: [], tagIds: [] });
  const [classificacaoReady, setClassificacaoReady] = useState(false);
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);

  const [enunciado, setEnunciado] = useState('');
  const [tipo, setTipo] = useState<TipoQuestao>('NUMERICA');
  const [respostaCorreta, setRespostaCorreta] = useState('');
  const [explicacao, setExplicacao] = useState('');
  const [dica1, setDica1] = useState('');
  const [dica2, setDica2] = useState('');
  const [dica3, setDica3] = useState('');
  const [limiteQuestao, setLimiteQuestao] = useState('');
  const [alternativas, setAlternativas] = useState<AlternativaDraft[]>(novasAlternativas);

  const carregarQuestoes = useCallback(async () => {
    if (!current) return;
    try {
      setQuestoes(await listarQuestoes(current.id));
    } catch {
      setMessage('Não foi possível carregar as questões.');
    }
  }, [current?.id]);

  useEffect(() => { void carregarQuestoes(); }, [carregarQuestoes]);

  const onClassificationChange = useCallback((value: ClassificacaoSelection) => {
    setClassificacao(value);
    setClassificacaoReady(true);
  }, []);

  const proximaOrdem = useMemo(() => Math.max(0, ...questoes.map((q) => q.ordem)) + 1, [questoes]);

  async function salvarAtividade(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setMessage('');
    try {
      const payload = { titulo, descricao: descricao || null, anoEscolar, conteudo, dificuldade, limiteTentativas };
      const saved = current ? await atualizarAtividade(current.id, payload) : await criarAtividade(payload);
      setCurrent(saved);
      onSaved(saved);
      setMessage('Dados gerais salvos. Agora você pode configurar currículo e questões.');
    } catch {
      setMessage('Não foi possível salvar a atividade. Revise os campos.');
    } finally {
      setBusy(false);
    }
  }

  function abrirNovaQuestao() {
    setEditingQuestao(null);
    setEnunciado('');
    setTipo('NUMERICA');
    setRespostaCorreta('');
    setExplicacao('');
    setDica1('');
    setDica2('');
    setDica3('');
    setLimiteQuestao('');
    setAlternativas(novasAlternativas());
    setShowQuestao(true);
    setShowLibrary(false);
  }

  function editarQuestaoItem(q: QuestaoProfessor) {
    setEditingQuestao(q);
    setEnunciado(q.enunciado);
    setTipo(q.tipo);
    setRespostaCorreta(q.respostaCorreta ?? '');
    setExplicacao(q.explicacao ?? '');
    setDica1(q.dica1 ?? '');
    setDica2(q.dica2 ?? '');
    setDica3(q.dica3 ?? '');
    setLimiteQuestao(q.limiteTentativas ? String(q.limiteTentativas) : '');
    setAlternativas(q.alternativas.length ? q.alternativas.map((a, index) => ({ ...a, localId: index + 1 })) : novasAlternativas());
    setShowQuestao(true);
    setShowLibrary(false);
  }

  function mudarTipo(next: TipoQuestao) {
    setTipo(next);
    if (next === 'MULTIPLA_ESCOLHA' && alternativas.length < 2) setAlternativas(novasAlternativas());
  }

  async function salvarQuestao(event: FormEvent) {
    event.preventDefault();
    if (!current) return;
    const alternativasPayload = tipo === 'MULTIPLA_ESCOLHA'
      ? alternativas.map(({ texto, correta, ordem }) => ({ texto, correta, ordem }))
      : [];
    if (tipo === 'MULTIPLA_ESCOLHA' && (alternativasPayload.length < 2 || alternativasPayload.filter((a) => a.correta).length !== 1)) {
      setMessage('Múltipla escolha precisa de pelo menos duas alternativas e exatamente uma correta.');
      return;
    }
    const payload: QuestaoPayload = {
      enunciado,
      tipo,
      respostaCorreta: tipo === 'MULTIPLA_ESCOLHA' ? null : respostaCorreta,
      explicacao: explicacao || null,
      dica1: dica1 || null,
      dica2: dica2 || null,
      dica3: dica3 || null,
      imagemUrl: null,
      ordem: editingQuestao?.ordem ?? proximaOrdem,
      limiteTentativas: limiteQuestao ? Number(limiteQuestao) : null,
      alternativas: alternativasPayload,
    };
    try {
      if (editingQuestao) await atualizarQuestao(current.id, editingQuestao.id, payload);
      else await criarQuestao(current.id, payload);
      await carregarQuestoes();
      setShowQuestao(false);
      setMessage('Questão salva.');
    } catch {
      setMessage('Não foi possível salvar a questão.');
    }
  }

  async function removerQuestao(id: number) {
    if (!current) return;
    await excluirQuestao(current.id, id);
    await carregarQuestoes();
  }

  async function salvarQuestaoBanco(questaoId: number) {
    if (!current || !classificacaoReady) return;
    try {
      await salvarQuestaoNoBanco(current.id, questaoId, {
        anoEscolar: current.anoEscolar,
        conteudo: current.conteudo,
        dificuldade: current.dificuldade,
        habilidadeIds: classificacao.habilidadeIds,
        tagIds: classificacao.tagIds,
      });
      setMessage('Questão salva na sua biblioteca.');
    } catch {
      setMessage('Não foi possível salvar a questão na biblioteca.');
    }
  }

  async function publicar() {
    if (!current) return;
    try {
      const publicada = await publicarAtividade(current.id);
      setCurrent(publicada);
      onSaved(publicada);
      setMessage('Atividade publicada e pronta para atribuição.');
    } catch {
      setMessage('A atividade ainda não atende aos critérios de publicação.');
    }
  }

  function atualizarAlternativa(index: number, texto: string) {
    setAlternativas((items) => items.map((item, i) => i === index ? { ...item, texto } : item));
  }

  function marcarCorreta(index: number) {
    setAlternativas((items) => items.map((item, i) => ({ ...item, correta: i === index })));
  }

  function adicionarAlternativa() {
    setAlternativas((items) => [...items, { localId: Date.now(), texto: '', correta: false, ordem: items.length + 1 }]);
  }

  return (
    <section className="session-card activity-editor">
      <div className="session-header">
        <div>
          <p className="card-kicker">Editor de atividade</p>
          <h1>{current ? current.titulo : 'Nova atividade'}</h1>
          <p>Configure os dados gerais, o currículo, as questões, dicas e limites de tentativas.</p>
        </div>
        <button className="secondary-button" type="button" onClick={onCancel}>Voltar</button>
      </div>

      {message && <p className="form-message" role="status">{message}</p>}

      <form className="auth-form activity-form" onSubmit={salvarAtividade}>
        <label>Título<input value={titulo} onChange={(e) => setTitulo(e.target.value)} required maxLength={160} /></label>
        <label>Descrição<textarea value={descricao ?? ''} onChange={(e) => setDescricao(e.target.value)} rows={3} /></label>
        <div className="form-row">
          <label>Ano escolar
            <select value={anoEscolar} onChange={(e) => setAnoEscolar(Number(e.target.value))}>
              <option value="6">6º ano</option><option value="7">7º ano</option><option value="8">8º ano</option>
            </select>
          </label>
          <label>Dificuldade
            <select value={dificuldade} onChange={(e) => setDificuldade(e.target.value as DificuldadeAtividade)}>
              <option value="BASICA">Básica</option><option value="INTERMEDIARIA">Intermediária</option><option value="AVANCADA">Avançada</option>
            </select>
          </label>
        </div>
        <div className="form-row">
          <label>Conteúdo<input value={conteudo} onChange={(e) => setConteudo(e.target.value)} required maxLength={60} /></label>
          <label>Limite padrão de tentativas<input type="number" min={1} value={limiteTentativas} onChange={(e) => setLimiteTentativas(Number(e.target.value))} required /></label>
        </div>
        <button className="primary-button" disabled={busy}>{busy ? 'Salvando…' : 'Salvar atividade'}</button>
      </form>

      {current && <ClassificacaoCurricularFields atividadeId={current.id} anoEscolar={current.anoEscolar} onSelectionChange={onClassificationChange} />}

      {current && (
        <section className="question-section" aria-labelledby="questoes-title">
          <div className="section-heading">
            <div><p className="card-kicker">Conteúdo avaliativo</p><h2 id="questoes-title">Questões da atividade</h2></div>
            <div className="activity-actions">
              <button className="secondary-button" type="button" onClick={abrirNovaQuestao}>Adicionar questão</button>
              <button className="secondary-button" type="button" onClick={() => { setShowLibrary(true); setShowQuestao(false); }}>Buscar na biblioteca</button>
            </div>
          </div>

          {showLibrary && <QuestaoLibraryPicker atividadeId={current.id} onImported={carregarQuestoes} onClose={() => setShowLibrary(false)} />}

          {questoes.map((q) => (
            <article className="question-summary" key={q.id}>
              <div><strong>{q.ordem}. {q.enunciado}</strong><small>{q.tipo} • limite {q.limiteTentativas ?? current.limiteTentativas}</small></div>
              <div className="activity-actions">
                <button type="button" className="secondary-button" onClick={() => editarQuestaoItem(q)}>Editar questão</button>
                {classificacaoReady && <button type="button" className="secondary-button" onClick={() => void salvarQuestaoBanco(q.id)}>Salvar no banco</button>}
                <button type="button" className="secondary-button" onClick={() => void removerQuestao(q.id)}>Excluir</button>
              </div>
            </article>
          ))}

          {showQuestao && (
            <form className="auth-form question-editor" onSubmit={salvarQuestao}>
              <h3>{editingQuestao ? 'Editar questão' : 'Nova questão'}</h3>
              <label>Enunciado<textarea value={enunciado} onChange={(e) => setEnunciado(e.target.value)} required rows={3} /></label>
              <label>Tipo de questão
                <select value={tipo} onChange={(e) => mudarTipo(e.target.value as TipoQuestao)}>
                  <option value="NUMERICA">Numérica</option>
                  <option value="TEXTO_CURTO">Texto curto</option>
                  <option value="MULTIPLA_ESCOLHA">Múltipla escolha</option>
                </select>
              </label>
              {tipo === 'MULTIPLA_ESCOLHA' ? (
                <fieldset className="alternatives-fieldset">
                  <legend>Marque a alternativa correta</legend>
                  {alternativas.map((alt, index) => (
                    <div className="alternative-row" key={alt.localId}>
                      <label>Alternativa {index + 1}<input value={alt.texto} onChange={(e) => atualizarAlternativa(index, e.target.value)} required /></label>
                      <label className="check-row"><input type="radio" name="correta" checked={alt.correta} onChange={() => marcarCorreta(index)} />Correta</label>
                    </div>
                  ))}
                  <button className="secondary-button" type="button" onClick={adicionarAlternativa}>Adicionar alternativa</button>
                </fieldset>
              ) : (
                <label>Resposta correta<input value={respostaCorreta} onChange={(e) => setRespostaCorreta(e.target.value)} required /></label>
              )}
              <label>Explicação<textarea value={explicacao} onChange={(e) => setExplicacao(e.target.value)} rows={2} /></label>
              <div className="form-row three-columns">
                <label>Dica 1<input value={dica1} onChange={(e) => setDica1(e.target.value)} /></label>
                <label>Dica 2<input value={dica2} onChange={(e) => setDica2(e.target.value)} /></label>
                <label>Dica 3<input value={dica3} onChange={(e) => setDica3(e.target.value)} /></label>
              </div>
              <label>Limite de tentativas desta questão (opcional)<input type="number" min={1} value={limiteQuestao} onChange={(e) => setLimiteQuestao(e.target.value)} /></label>
              <div className="activity-actions">
                <button className="primary-button">Salvar questão</button>
                <button className="secondary-button" type="button" onClick={() => setShowQuestao(false)}>Cancelar questão</button>
              </div>
            </form>
          )}

          <div className="publish-bar">
            <span>{current.publicada ? 'Esta atividade está publicada.' : 'Revise as questões antes de publicar.'}</span>
            {!current.publicada && <button className="primary-button" type="button" onClick={() => void publicar()}>Publicar atividade</button>}
          </div>
        </section>
      )}
    </section>
  );
}

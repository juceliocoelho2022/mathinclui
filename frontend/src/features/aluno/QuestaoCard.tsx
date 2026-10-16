import { FormEvent } from 'react';
import type { ConfiguracaoAcessibilidade } from '../../services/acessibilidadeApi';
import type { AlunoQuestao, RespostaQuestaoResultado } from '../../services/alunoAtividadesApi';

type Props = {
  questao: AlunoQuestao;
  acessibilidade: ConfiguracaoAcessibilidade;
  resposta: string;
  onResposta: (value: string) => void;
  onResponder: () => void | Promise<void>;
  feedback?: RespostaQuestaoResultado | null;
  busy?: boolean;
};

export function QuestaoCard({ questao, acessibilidade, resposta, onResposta, onResponder, feedback, busy = false }: Props) {
  const fontClass = acessibilidade.tamanhoFonte === 'EXTRA_GRANDE'
    ? 'font-extra-grande'
    : acessibilidade.tamanhoFonte === 'GRANDE' ? 'font-grande' : 'font-padrao';

  function submit(event: FormEvent) {
    event.preventDefault();
    void onResponder();
  }

  function lerEnunciado() {
    if (typeof window === 'undefined' || !window.speechSynthesis) return;
    window.speechSynthesis.cancel();
    window.speechSynthesis.speak(new SpeechSynthesisUtterance(questao.enunciado));
  }

  const dica = feedback?.dica ?? questao.dicaAtual;
  const retryGuiado = Boolean(feedback && !feedback.correta && !feedback.encerrada && acessibilidade.repeticaoGuiada);

  return (
    <article data-testid="questao-card" className={`question-player ${fontClass} ${acessibilidade.apoioVisual ? 'with-visual-support' : ''}`}>
      <div className="question-meta">
        <span>Questão {questao.ordem}</span>
        <span>{questao.tentativasUsadas}/{questao.limiteTentativas} tentativa(s) usadas</span>
      </div>
      <h2>{questao.enunciado}</h2>
      {questao.imagemUrl && acessibilidade.apoioVisual && <img className="question-image" src={questao.imagemUrl} alt="Apoio visual da questão" />}
      {acessibilidade.leituraVoz && <button className="secondary-button" type="button" onClick={lerEnunciado}>Ouvir enunciado</button>}
      {acessibilidade.dicasPassoAPasso && dica && !retryGuiado && <aside className="hint-box"><strong>Dica</strong><p>{dica}</p></aside>}

      <form className="answer-form" onSubmit={submit}>
        {questao.tipo === 'MULTIPLA_ESCOLHA' ? (
          <fieldset>
            <legend>Escolha uma alternativa</legend>
            {questao.alternativas.map((alternativa) => (
              <label className="answer-option" key={alternativa.id}>
                <input type="radio" name="resposta" value={alternativa.id} checked={resposta === String(alternativa.id)} onChange={(e) => onResposta(e.target.value)} required />
                {alternativa.texto}
              </label>
            ))}
          </fieldset>
        ) : (
          <label>Sua resposta
            <input
              type="text"
              inputMode={questao.tipo === 'NUMERICA' ? 'decimal' : 'text'}
              value={resposta}
              onChange={(e) => onResposta(e.target.value)}
              required
              disabled={feedback?.encerrada}
            />
          </label>
        )}
        {!feedback?.encerrada && <button className="primary-button" disabled={busy || !resposta.trim()}>{busy ? 'Verificando…' : 'Responder'}</button>}
      </form>

      {feedback && (
        <section className={`feedback-box ${feedback.correta ? 'success' : feedback.encerrada ? 'final' : 'retry'}`} aria-live="polite">
          <strong>{feedback.correta ? 'Muito bem! Resposta correta.' : feedback.encerrada ? 'Vamos revisar esta questão.' : retryGuiado ? 'Vamos por partes.' : 'Ainda não. Tente novamente.'}</strong>
          {retryGuiado && dica && <p>{dica}</p>}
          {feedback.encerrada && feedback.explicacao && <p>{feedback.explicacao}</p>}
          {feedback.encerrada && feedback.respostaCorreta && <p><strong>Resposta correta: {feedback.respostaCorreta}</strong></p>}
        </section>
      )}
    </article>
  );
}

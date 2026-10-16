package br.com.mathinclui.acessibilidade;

import br.com.mathinclui.acessibilidade.domain.AdaptacaoAlunoAtribuicao;
import br.com.mathinclui.acessibilidade.domain.AdaptacaoAtribuicao;
import br.com.mathinclui.acessibilidade.domain.ConfiguracaoAcessibilidadeEfetiva;
import br.com.mathinclui.acessibilidade.domain.PreferenciaInterfaceAluno;
import br.com.mathinclui.acessibilidade.repository.AdaptacaoAlunoAtribuicaoRepository;
import br.com.mathinclui.acessibilidade.repository.AdaptacaoAtribuicaoRepository;
import br.com.mathinclui.acessibilidade.repository.PreferenciaInterfaceAlunoRepository;
import br.com.mathinclui.aluno.domain.Aluno;
import br.com.mathinclui.aluno.repository.AlunoRepository;
import br.com.mathinclui.atribuicao.repository.AtribuicaoRepository;
import br.com.mathinclui.perfilpedagogico.domain.NivelApoio;
import br.com.mathinclui.perfilpedagogico.domain.PerfilPedagogico;
import br.com.mathinclui.perfilpedagogico.domain.TamanhoFonte;
import br.com.mathinclui.perfilpedagogico.repository.PerfilPedagogicoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AcessibilidadeEfetivaService {
    private final AlunoRepository alunoRepository;
    private final AtribuicaoRepository atribuicaoRepository;
    private final PerfilPedagogicoRepository perfilRepository;
    private final AdaptacaoAtribuicaoRepository adaptacaoRepository;
    private final AdaptacaoAlunoAtribuicaoRepository adaptacaoAlunoRepository;
    private final PreferenciaInterfaceAlunoRepository preferenciaRepository;

    public AcessibilidadeEfetivaService(AlunoRepository alunoRepository,
                                        AtribuicaoRepository atribuicaoRepository,
                                        PerfilPedagogicoRepository perfilRepository,
                                        AdaptacaoAtribuicaoRepository adaptacaoRepository,
                                        AdaptacaoAlunoAtribuicaoRepository adaptacaoAlunoRepository,
                                        PreferenciaInterfaceAlunoRepository preferenciaRepository) {
        this.alunoRepository = alunoRepository;
        this.atribuicaoRepository = atribuicaoRepository;
        this.perfilRepository = perfilRepository;
        this.adaptacaoRepository = adaptacaoRepository;
        this.adaptacaoAlunoRepository = adaptacaoAlunoRepository;
        this.preferenciaRepository = preferenciaRepository;
    }

    @Transactional(readOnly = true)
    public ConfiguracaoAcessibilidadeEfetiva calcular(Long alunoId, Long atribuicaoId) {
        var aluno = alunoRepository.findById(alunoId).filter(Aluno::isAtivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado."));
        var atribuicao = atribuicaoRepository.findById(atribuicaoId)
                .filter(a -> a.destinadaAo(alunoId, aluno.getTurmaId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atividade atribuída não encontrada."));

        var perfil = perfilRepository.findByAlunoId(alunoId).orElseGet(() -> new PerfilPedagogico(alunoId));
        var geral = adaptacaoRepository.findByAtribuicaoId(atribuicaoId).orElse(null);
        var individual = atribuicao.getTurmaId() == null ? null
                : adaptacaoAlunoRepository.findByAtribuicaoIdAndAlunoId(atribuicaoId, alunoId).orElse(null);
        var preferencia = preferenciaRepository.findByAlunoId(alunoId).orElse(null);

        TamanhoFonte fonte = perfil.getTamanhoFonte();
        fonte = maiorFonte(fonte, geral == null ? null : geral.getTamanhoFonteMinima());
        fonte = maiorFonte(fonte, individual == null ? null : individual.getTamanhoFonteMinima());
        fonte = maiorFonte(fonte, preferencia == null ? null : preferencia.getTamanhoFonte());

        boolean leituraVoz = perfil.isLeituraVoz()
                || ativo(geral == null ? null : geral.getLeituraVozMinima())
                || ativo(individual == null ? null : individual.getLeituraVozMinima())
                || (preferencia != null && preferencia.isLeituraVoz());
        boolean apoioVisual = perfil.isApoioVisual()
                || ativo(geral == null ? null : geral.getApoioVisualMinimo())
                || ativo(individual == null ? null : individual.getApoioVisualMinimo())
                || (preferencia != null && preferencia.isApoioVisual());
        boolean dicas = perfil.isDicasPassoAPasso()
                || ativo(geral == null ? null : geral.getDicasPassoAPasso())
                || ativo(individual == null ? null : individual.getDicasPassoAPasso());
        boolean repeticao = perfil.isRepeticaoGuiada()
                || ativo(geral == null ? null : geral.getRepeticaoGuiada())
                || ativo(individual == null ? null : individual.getRepeticaoGuiada());
        boolean tempo = perfil.isTempoAmpliado()
                || ativo(geral == null ? null : geral.getTempoAmpliado())
                || ativo(individual == null ? null : individual.getTempoAmpliado());

        short questoes = perfil.getQuestoesPorTela();
        questoes = menorCarga(questoes, geral == null ? null : geral.getQuestoesPorTela());
        questoes = menorCarga(questoes, individual == null ? null : individual.getQuestoesPorTela());

        NivelApoio nivel = perfil.getNivelApoio();
        nivel = maiorNivel(nivel, geral == null ? null : geral.getNivelApoio());
        nivel = maiorNivel(nivel, individual == null ? null : individual.getNivelApoio());

        return new ConfiguracaoAcessibilidadeEfetiva(fonte, leituraVoz, apoioVisual, dicas, repeticao,
                questoes, tempo, nivel);
    }

    private boolean ativo(Boolean valor) {
        return Boolean.TRUE.equals(valor);
    }

    private short menorCarga(short atual, Short candidato) {
        return candidato == null ? atual : (short) Math.min(atual, candidato);
    }

    private TamanhoFonte maiorFonte(TamanhoFonte atual, TamanhoFonte candidato) {
        if (candidato == null) return atual;
        return rankFonte(candidato) > rankFonte(atual) ? candidato : atual;
    }

    private int rankFonte(TamanhoFonte fonte) {
        return switch (fonte) {
            case PADRAO -> 0;
            case GRANDE -> 1;
            case EXTRA_GRANDE -> 2;
        };
    }

    private NivelApoio maiorNivel(NivelApoio atual, NivelApoio candidato) {
        if (candidato == null) return atual;
        return rankNivel(candidato) > rankNivel(atual) ? candidato : atual;
    }

    private int rankNivel(NivelApoio nivel) {
        return switch (nivel) {
            case PADRAO -> 0;
            case MODERADO -> 1;
            case AMPLIADO -> 2;
        };
    }
}

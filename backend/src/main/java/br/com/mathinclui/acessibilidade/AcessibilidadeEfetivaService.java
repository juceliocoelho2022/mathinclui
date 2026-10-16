package br.com.mathinclui.acessibilidade;

import br.com.mathinclui.acessibilidade.domain.*;
import br.com.mathinclui.acessibilidade.repository.*;
import br.com.mathinclui.aluno.domain.Aluno;
import br.com.mathinclui.aluno.repository.AlunoRepository;
import br.com.mathinclui.atribuicao.domain.Atribuicao;
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
    private final PreferenciaInterfaceAlunoRepository preferenciaRepository;
    private final AdaptacaoAtribuicaoRepository adaptacaoRepository;
    private final AdaptacaoAlunoAtribuicaoRepository adaptacaoAlunoRepository;

    public AcessibilidadeEfetivaService(AlunoRepository alunoRepository, AtribuicaoRepository atribuicaoRepository,
                                        PerfilPedagogicoRepository perfilRepository,
                                        PreferenciaInterfaceAlunoRepository preferenciaRepository,
                                        AdaptacaoAtribuicaoRepository adaptacaoRepository,
                                        AdaptacaoAlunoAtribuicaoRepository adaptacaoAlunoRepository) {
        this.alunoRepository = alunoRepository;
        this.atribuicaoRepository = atribuicaoRepository;
        this.perfilRepository = perfilRepository;
        this.preferenciaRepository = preferenciaRepository;
        this.adaptacaoRepository = adaptacaoRepository;
        this.adaptacaoAlunoRepository = adaptacaoAlunoRepository;
    }

    @Transactional(readOnly = true)
    public ConfiguracaoAcessibilidadeEfetiva calcular(Long alunoId, Long atribuicaoId) {
        Aluno aluno = alunoRepository.findById(alunoId).filter(Aluno::isAtivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado."));
        Atribuicao atribuicao = atribuicaoRepository.findById(atribuicaoId)
                .filter(a -> a.destinadaAo(alunoId, aluno.getTurmaId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atividade atribuída não encontrada."));

        PerfilPedagogico perfil = perfilRepository.findByAlunoId(alunoId).orElseGet(() -> new PerfilPedagogico(alunoId));
        TamanhoFonte fonte = perfil.getTamanhoFonte();
        boolean leitura = perfil.isLeituraVoz();
        boolean visual = perfil.isApoioVisual();
        boolean dicas = perfil.isDicasPassoAPasso();
        boolean repeticao = perfil.isRepeticaoGuiada();
        short questoes = perfil.getQuestoesPorTela();
        boolean tempo = perfil.isTempoAmpliado();
        NivelApoio nivel = perfil.getNivelApoio();

        var geral = adaptacaoRepository.findById(atribuicaoId).orElse(null);
        if (geral != null) {
            fonte = maiorFonte(fonte, geral.getTamanhoFonteMinima());
            leitura |= Boolean.TRUE.equals(geral.getLeituraVozMinima());
            visual |= Boolean.TRUE.equals(geral.getApoioVisualMinimo());
            dicas |= Boolean.TRUE.equals(geral.getDicasPassoAPasso());
            repeticao |= Boolean.TRUE.equals(geral.getRepeticaoGuiada());
            if (geral.getQuestoesPorTela() != null) questoes = (short) Math.min(questoes, geral.getQuestoesPorTela());
            tempo |= Boolean.TRUE.equals(geral.getTempoAmpliado());
            nivel = maiorNivel(nivel, geral.getNivelApoio());
        }

        var individual = atribuicao.getTurmaId() == null ? null :
                adaptacaoAlunoRepository.findByAtribuicaoIdAndAlunoId(atribuicaoId, alunoId).orElse(null);
        if (individual != null) {
            fonte = maiorFonte(fonte, individual.getTamanhoFonteMinima());
            leitura |= Boolean.TRUE.equals(individual.getLeituraVozMinima());
            visual |= Boolean.TRUE.equals(individual.getApoioVisualMinimo());
            dicas |= Boolean.TRUE.equals(individual.getDicasPassoAPasso());
            repeticao |= Boolean.TRUE.equals(individual.getRepeticaoGuiada());
            if (individual.getQuestoesPorTela() != null) questoes = (short) Math.min(questoes, individual.getQuestoesPorTela());
            tempo |= Boolean.TRUE.equals(individual.getTempoAmpliado());
            nivel = maiorNivel(nivel, individual.getNivelApoio());
        }

        var preferencia = preferenciaRepository.findById(alunoId).orElse(null);
        if (preferencia != null) {
            fonte = maiorFonte(fonte, preferencia.getTamanhoFonte());
            leitura |= preferencia.isLeituraVoz();
            visual |= preferencia.isApoioVisual();
        }

        return new ConfiguracaoAcessibilidadeEfetiva(fonte, leitura, visual, dicas, repeticao, questoes, tempo, nivel);
    }

    private TamanhoFonte maiorFonte(TamanhoFonte atual, TamanhoFonte candidato) {
        if (candidato == null) return atual;
        return rank(candidato) > rank(atual) ? candidato : atual;
    }

    private int rank(TamanhoFonte fonte) {
        return switch (fonte) {
            case PADRAO -> 0;
            case GRANDE -> 1;
            case EXTRA_GRANDE -> 2;
        };
    }

    private NivelApoio maiorNivel(NivelApoio atual, NivelApoio candidato) {
        if (candidato == null) return atual;
        return rank(candidato) > rank(atual) ? candidato : atual;
    }

    private int rank(NivelApoio nivel) {
        return switch (nivel) {
            case PADRAO -> 0;
            case MODERADO -> 1;
            case AMPLIADO -> 2;
        };
    }
}

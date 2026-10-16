package br.com.mathinclui.tentativa;

import br.com.mathinclui.acessibilidade.AcessibilidadeEfetivaService;
import br.com.mathinclui.acessibilidade.domain.ConfiguracaoAcessibilidadeEfetiva;
import br.com.mathinclui.aluno.domain.Aluno;
import br.com.mathinclui.aluno.repository.AlunoRepository;
import br.com.mathinclui.atividade.domain.Atividade;
import br.com.mathinclui.atividade.repository.AtividadeRepository;
import br.com.mathinclui.atribuicao.domain.Atribuicao;
import br.com.mathinclui.atribuicao.repository.AtribuicaoRepository;
import br.com.mathinclui.perfilpedagogico.domain.NivelApoio;
import br.com.mathinclui.perfilpedagogico.domain.TamanhoFonte;
import br.com.mathinclui.questao.repository.AlternativaRepository;
import br.com.mathinclui.questao.repository.QuestaoRepository;
import br.com.mathinclui.resposta.repository.RespostaRepository;
import br.com.mathinclui.resposta.repository.RespostaTentativaRepository;
import br.com.mathinclui.tentativa.repository.TentativaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlunoAtividadeAccessibilityTest {
    @Mock AlunoRepository alunoRepository;
    @Mock AtribuicaoRepository atribuicaoRepository;
    @Mock AtividadeRepository atividadeRepository;
    @Mock QuestaoRepository questaoRepository;
    @Mock AlternativaRepository alternativaRepository;
    @Mock TentativaRepository tentativaRepository;
    @Mock RespostaRepository respostaRepository;
    @Mock RespostaTentativaRepository historicoRepository;
    @Mock AcessibilidadeEfetivaService acessibilidadeService;

    private AlunoAtividadeService service;

    @BeforeEach
    void setUp() {
        service = new AlunoAtividadeService(
                alunoRepository, atribuicaoRepository, atividadeRepository, questaoRepository,
                alternativaRepository, tentativaRepository, respostaRepository, historicoRepository,
                acessibilidadeService, Clock.fixed(Instant.parse("2026-10-16T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void detalheDeveUsarConfiguracaoEfetivaSemAlterarContratoDasQuestoes() {
        var aluno = new Aluno(20L, "Ana", "ana", "hash");
        var atribuicao = new Atribuicao(10L, 1L, 20L, null,
                Instant.parse("2026-10-16T00:00:00Z"), null);
        var atividade = mock(Atividade.class);
        var acessibilidade = new ConfiguracaoAcessibilidadeEfetiva(
                TamanhoFonte.EXTRA_GRANDE, true, true, true, true,
                (short) 1, true, NivelApoio.AMPLIADO);

        when(alunoRepository.findById(2L)).thenReturn(Optional.of(aluno));
        when(atribuicaoRepository.findById(30L)).thenReturn(Optional.of(atribuicao));
        when(atividadeRepository.findById(10L)).thenReturn(Optional.of(atividade));
        when(atividade.isPublicada()).thenReturn(true);
        when(atividade.getId()).thenReturn(10L);
        when(atividade.getTitulo()).thenReturn("Frações");
        when(atividade.getAnoEscolar()).thenReturn((short) 7);
        when(atividade.getConteudo()).thenReturn("FRACOES");
        when(questaoRepository.findAllByAtividadeIdOrderByOrdemAsc(10L)).thenReturn(List.of());
        when(tentativaRepository.findByAtribuicaoIdAndAlunoId(30L, 2L)).thenReturn(Optional.empty());
        when(acessibilidadeService.calcular(2L, 30L)).thenReturn(acessibilidade);

        var detalhe = service.detalhar(2L, 30L);

        verify(acessibilidadeService).calcular(2L, 30L);
        assertThat(detalhe.acessibilidade().tamanhoFonte()).isEqualTo(TamanhoFonte.EXTRA_GRANDE);
        assertThat(detalhe.acessibilidade().leituraVoz()).isTrue();
        assertThat(detalhe.acessibilidade().nivelApoio()).isEqualTo(NivelApoio.AMPLIADO);
        assertThat(detalhe.questoes()).isEmpty();
    }
}

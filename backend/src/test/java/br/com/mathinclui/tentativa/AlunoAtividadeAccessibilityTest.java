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
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AlunoAtividadeAccessibilityTest {
    @Test
    void detalheUsaConfiguracaoEfetivaSemGabarito() {
        var alunoRepository = mock(AlunoRepository.class);
        var atribuicaoRepository = mock(AtribuicaoRepository.class);
        var atividadeRepository = mock(AtividadeRepository.class);
        var questaoRepository = mock(QuestaoRepository.class);
        var alternativaRepository = mock(AlternativaRepository.class);
        var tentativaRepository = mock(TentativaRepository.class);
        var respostaRepository = mock(RespostaRepository.class);
        var historicoRepository = mock(RespostaTentativaRepository.class);
        var acessibilidadeService = mock(AcessibilidadeEfetivaService.class);

        var aluno = mock(Aluno.class);
        when(aluno.getId()).thenReturn(20L);
        when(aluno.isAtivo()).thenReturn(true);
        when(aluno.getTurmaId()).thenReturn(30L);
        when(alunoRepository.findById(20L)).thenReturn(Optional.of(aluno));
        var atribuicao = mock(Atribuicao.class);
        when(atribuicao.destinadaAo(20L, 30L)).thenReturn(true);
        when(atribuicao.getAtividadeId()).thenReturn(5L);
        when(atribuicaoRepository.findById(10L)).thenReturn(Optional.of(atribuicao));
        var atividade = mock(Atividade.class);
        when(atividade.isPublicada()).thenReturn(true);
        when(atividade.getId()).thenReturn(5L);
        when(atividade.getTitulo()).thenReturn("Frações");
        when(atividade.getAnoEscolar()).thenReturn((short) 7);
        when(atividade.getConteudo()).thenReturn("FRACOES");
        when(atividadeRepository.findById(5L)).thenReturn(Optional.of(atividade));
        when(tentativaRepository.findByAtribuicaoIdAndAlunoId(10L, 20L)).thenReturn(Optional.empty());
        when(questaoRepository.findAllByAtividadeIdOrderByOrdemAsc(5L)).thenReturn(List.of());
        var efetiva = new ConfiguracaoAcessibilidadeEfetiva(TamanhoFonte.EXTRA_GRANDE, true, true, true,
                true, (short) 1, true, NivelApoio.AMPLIADO);
        when(acessibilidadeService.calcular(20L, 10L)).thenReturn(efetiva);

        var service = new AlunoAtividadeService(alunoRepository, atribuicaoRepository, atividadeRepository,
                questaoRepository, alternativaRepository, tentativaRepository, respostaRepository,
                historicoRepository, acessibilidadeService,
                Clock.fixed(Instant.parse("2026-10-16T12:00:00Z"), ZoneOffset.UTC));

        var detalhe = service.detalhar(20L, 10L);

        assertThat(detalhe.acessibilidade().tamanhoFonte()).isEqualTo(TamanhoFonte.EXTRA_GRANDE);
        assertThat(detalhe.acessibilidade().repeticaoGuiada()).isTrue();
        assertThat(detalhe.questoes()).isEmpty();
        verify(acessibilidadeService).calcular(20L, 10L);
    }
}

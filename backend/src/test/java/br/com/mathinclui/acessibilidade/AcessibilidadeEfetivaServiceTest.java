package br.com.mathinclui.acessibilidade;

import br.com.mathinclui.acessibilidade.domain.AdaptacaoAlunoAtribuicao;
import br.com.mathinclui.acessibilidade.domain.AdaptacaoAtribuicao;
import br.com.mathinclui.acessibilidade.domain.PreferenciaInterfaceAluno;
import br.com.mathinclui.acessibilidade.repository.AdaptacaoAlunoAtribuicaoRepository;
import br.com.mathinclui.acessibilidade.repository.AdaptacaoAtribuicaoRepository;
import br.com.mathinclui.acessibilidade.repository.PreferenciaInterfaceAlunoRepository;
import br.com.mathinclui.aluno.domain.Aluno;
import br.com.mathinclui.aluno.repository.AlunoRepository;
import br.com.mathinclui.atribuicao.domain.Atribuicao;
import br.com.mathinclui.atribuicao.repository.AtribuicaoRepository;
import br.com.mathinclui.perfilpedagogico.domain.NivelApoio;
import br.com.mathinclui.perfilpedagogico.domain.PerfilPedagogico;
import br.com.mathinclui.perfilpedagogico.domain.TamanhoFonte;
import br.com.mathinclui.perfilpedagogico.repository.PerfilPedagogicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcessibilidadeEfetivaServiceTest {
    @Mock AlunoRepository alunoRepository;
    @Mock AtribuicaoRepository atribuicaoRepository;
    @Mock PerfilPedagogicoRepository perfilRepository;
    @Mock PreferenciaInterfaceAlunoRepository preferenciaRepository;
    @Mock AdaptacaoAtribuicaoRepository adaptacaoRepository;
    @Mock AdaptacaoAlunoAtribuicaoRepository adaptacaoAlunoRepository;

    private AcessibilidadeEfetivaService service;
    private final Long alunoId = 20L;
    private final Long turmaId = 30L;
    private final Long atribuicaoId = 40L;

    @BeforeEach
    void setUp() {
        service = new AcessibilidadeEfetivaService(alunoRepository, atribuicaoRepository, perfilRepository,
                preferenciaRepository, adaptacaoRepository, adaptacaoAlunoRepository);
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(new Aluno(turmaId, "Ana", "ana", "hash")));
        when(atribuicaoRepository.findById(atribuicaoId)).thenReturn(Optional.of(
                new Atribuicao(1L, 2L, turmaId, null, Instant.parse("2026-10-16T00:00:00Z"), null)));
        when(adaptacaoRepository.findById(atribuicaoId)).thenReturn(Optional.empty());
        when(adaptacaoAlunoRepository.findByAtribuicaoIdAndAlunoId(atribuicaoId, alunoId)).thenReturn(Optional.empty());
        when(preferenciaRepository.findById(alunoId)).thenReturn(Optional.empty());
    }

    @Test
    void preferenciaDoAlunoNaoReduzFonteMinimaDoPerfil() {
        when(perfilRepository.findByAlunoId(alunoId)).thenReturn(Optional.of(perfil(
                TamanhoFonte.GRANDE, false, false, false, false, (short) 2, false, NivelApoio.MODERADO)));
        var preferencia = new PreferenciaInterfaceAluno(alunoId);
        preferencia.atualizar(TamanhoFonte.PADRAO, false, false);
        when(preferenciaRepository.findById(alunoId)).thenReturn(Optional.of(preferencia));

        var result = service.calcular(alunoId, atribuicaoId);

        assertThat(result.tamanhoFonte()).isEqualTo(TamanhoFonte.GRANDE);
        assertThat(result.questoesPorTela()).isEqualTo((short) 2);
        assertThat(result.nivelApoio()).isEqualTo(NivelApoio.MODERADO);
    }

    @Test
    void preferenciaDoAlunoPodeAumentarFonte() {
        when(perfilRepository.findByAlunoId(alunoId)).thenReturn(Optional.of(perfil(
                TamanhoFonte.GRANDE, false, false, false, false, (short) 2, false, NivelApoio.PADRAO)));
        var preferencia = new PreferenciaInterfaceAluno(alunoId);
        preferencia.atualizar(TamanhoFonte.EXTRA_GRANDE, false, false);
        when(preferenciaRepository.findById(alunoId)).thenReturn(Optional.of(preferencia));

        assertThat(service.calcular(alunoId, atribuicaoId).tamanhoFonte()).isEqualTo(TamanhoFonte.EXTRA_GRANDE);
    }

    @Test
    void combinaApoioPorOrMinimoDeQuestoesEMaiorNivel() {
        when(perfilRepository.findByAlunoId(alunoId)).thenReturn(Optional.of(perfil(
                TamanhoFonte.PADRAO, false, false, false, false, (short) 4, false, NivelApoio.PADRAO)));
        when(adaptacaoRepository.findById(atribuicaoId)).thenReturn(Optional.of(new AdaptacaoAtribuicao(
                atribuicaoId, TamanhoFonte.GRANDE, false, true, true, null, (short) 3, null, NivelApoio.MODERADO)));
        when(adaptacaoAlunoRepository.findByAtribuicaoIdAndAlunoId(atribuicaoId, alunoId)).thenReturn(Optional.of(
                new AdaptacaoAlunoAtribuicao(atribuicaoId, alunoId, null, true, false, null, true,
                        (short) 1, true, NivelApoio.AMPLIADO)));
        var pref = new PreferenciaInterfaceAluno(alunoId);
        pref.atualizar(TamanhoFonte.PADRAO, false, false);
        when(preferenciaRepository.findById(alunoId)).thenReturn(Optional.of(pref));

        var result = service.calcular(alunoId, atribuicaoId);

        assertThat(result.tamanhoFonte()).isEqualTo(TamanhoFonte.GRANDE);
        assertThat(result.leituraVoz()).isTrue();
        assertThat(result.apoioVisual()).isTrue();
        assertThat(result.dicasPassoAPasso()).isTrue();
        assertThat(result.repeticaoGuiada()).isTrue();
        assertThat(result.questoesPorTela()).isEqualTo((short) 1);
        assertThat(result.tempoAmpliado()).isTrue();
        assertThat(result.nivelApoio()).isEqualTo(NivelApoio.AMPLIADO);
    }

    @Test
    void valoresNulosDeOverrideHerdamPerfil() {
        when(perfilRepository.findByAlunoId(alunoId)).thenReturn(Optional.of(perfil(
                TamanhoFonte.GRANDE, true, true, true, true, (short) 2, true, NivelApoio.MODERADO)));
        when(adaptacaoRepository.findById(atribuicaoId)).thenReturn(Optional.of(new AdaptacaoAtribuicao(
                atribuicaoId, null, null, null, null, null, null, null, null)));

        var result = service.calcular(alunoId, atribuicaoId);

        assertThat(result.tamanhoFonte()).isEqualTo(TamanhoFonte.GRANDE);
        assertThat(result.leituraVoz()).isTrue();
        assertThat(result.apoioVisual()).isTrue();
        assertThat(result.questoesPorTela()).isEqualTo((short) 2);
        assertThat(result.nivelApoio()).isEqualTo(NivelApoio.MODERADO);
    }

    @Test
    void rejeitaAtribuicaoQueNaoPertenceAoAlunoNemSuaTurma() {
        when(atribuicaoRepository.findById(atribuicaoId)).thenReturn(Optional.of(
                new Atribuicao(1L, 2L, 999L, null, Instant.parse("2026-10-16T00:00:00Z"), null)));

        assertThatThrownBy(() -> service.calcular(alunoId, atribuicaoId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    private PerfilPedagogico perfil(TamanhoFonte fonte, boolean leitura, boolean visual, boolean dicas,
                                    boolean repeticao, short questoes, boolean tempo, NivelApoio nivel) {
        var perfil = new PerfilPedagogico(alunoId);
        perfil.atualizar(fonte, leitura, visual, dicas, repeticao, questoes, tempo, nivel);
        return perfil;
    }
}

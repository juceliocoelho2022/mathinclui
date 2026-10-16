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
import org.springframework.http.HttpStatus;
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
    @Mock AdaptacaoAtribuicaoRepository adaptacaoRepository;
    @Mock AdaptacaoAlunoAtribuicaoRepository adaptacaoAlunoRepository;
    @Mock PreferenciaInterfaceAlunoRepository preferenciaRepository;

    private AcessibilidadeEfetivaService service;

    @BeforeEach
    void setUp() {
        service = new AcessibilidadeEfetivaService(
                alunoRepository, atribuicaoRepository, perfilRepository,
                adaptacaoRepository, adaptacaoAlunoRepository, preferenciaRepository);
    }

    @Test
    void deveComporApoiosSemPermitirReducaoEUsarMaiorFonteMenorCargaEMaiorNivel() {
        var aluno = new Aluno(20L, "Ana", "ana", "hash");
        var atribuicao = new Atribuicao(10L, 1L, 20L, null,
                Instant.parse("2026-10-16T00:00:00Z"), null);
        var perfil = new PerfilPedagogico(2L);
        perfil.atualizar(TamanhoFonte.GRANDE, true, false, true, false,
                (short) 3, false, NivelApoio.MODERADO);

        var geral = new AdaptacaoAtribuicao(30L);
        geral.atualizar(TamanhoFonte.PADRAO, false, true, null, true,
                (short) 2, true, NivelApoio.PADRAO);
        var individual = new AdaptacaoAlunoAtribuicao(30L, 2L);
        individual.atualizar(TamanhoFonte.EXTRA_GRANDE, false, false, false, null,
                (short) 1, false, NivelApoio.AMPLIADO);
        var preferencia = new PreferenciaInterfaceAluno(2L);
        preferencia.atualizar(TamanhoFonte.PADRAO, false, false);

        when(alunoRepository.findById(2L)).thenReturn(Optional.of(aluno));
        when(atribuicaoRepository.findById(30L)).thenReturn(Optional.of(atribuicao));
        when(perfilRepository.findByAlunoId(2L)).thenReturn(Optional.of(perfil));
        when(adaptacaoRepository.findByAtribuicaoId(30L)).thenReturn(Optional.of(geral));
        when(adaptacaoAlunoRepository.findByAtribuicaoIdAndAlunoId(30L, 2L)).thenReturn(Optional.of(individual));
        when(preferenciaRepository.findByAlunoId(2L)).thenReturn(Optional.of(preferencia));

        var efetiva = service.calcular(2L, 30L);

        assertThat(efetiva.tamanhoFonte()).isEqualTo(TamanhoFonte.EXTRA_GRANDE);
        assertThat(efetiva.leituraVoz()).isTrue();
        assertThat(efetiva.apoioVisual()).isTrue();
        assertThat(efetiva.dicasPassoAPasso()).isTrue();
        assertThat(efetiva.repeticaoGuiada()).isTrue();
        assertThat(efetiva.questoesPorTela()).isEqualTo((short) 1);
        assertThat(efetiva.tempoAmpliado()).isTrue();
        assertThat(efetiva.nivelApoio()).isEqualTo(NivelApoio.AMPLIADO);
    }

    @Test
    void devePermitirQuePreferenciaAtiveRecursosOpcionaisEAumenteFonte() {
        var aluno = new Aluno(20L, "Ana", "ana", "hash");
        var atribuicao = new Atribuicao(10L, 1L, 20L, null,
                Instant.parse("2026-10-16T00:00:00Z"), null);
        var perfil = new PerfilPedagogico(2L);
        perfil.atualizar(TamanhoFonte.PADRAO, false, false, false, false,
                (short) 2, false, NivelApoio.PADRAO);
        var preferencia = new PreferenciaInterfaceAluno(2L);
        preferencia.atualizar(TamanhoFonte.GRANDE, true, true);

        when(alunoRepository.findById(2L)).thenReturn(Optional.of(aluno));
        when(atribuicaoRepository.findById(30L)).thenReturn(Optional.of(atribuicao));
        when(perfilRepository.findByAlunoId(2L)).thenReturn(Optional.of(perfil));
        when(adaptacaoRepository.findByAtribuicaoId(30L)).thenReturn(Optional.empty());
        when(adaptacaoAlunoRepository.findByAtribuicaoIdAndAlunoId(30L, 2L)).thenReturn(Optional.empty());
        when(preferenciaRepository.findByAlunoId(2L)).thenReturn(Optional.of(preferencia));

        var efetiva = service.calcular(2L, 30L);

        assertThat(efetiva.tamanhoFonte()).isEqualTo(TamanhoFonte.GRANDE);
        assertThat(efetiva.leituraVoz()).isTrue();
        assertThat(efetiva.apoioVisual()).isTrue();
        assertThat(efetiva.questoesPorTela()).isEqualTo((short) 2);
    }

    @Test
    void deveHerdarPerfilQuandoOverridesEPreferenciasNaoExistem() {
        var aluno = new Aluno(20L, "Ana", "ana", "hash");
        var atribuicao = new Atribuicao(10L, 1L, 20L, null,
                Instant.parse("2026-10-16T00:00:00Z"), null);
        var perfil = new PerfilPedagogico(2L);
        perfil.atualizar(TamanhoFonte.GRANDE, true, true, false, true,
                (short) 4, true, NivelApoio.MODERADO);

        when(alunoRepository.findById(2L)).thenReturn(Optional.of(aluno));
        when(atribuicaoRepository.findById(30L)).thenReturn(Optional.of(atribuicao));
        when(perfilRepository.findByAlunoId(2L)).thenReturn(Optional.of(perfil));
        when(adaptacaoRepository.findByAtribuicaoId(30L)).thenReturn(Optional.empty());
        when(adaptacaoAlunoRepository.findByAtribuicaoIdAndAlunoId(30L, 2L)).thenReturn(Optional.empty());
        when(preferenciaRepository.findByAlunoId(2L)).thenReturn(Optional.empty());

        var efetiva = service.calcular(2L, 30L);

        assertThat(efetiva.tamanhoFonte()).isEqualTo(TamanhoFonte.GRANDE);
        assertThat(efetiva.leituraVoz()).isTrue();
        assertThat(efetiva.apoioVisual()).isTrue();
        assertThat(efetiva.repeticaoGuiada()).isTrue();
        assertThat(efetiva.questoesPorTela()).isEqualTo((short) 4);
        assertThat(efetiva.tempoAmpliado()).isTrue();
        assertThat(efetiva.nivelApoio()).isEqualTo(NivelApoio.MODERADO);
    }

    @Test
    void deveRetornar404QuandoAtribuicaoNaoForDestinadaAoAluno() {
        var aluno = new Aluno(20L, "Ana", "ana", "hash");
        var atribuicaoDeOutraTurma = new Atribuicao(10L, 1L, 99L, null,
                Instant.parse("2026-10-16T00:00:00Z"), null);
        when(alunoRepository.findById(2L)).thenReturn(Optional.of(aluno));
        when(atribuicaoRepository.findById(30L)).thenReturn(Optional.of(atribuicaoDeOutraTurma));

        assertThatThrownBy(() -> service.calcular(2L, 30L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}

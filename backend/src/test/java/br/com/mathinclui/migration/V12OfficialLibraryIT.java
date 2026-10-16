package br.com.mathinclui.migration;

import br.com.mathinclui.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class V12OfficialLibraryIT extends PostgresIntegrationTest {

    @Test
    void deveAplicarFlywayAteV12() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("12");
    }

    @Test
    void deveSemearSeisHabilidadesBnccValidadasDuasPorAno() throws Exception {
        try (Connection c = connection()) {
            assertThat(count(c, "SELECT COUNT(*) FROM habilidades_bncc WHERE codigo IN ('EF06MA07','EF06MA09','EF07MA17','EF07MA18','EF08MA04','EF08MA06')"))
                    .isEqualTo(6);
            assertThat(count(c, "SELECT COUNT(*) FROM habilidades_bncc WHERE ano_escolar=6 AND codigo IN ('EF06MA07','EF06MA09')"))
                    .isEqualTo(2);
            assertThat(count(c, "SELECT COUNT(*) FROM habilidades_bncc WHERE ano_escolar=7 AND codigo IN ('EF07MA17','EF07MA18')"))
                    .isEqualTo(2);
            assertThat(count(c, "SELECT COUNT(*) FROM habilidades_bncc WHERE ano_escolar=8 AND codigo IN ('EF08MA04','EF08MA06')"))
                    .isEqualTo(2);
        }
    }

    @Test
    void deveSemearDuasAtividadesOficiaisPorAnoComClassificacao() throws Exception {
        try (Connection c = connection()) {
            assertThat(count(c, "SELECT COUNT(*) FROM atividades WHERE titulo LIKE 'Biblioteca v0.5 - %' AND tipo_origem='SISTEMA' AND publicada=TRUE AND professor_id IS NULL"))
                    .isEqualTo(6);
            for (int ano = 6; ano <= 8; ano++) {
                assertThat(count(c, "SELECT COUNT(*) FROM atividades WHERE titulo LIKE 'Biblioteca v0.5 - %' AND ano_escolar=?", ano))
                        .as("duas atividades oficiais do %dº ano", ano)
                        .isEqualTo(2);
            }
            assertThat(count(c, "SELECT COUNT(DISTINCT a.id) FROM atividades a JOIN atividade_habilidade_bncc ah ON ah.atividade_id=a.id WHERE a.titulo LIKE 'Biblioteca v0.5 - %'"))
                    .isEqualTo(6);
        }
    }

    @Test
    void deveCobrirTresTiposDeQuestaoEAlternativaCorretaUnica() throws Exception {
        try (Connection c = connection()) {
            assertThat(count(c, "SELECT COUNT(DISTINCT q.tipo) FROM questoes q JOIN atividades a ON a.id=q.atividade_id WHERE a.titulo LIKE 'Biblioteca v0.5 - %'"))
                    .isEqualTo(3);
            assertThat(count(c, "SELECT COUNT(*) FROM questoes q JOIN atividades a ON a.id=q.atividade_id WHERE a.titulo LIKE 'Biblioteca v0.5 - %' AND q.tipo='MULTIPLA_ESCOLHA'"))
                    .isGreaterThan(0);
            assertThat(count(c, "SELECT COUNT(*) FROM (SELECT q.id FROM questoes q JOIN atividades a ON a.id=q.atividade_id JOIN alternativas alt ON alt.questao_id=q.id WHERE a.titulo LIKE 'Biblioteca v0.5 - %' AND q.tipo='MULTIPLA_ESCOLHA' GROUP BY q.id HAVING SUM(CASE WHEN alt.correta THEN 1 ELSE 0 END) <> 1) x"))
                    .isZero();
        }
    }

    @Test
    void deveSemearBancoOficialDeQuestoesReutilizaveis() throws Exception {
        try (Connection c = connection()) {
            assertThat(count(c, "SELECT COUNT(*) FROM questoes_modelo WHERE tipo_origem='SISTEMA' AND professor_id IS NULL"))
                    .isGreaterThanOrEqualTo(6);
            for (int ano = 6; ano <= 8; ano++) {
                assertThat(count(c, "SELECT COUNT(*) FROM questoes_modelo WHERE tipo_origem='SISTEMA' AND ano_escolar=?", ano))
                        .as("questões-modelo oficiais do %dº ano", ano)
                        .isGreaterThanOrEqualTo(2);
            }
            assertThat(count(c, "SELECT COUNT(DISTINCT tipo) FROM questoes_modelo WHERE tipo_origem='SISTEMA'"))
                    .isEqualTo(3);
            assertThat(count(c, "SELECT COUNT(DISTINCT qm.id) FROM questoes_modelo qm JOIN questao_modelo_habilidade_bncc qh ON qh.questao_modelo_id=qm.id WHERE qm.tipo_origem='SISTEMA'"))
                    .isGreaterThanOrEqualTo(6);
        }
    }

    private long count(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("COUNT did not return a row");
                return rs.getLong(1);
            }
        }
    }
}

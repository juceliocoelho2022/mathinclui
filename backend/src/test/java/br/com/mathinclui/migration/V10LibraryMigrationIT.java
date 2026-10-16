package br.com.mathinclui.migration;

import br.com.mathinclui.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class V10LibraryMigrationIT extends PostgresIntegrationTest {

    @Test
    void deveAplicarFlywayAteV10ECriarEstruturasDaBiblioteca() throws Exception {
        assertThat(Integer.parseInt(flyway.info().current().getVersion().getVersion())).isGreaterThanOrEqualTo(10);
        try (Connection c = connection()) {
            assertThat(count(c, """
                    SELECT COUNT(*) FROM information_schema.tables
                    WHERE table_schema='public' AND table_name IN (
                      'habilidades_bncc','tags_professor','questoes_modelo','alternativas_modelo',
                      'atividade_habilidade_bncc','questao_modelo_habilidade_bncc','atividade_tag','questao_modelo_tag')
                    """)).isEqualTo(8);
            assertThat(count(c, """
                    SELECT COUNT(*) FROM information_schema.columns
                    WHERE table_schema='public' AND
                      ((table_name='atividades' AND column_name IN ('origem_atividade_id','copiado_em')) OR
                       (table_name='questoes' AND column_name IN ('origem_questao_modelo_id','copiado_em')))
                    """)).isEqualTo(4);
        }
    }

    @Test
    void deveGarantirOwnershipCoerenteDeQuestaoModelo() throws Exception {
        try (Connection c = connection()) {
            long professor = professor(c, "owner");
            assertThatThrownBy(() -> execute(c, """
                    INSERT INTO questoes_modelo(professor_id,tipo_origem,enunciado,tipo,resposta_correta,ano_escolar,conteudo,dificuldade)
                    VALUES (?, 'SISTEMA', 'Q', 'NUMERICA', '1', 6, 'NUMEROS', 'BASICA')
                    """, professor)).isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> execute(c, """
                    INSERT INTO questoes_modelo(professor_id,tipo_origem,enunciado,tipo,resposta_correta,ano_escolar,conteudo,dificuldade)
                    VALUES (NULL, 'PROFESSOR', 'Q', 'NUMERICA', '1', 6, 'NUMEROS', 'BASICA')
                    """)).isInstanceOf(SQLException.class);
        }
    }

    @Test
    void deveValidarRespostaEAlternativaCorretaDoModelo() throws Exception {
        try (Connection c = connection()) {
            long professor = professor(c, "answer");
            assertThatThrownBy(() -> execute(c, """
                    INSERT INTO questoes_modelo(professor_id,tipo_origem,enunciado,tipo,resposta_correta,ano_escolar,conteudo,dificuldade)
                    VALUES (?, 'PROFESSOR', 'Q', 'NUMERICA', NULL, 6, 'NUMEROS', 'BASICA')
                    """, professor)).isInstanceOf(SQLException.class);

            long modelo = insertReturningId(c, """
                    INSERT INTO questoes_modelo(professor_id,tipo_origem,enunciado,tipo,resposta_correta,ano_escolar,conteudo,dificuldade)
                    VALUES (?, 'PROFESSOR', 'Q', 'MULTIPLA_ESCOLHA', NULL, 6, 'NUMEROS', 'BASICA') RETURNING id
                    """, professor);
            execute(c, "INSERT INTO alternativas_modelo(questao_modelo_id,texto,correta,ordem) VALUES (?, 'A', TRUE, 1)", modelo);
            assertThatThrownBy(() -> execute(c,
                    "INSERT INTO alternativas_modelo(questao_modelo_id,texto,correta,ordem) VALUES (?, 'B', TRUE, 2)", modelo))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void tagNormalizadaEhUnicaPorProfessorMasPodeRepetirEntreProfessores() throws Exception {
        try (Connection c = connection()) {
            long p1 = professor(c, "tag1");
            long p2 = professor(c, "tag2");
            execute(c, "INSERT INTO tags_professor(professor_id,nome,nome_normalizado) VALUES (?, 'Revisão', 'revisão')", p1);
            assertThatThrownBy(() -> execute(c,
                    "INSERT INTO tags_professor(professor_id,nome,nome_normalizado) VALUES (?, 'REVISÃO', 'revisão')", p1))
                    .isInstanceOf(SQLException.class);
            execute(c, "INSERT INTO tags_professor(professor_id,nome,nome_normalizado) VALUES (?, 'Revisão', 'revisão')", p2);
        }
    }

    @Test
    void relacoesCurricularesRejeitamIdsInexistentes() throws Exception {
        try (Connection c = connection()) {
            long professor = professor(c, "bncc");
            long atividade = atividade(c, professor, "Fonte BNCC");
            assertThatThrownBy(() -> execute(c,
                    "INSERT INTO atividade_habilidade_bncc(atividade_id,habilidade_id) VALUES (?, 999999999)", atividade))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void excluirFonteNaoApagaCopiaEAnulaProveniencia() throws Exception {
        try (Connection c = connection()) {
            long professor = professor(c, "copy");
            long fonte = atividade(c, professor, "Fonte");
            long copia = insertReturningId(c, """
                    INSERT INTO atividades(professor_id,titulo,ano_escolar,conteudo,dificuldade,tipo_origem,publicada,origem_atividade_id,copiado_em)
                    VALUES (?, 'Copia', 7, 'ALGEBRA', 'BASICA', 'PROFESSOR', FALSE, ?, CURRENT_TIMESTAMP) RETURNING id
                    """, professor, fonte);

            execute(c, "DELETE FROM atividades WHERE id=?", fonte);

            assertThat(count(c, "SELECT COUNT(*) FROM atividades WHERE id=?", copia)).isEqualTo(1);
            assertThat(count(c, "SELECT COUNT(*) FROM atividades WHERE id=? AND origem_atividade_id IS NULL", copia)).isEqualTo(1);
        }
    }

    private long professor(Connection c, String prefix) throws SQLException {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        return insertReturningId(c,
                "INSERT INTO professores(nome,email,senha_hash) VALUES ('Prof', ?, 'hash') RETURNING id",
                prefix + "-" + suffix + "@teste.local");
    }

    private long atividade(Connection c, long professorId, String titulo) throws SQLException {
        return insertReturningId(c, """
                INSERT INTO atividades(professor_id,titulo,ano_escolar,conteudo,dificuldade,tipo_origem,publicada)
                VALUES (?, ?, 7, 'ALGEBRA', 'BASICA', 'PROFESSOR', FALSE) RETURNING id
                """, professorId, titulo + UUID.randomUUID());
    }

    private long insertReturningId(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("INSERT did not return an id");
                return rs.getLong(1);
            }
        }
    }

    private long count(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("COUNT did not return a row");
                return rs.getLong(1);
            }
        }
    }

    private void execute(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
    }
}

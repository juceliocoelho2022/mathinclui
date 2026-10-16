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
    void deveAplicarFlywayPeloMenosAteV10ECriarEstruturas() throws Exception {
        assertThat(Integer.parseInt(flyway.info().current().getVersion().getVersion())).isGreaterThanOrEqualTo(10);
        try (Connection c = connection()) {
            assertThat(count(c, "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_name IN ('habilidades_bncc','atividade_habilidade_bncc','tags_professor','atividade_tag','questoes_modelo','alternativas_modelo','questao_modelo_habilidade_bncc','questao_modelo_tag')"))
                    .isEqualTo(8);
        }
    }

    @Test
    void deveGarantirOwnershipCoerenteDeModelo() throws Exception {
        try (Connection c = connection()) {
            long professorId = professor(c, "own");
            assertThatThrownBy(() -> execute(c,
                    "INSERT INTO questoes_modelo(professor_id,tipo_origem,enunciado,tipo,resposta_correta,ano_escolar,conteudo,dificuldade) VALUES (?, 'SISTEMA','Q','NUMERICA','1',7,'NUMEROS','BASICA')", professorId))
                    .isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> execute(c,
                    "INSERT INTO questoes_modelo(tipo_origem,enunciado,tipo,resposta_correta,ano_escolar,conteudo,dificuldade) VALUES ('PROFESSOR','Q','NUMERICA','1',7,'NUMEROS','BASICA')"))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void deveExigirRespostaParaNaoMultiplaEscolhaEUmaAlternativaCorretaNoMaximo() throws Exception {
        try (Connection c = connection()) {
            long professorId = professor(c, "answer");
            assertThatThrownBy(() -> execute(c,
                    "INSERT INTO questoes_modelo(professor_id,tipo_origem,enunciado,tipo,ano_escolar,conteudo,dificuldade) VALUES (?,'PROFESSOR','Q','NUMERICA',7,'NUMEROS','BASICA')", professorId))
                    .isInstanceOf(SQLException.class);
            long modeloId = insertReturningId(c,
                    "INSERT INTO questoes_modelo(professor_id,tipo_origem,enunciado,tipo,ano_escolar,conteudo,dificuldade) VALUES (?,'PROFESSOR','Q','MULTIPLA_ESCOLHA',7,'NUMEROS','BASICA') RETURNING id", professorId);
            execute(c, "INSERT INTO alternativas_modelo(questao_modelo_id,texto,correta,ordem) VALUES (?,'A',TRUE,1)", modeloId);
            assertThatThrownBy(() -> execute(c,
                    "INSERT INTO alternativas_modelo(questao_modelo_id,texto,correta,ordem) VALUES (?,'B',TRUE,2)", modeloId))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void deveNormalizarUnicidadeDeTagPorProfessorSemImpedirOutroProfessor() throws Exception {
        try (Connection c = connection()) {
            long p1 = professor(c, "tag1");
            long p2 = professor(c, "tag2");
            execute(c, "INSERT INTO tags_professor(professor_id,nome,nome_normalizado) VALUES (?,'Frações','frações')", p1);
            assertThatThrownBy(() -> execute(c,
                    "INSERT INTO tags_professor(professor_id,nome,nome_normalizado) VALUES (?,'FRAÇÕES','frações')", p1))
                    .isInstanceOf(SQLException.class);
            execute(c, "INSERT INTO tags_professor(professor_id,nome,nome_normalizado) VALUES (?,'Frações','frações')", p2);
            assertThat(count(c, "SELECT COUNT(*) FROM tags_professor WHERE nome_normalizado='frações'")).isEqualTo(2);
        }
    }

    @Test
    void provenienciaDaCopiaNaoDeveBloquearExclusaoNemApagarCopia() throws Exception {
        try (Connection c = connection()) {
            long professorId = professor(c, "copy");
            long origem = atividade(c, professorId, "Origem");
            long copia = insertReturningId(c,
                    "INSERT INTO atividades(professor_id,titulo,ano_escolar,conteudo,dificuldade,tipo_origem,publicada,origem_atividade_id,copiado_em) VALUES (?,'Cópia',7,'NUMEROS','BASICA','PROFESSOR',FALSE,?,CURRENT_TIMESTAMP) RETURNING id",
                    professorId, origem);
            execute(c, "DELETE FROM atividades WHERE id=?", origem);
            assertThat(count(c, "SELECT COUNT(*) FROM atividades WHERE id=? AND origem_atividade_id IS NULL", copia)).isEqualTo(1);
        }
    }

    @Test
    void relacoesCurricularesDevemRejeitarIdsInexistentes() throws Exception {
        try (Connection c = connection()) {
            long professorId = professor(c, "fk");
            long atividadeId = atividade(c, professorId, "Com BNCC");
            assertThatThrownBy(() -> execute(c,
                    "INSERT INTO atividade_habilidade_bncc(atividade_id,habilidade_id) VALUES (?,999999999)", atividadeId))
                    .isInstanceOf(SQLException.class);
        }
    }

    private long professor(Connection c, String prefix) throws SQLException {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return insertReturningId(c,
                "INSERT INTO professores(nome,email,senha_hash) VALUES ('Prof',?,'hash') RETURNING id",
                prefix + "-" + suffix + "@teste.local");
    }

    private long atividade(Connection c, long professorId, String titulo) throws SQLException {
        return insertReturningId(c,
                "INSERT INTO atividades(professor_id,titulo,ano_escolar,conteudo,dificuldade,tipo_origem,publicada) VALUES (?,?,7,'NUMEROS','BASICA','PROFESSOR',FALSE) RETURNING id",
                professorId, titulo + UUID.randomUUID().toString().substring(0, 5));
    }

    private long insertReturningId(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("INSERT did not return id");
                return rs.getLong(1);
            }
        }
    }

    private long count(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("COUNT did not return row");
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

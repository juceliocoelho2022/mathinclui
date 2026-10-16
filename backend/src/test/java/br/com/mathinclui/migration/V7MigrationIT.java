package br.com.mathinclui.migration;

import br.com.mathinclui.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class V7MigrationIT extends PostgresIntegrationTest {

    @Test
    void deveRejeitarLimiteDeTentativasZero() throws Exception {
        try (Connection c = connection()) {
            long atividadeId = fixture(c).atividadeId();
            assertThatThrownBy(() -> execute(c,
                    "UPDATE atividades SET limite_tentativas = 0 WHERE id = ?", atividadeId))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void devePermitirNoMaximoUmaAlternativaCorretaPorQuestao() throws Exception {
        try (Connection c = connection()) {
            Fixture fixture = fixture(c);
            execute(c, "INSERT INTO alternativas(questao_id, texto, correta, ordem) VALUES (?, 'A', TRUE, 1)", fixture.questaoId());
            assertThatThrownBy(() -> execute(c,
                    "INSERT INTO alternativas(questao_id, texto, correta, ordem) VALUES (?, 'B', TRUE, 2)", fixture.questaoId()))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void devePermitirUmaUnicaTentativaPorAtribuicaoEAluno() throws Exception {
        try (Connection c = connection()) {
            Fixture fixture = fixture(c);
            execute(c, "INSERT INTO tentativas(atribuicao_id, aluno_id) VALUES (?, ?)", fixture.atribuicaoId(), fixture.alunoId());
            assertThatThrownBy(() -> execute(c,
                    "INSERT INTO tentativas(atribuicao_id, aluno_id) VALUES (?, ?)", fixture.atribuicaoId(), fixture.alunoId()))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void deveGarantirNumeroDeSubmissaoUnicoPorQuestaoNaTentativa() throws Exception {
        try (Connection c = connection()) {
            Fixture fixture = fixture(c);
            long tentativaId = insertReturningId(c,
                    "INSERT INTO tentativas(atribuicao_id, aluno_id) VALUES (?, ?) RETURNING id",
                    fixture.atribuicaoId(), fixture.alunoId());
            execute(c, "INSERT INTO resposta_tentativas(tentativa_id, questao_id, numero_tentativa, resposta, correta, dica_liberada, tempo_segundos) VALUES (?, ?, 1, 'A', FALSE, 1, 4)", tentativaId, fixture.questaoId());
            assertThatThrownBy(() -> execute(c,
                    "INSERT INTO resposta_tentativas(tentativa_id, questao_id, numero_tentativa, resposta, correta, dica_liberada, tempo_segundos) VALUES (?, ?, 1, 'B', TRUE, 0, 2)",
                    tentativaId, fixture.questaoId()))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void devePermitirExcluirAtividadeComAtribuicaoSemTentativas() throws Exception {
        try (Connection c = connection()) {
            Fixture fixture = fixture(c);

            execute(c, "DELETE FROM atividades WHERE id = ?", fixture.atividadeId());

            assertThat(count(c, "SELECT COUNT(*) FROM atribuicoes WHERE id = ?", fixture.atribuicaoId())).isZero();
        }
    }

    private Fixture fixture(Connection c) throws SQLException {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        long professorId = insertReturningId(c,
                "INSERT INTO professores(nome, email, senha_hash) VALUES ('Prof Teste', ?, 'hash') RETURNING id",
                "prof-" + suffix + "@teste.local");
        long turmaId = insertReturningId(c,
                "INSERT INTO turmas(professor_id, nome, ano_escolar, codigo) VALUES (?, '7A', 7, ?) RETURNING id",
                professorId, "T" + suffix);
        long alunoId = insertReturningId(c,
                "INSERT INTO alunos(turma_id, nome, username, pin_hash) VALUES (?, 'Aluno Teste', ?, 'hash') RETURNING id",
                turmaId, "aluno_" + suffix);
        long atividadeId = insertReturningId(c,
                "INSERT INTO atividades(professor_id, titulo, ano_escolar, conteudo, dificuldade, tipo_origem, publicada) VALUES (?, ?, 7, 'PORCENTAGEM', 'BASICA', 'PROFESSOR', TRUE) RETURNING id",
                professorId, "Atividade " + suffix);
        long questaoId = insertReturningId(c,
                "INSERT INTO questoes(atividade_id, enunciado, tipo, resposta_correta, ordem) VALUES (?, 'Escolha', 'MULTIPLA_ESCOLHA', 'legacy', 1) RETURNING id",
                atividadeId);
        long atribuicaoId = insertReturningId(c,
                "INSERT INTO atribuicoes(atividade_id, professor_id, aluno_id, status) VALUES (?, ?, ?, 'ATIVA') RETURNING id",
                atividadeId, professorId, alunoId);
        return new Fixture(atividadeId, questaoId, atribuicaoId, alunoId);
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
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    private record Fixture(long atividadeId, long questaoId, long atribuicaoId, long alunoId) {}
}

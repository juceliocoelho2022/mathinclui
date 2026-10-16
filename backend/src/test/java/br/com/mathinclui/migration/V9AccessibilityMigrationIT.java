package br.com.mathinclui.migration;

import br.com.mathinclui.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class V9AccessibilityMigrationIT extends PostgresIntegrationTest {

    @Test
    void deveAplicarFlywayAteV9() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("9");
    }

    @Test
    void deveCriarAsTresEstruturasDeAcessibilidade() throws Exception {
        try (Connection c = connection()) {
            assertThat(count(c, "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_name IN ('preferencias_interface_aluno','adaptacoes_atribuicao','adaptacoes_aluno_atribuicao')"))
                    .isEqualTo(3);
        }
    }

    @Test
    void deveAceitarLiteraisPersistidosAtuais() throws Exception {
        try (Connection c = connection()) {
            Fixture f = fixture(c);
            execute(c, "INSERT INTO preferencias_interface_aluno(aluno_id, tamanho_fonte, leitura_voz, apoio_visual) VALUES (?, 'EXTRA_GRANDE', TRUE, TRUE)", f.alunoId());
            execute(c, "INSERT INTO adaptacoes_atribuicao(atribuicao_id, tamanho_fonte_minima, nivel_apoio) VALUES (?, 'GRANDE', 'AMPLIADO')", f.atribuicaoId());
            assertThat(count(c, "SELECT COUNT(*) FROM preferencias_interface_aluno WHERE aluno_id = ?", f.alunoId())).isEqualTo(1);
        }
    }

    @Test
    void deveRejeitarEnumInexistenteEQuestoesPorTelaZero() throws Exception {
        try (Connection c = connection()) {
            Fixture f = fixture(c);
            assertThatThrownBy(() -> execute(c,
                    "INSERT INTO preferencias_interface_aluno(aluno_id, tamanho_fonte) VALUES (?, 'NORMAL')", f.alunoId()))
                    .isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> execute(c,
                    "INSERT INTO adaptacoes_atribuicao(atribuicao_id, questoes_por_tela) VALUES (?, 0)", f.atribuicaoId()))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void deveGarantirUmaAdaptacaoIndividualPorAtribuicaoEAluno() throws Exception {
        try (Connection c = connection()) {
            Fixture f = fixture(c);
            execute(c, "INSERT INTO adaptacoes_aluno_atribuicao(atribuicao_id, aluno_id, apoio_visual_minimo) VALUES (?, ?, TRUE)", f.atribuicaoId(), f.alunoId());
            assertThatThrownBy(() -> execute(c,
                    "INSERT INTO adaptacoes_aluno_atribuicao(atribuicao_id, aluno_id, leitura_voz_minima) VALUES (?, ?, TRUE)", f.atribuicaoId(), f.alunoId()))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void deveAplicarForeignKeysDeAlunoEAtribuicao() throws Exception {
        try (Connection c = connection()) {
            assertThatThrownBy(() -> execute(c,
                    "INSERT INTO preferencias_interface_aluno(aluno_id) VALUES (999999999)"))
                    .isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> execute(c,
                    "INSERT INTO adaptacoes_atribuicao(atribuicao_id) VALUES (999999999)"))
                    .isInstanceOf(SQLException.class);
        }
    }

    private Fixture fixture(Connection c) throws SQLException {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        long professorId = insertReturningId(c,
                "INSERT INTO professores(nome, email, senha_hash) VALUES ('Prof V9', ?, 'hash') RETURNING id",
                "v9-" + suffix + "@teste.local");
        long turmaId = insertReturningId(c,
                "INSERT INTO turmas(professor_id, nome, ano_escolar, codigo) VALUES (?, '7A', 7, ?) RETURNING id",
                professorId, "V9" + suffix);
        long alunoId = insertReturningId(c,
                "INSERT INTO alunos(turma_id, nome, username, pin_hash) VALUES (?, 'Aluno V9', ?, 'hash') RETURNING id",
                turmaId, "v9_" + suffix);
        long atividadeId = insertReturningId(c,
                "INSERT INTO atividades(professor_id, titulo, ano_escolar, conteudo, dificuldade, tipo_origem, publicada) VALUES (?, ?, 7, 'NUMEROS', 'BASICA', 'PROFESSOR', TRUE) RETURNING id",
                professorId, "Atividade V9 " + suffix);
        long atribuicaoId = insertReturningId(c,
                "INSERT INTO atribuicoes(atividade_id, professor_id, turma_id, status) VALUES (?, ?, ?, 'ATIVA') RETURNING id",
                atividadeId, professorId, turmaId);
        return new Fixture(alunoId, atribuicaoId);
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

    private record Fixture(long alunoId, long atribuicaoId) {}
}

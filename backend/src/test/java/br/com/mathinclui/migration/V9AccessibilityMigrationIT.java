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
    void deveTerAplicadoV9OuSuperior() {
        int current = Integer.parseInt(flyway.info().current().getVersion().getVersion());
        assertThat(current).isGreaterThanOrEqualTo(9);
    }

    @Test
    void deveCriarAsTresTabelasDeAcessibilidade() throws Exception {
        try (Connection c = connection()) {
            assertThat(count(c, """
                    SELECT COUNT(*)
                    FROM information_schema.tables
                    WHERE table_schema = 'public'
                      AND table_name IN ('preferencias_interface_aluno','adaptacoes_atribuicao','adaptacoes_aluno_atribuicao')
                    """)).isEqualTo(3);
        }
    }

    @Test
    void deveAceitarLiteraisPersistidosAtuaisERejeitarEnumInvalido() throws Exception {
        try (Connection c = connection()) {
            Fixture f = fixture(c);
            execute(c, "INSERT INTO preferencias_interface_aluno(aluno_id,tamanho_fonte,leitura_voz,apoio_visual) VALUES (?, 'GRANDE', TRUE, FALSE)", f.alunoId());
            execute(c, "INSERT INTO adaptacoes_atribuicao(atribuicao_id,tamanho_fonte_minima,nivel_apoio) VALUES (?, 'EXTRA_GRANDE', 'AMPLIADO')", f.atribuicaoId());

            assertThatThrownBy(() -> execute(c,
                    "UPDATE preferencias_interface_aluno SET tamanho_fonte = 'NORMAL' WHERE aluno_id = ?", f.alunoId()))
                    .isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> execute(c,
                    "UPDATE adaptacoes_atribuicao SET nivel_apoio = 'ALTO' WHERE atribuicao_id = ?", f.atribuicaoId()))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void deveRejeitarQuestaoPorTelaForaDoIntervalo() throws Exception {
        try (Connection c = connection()) {
            Fixture f = fixture(c);
            assertThatThrownBy(() -> execute(c,
                    "INSERT INTO adaptacoes_atribuicao(atribuicao_id,questoes_por_tela) VALUES (?, 0)", f.atribuicaoId()))
                    .isInstanceOf(SQLException.class);
            execute(c, "INSERT INTO adaptacoes_atribuicao(atribuicao_id,questoes_por_tela) VALUES (?, 10)", f.atribuicaoId());
        }
    }

    @Test
    void deveGarantirUmaAdaptacaoIndividualPorAtribuicaoEAluno() throws Exception {
        try (Connection c = connection()) {
            Fixture f = fixture(c);
            execute(c, "INSERT INTO adaptacoes_aluno_atribuicao(atribuicao_id,aluno_id,apoio_visual_minimo) VALUES (?, ?, TRUE)", f.atribuicaoId(), f.alunoId());
            assertThatThrownBy(() -> execute(c,
                    "INSERT INTO adaptacoes_aluno_atribuicao(atribuicao_id,aluno_id,leitura_voz_minima) VALUES (?, ?, TRUE)", f.atribuicaoId(), f.alunoId()))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void deveAplicarFksEDeleteCascadeNosRegistrosDependentes() throws Exception {
        try (Connection c = connection()) {
            Fixture f = fixture(c);
            execute(c, "INSERT INTO preferencias_interface_aluno(aluno_id) VALUES (?)", f.alunoId());
            execute(c, "INSERT INTO adaptacoes_atribuicao(atribuicao_id) VALUES (?)", f.atribuicaoId());
            execute(c, "INSERT INTO adaptacoes_aluno_atribuicao(atribuicao_id,aluno_id) VALUES (?, ?)", f.atribuicaoId(), f.alunoId());

            assertThatThrownBy(() -> execute(c, "INSERT INTO preferencias_interface_aluno(aluno_id) VALUES (999999999)"))
                    .isInstanceOf(SQLException.class);

            execute(c, "DELETE FROM atribuicoes WHERE id = ?", f.atribuicaoId());
            assertThat(count(c, "SELECT COUNT(*) FROM adaptacoes_atribuicao WHERE atribuicao_id = ?", f.atribuicaoId())).isZero();
            assertThat(count(c, "SELECT COUNT(*) FROM adaptacoes_aluno_atribuicao WHERE atribuicao_id = ?", f.atribuicaoId())).isZero();

            execute(c, "DELETE FROM alunos WHERE id = ?", f.alunoId());
            assertThat(count(c, "SELECT COUNT(*) FROM preferencias_interface_aluno WHERE aluno_id = ?", f.alunoId())).isZero();
        }
    }

    private Fixture fixture(Connection c) throws SQLException {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        long professorId = insertReturningId(c,
                "INSERT INTO professores(nome,email,senha_hash) VALUES ('Prof V9', ?, 'hash') RETURNING id",
                "v9-" + suffix + "@teste.local");
        long turmaId = insertReturningId(c,
                "INSERT INTO turmas(professor_id,nome,ano_escolar,codigo) VALUES (?, '7A', 7, ?) RETURNING id",
                professorId, "V9" + suffix);
        long alunoId = insertReturningId(c,
                "INSERT INTO alunos(turma_id,nome,username,pin_hash) VALUES (?, 'Aluno V9', ?, 'hash') RETURNING id",
                turmaId, "v9_" + suffix);
        long atividadeId = insertReturningId(c,
                "INSERT INTO atividades(professor_id,titulo,ano_escolar,conteudo,dificuldade,tipo_origem,publicada) VALUES (?, ?, 7, 'ALGEBRA', 'BASICA', 'PROFESSOR', TRUE) RETURNING id",
                professorId, "Atividade V9 " + suffix);
        long atribuicaoId = insertReturningId(c,
                "INSERT INTO atribuicoes(atividade_id,professor_id,turma_id,status) VALUES (?, ?, ?, 'ATIVA') RETURNING id",
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

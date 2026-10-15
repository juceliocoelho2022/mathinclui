CREATE TABLE atribuicoes (
    id BIGSERIAL PRIMARY KEY,
    atividade_id BIGINT NOT NULL REFERENCES atividades(id),
    professor_id BIGINT NOT NULL REFERENCES professores(id),
    turma_id BIGINT REFERENCES turmas(id),
    aluno_id BIGINT REFERENCES alunos(id),
    data_inicio TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_limite TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVA' CHECK (status IN ('RASCUNHO', 'ATIVA', 'ENCERRADA', 'CANCELADA')),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_atribuicao_destino_exclusivo CHECK ((turma_id IS NOT NULL AND aluno_id IS NULL) OR (turma_id IS NULL AND aluno_id IS NOT NULL)),
    CONSTRAINT ck_atribuicao_datas CHECK (data_limite IS NULL OR data_limite >= data_inicio)
);
CREATE INDEX idx_atribuicoes_professor ON atribuicoes(professor_id);
CREATE INDEX idx_atribuicoes_turma ON atribuicoes(turma_id) WHERE turma_id IS NOT NULL;
CREATE INDEX idx_atribuicoes_aluno ON atribuicoes(aluno_id) WHERE aluno_id IS NOT NULL;

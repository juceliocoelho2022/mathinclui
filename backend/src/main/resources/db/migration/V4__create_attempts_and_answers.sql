CREATE TABLE tentativas (
    id BIGSERIAL PRIMARY KEY,
    atribuicao_id BIGINT NOT NULL REFERENCES atribuicoes(id),
    aluno_id BIGINT NOT NULL REFERENCES alunos(id),
    iniciada_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finalizada_em TIMESTAMPTZ,
    quantidade_acertos INTEGER NOT NULL DEFAULT 0 CHECK (quantidade_acertos >= 0),
    quantidade_erros INTEGER NOT NULL DEFAULT 0 CHECK (quantidade_erros >= 0),
    percentual NUMERIC(5,2) CHECK (percentual BETWEEN 0 AND 100),
    tempo_total_segundos INTEGER NOT NULL DEFAULT 0 CHECK (tempo_total_segundos >= 0)
);
CREATE INDEX idx_tentativas_aluno ON tentativas(aluno_id);
CREATE INDEX idx_tentativas_atribuicao ON tentativas(atribuicao_id);
CREATE TABLE respostas (
    id BIGSERIAL PRIMARY KEY,
    tentativa_id BIGINT NOT NULL REFERENCES tentativas(id) ON DELETE CASCADE,
    questao_id BIGINT NOT NULL REFERENCES questoes(id),
    resposta TEXT NOT NULL,
    correta BOOLEAN NOT NULL,
    quantidade_dicas SMALLINT NOT NULL DEFAULT 0 CHECK (quantidade_dicas BETWEEN 0 AND 3),
    tempo_segundos INTEGER NOT NULL DEFAULT 0 CHECK (tempo_segundos >= 0),
    respondida_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_resposta_tentativa_questao UNIQUE (tentativa_id, questao_id)
);

ALTER TABLE atividades
    ADD COLUMN limite_tentativas INTEGER;

UPDATE atividades
SET limite_tentativas = 3
WHERE limite_tentativas IS NULL;

ALTER TABLE atividades
    ALTER COLUMN limite_tentativas SET DEFAULT 3,
    ALTER COLUMN limite_tentativas SET NOT NULL,
    ADD CONSTRAINT ck_atividade_limite_tentativas CHECK (limite_tentativas >= 1);

ALTER TABLE questoes
    ADD COLUMN limite_tentativas INTEGER,
    ALTER COLUMN resposta_correta DROP NOT NULL,
    ADD CONSTRAINT ck_questao_limite_tentativas
        CHECK (limite_tentativas IS NULL OR limite_tentativas >= 1),
    ADD CONSTRAINT ck_questao_resposta_correta
        CHECK (tipo = 'MULTIPLA_ESCOLHA' OR resposta_correta IS NOT NULL);

CREATE UNIQUE INDEX uk_alternativa_questao_correta
    ON alternativas (questao_id)
    WHERE correta = TRUE;

ALTER TABLE tentativas
    ADD COLUMN status VARCHAR(20);

UPDATE tentativas
SET status = CASE
    WHEN finalizada_em IS NULL THEN 'EM_ANDAMENTO'
    ELSE 'FINALIZADA'
END
WHERE status IS NULL;

ALTER TABLE tentativas
    ALTER COLUMN status SET DEFAULT 'EM_ANDAMENTO',
    ALTER COLUMN status SET NOT NULL,
    ADD CONSTRAINT ck_tentativa_status CHECK (status IN ('EM_ANDAMENTO', 'FINALIZADA')),
    ADD CONSTRAINT uk_tentativa_atribuicao_aluno UNIQUE (atribuicao_id, aluno_id);

CREATE TABLE resposta_tentativas (
    id BIGSERIAL PRIMARY KEY,
    tentativa_id BIGINT NOT NULL REFERENCES tentativas(id) ON DELETE CASCADE,
    questao_id BIGINT NOT NULL REFERENCES questoes(id),
    numero_tentativa INTEGER NOT NULL CHECK (numero_tentativa >= 1),
    resposta TEXT NOT NULL,
    correta BOOLEAN NOT NULL,
    dica_liberada SMALLINT NOT NULL DEFAULT 0 CHECK (dica_liberada BETWEEN 0 AND 3),
    tempo_segundos INTEGER NOT NULL DEFAULT 0 CHECK (tempo_segundos >= 0),
    respondida_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_resposta_tentativa_numero
        UNIQUE (tentativa_id, questao_id, numero_tentativa)
);

CREATE INDEX idx_resposta_tentativas_tentativa
    ON resposta_tentativas(tentativa_id);

CREATE INDEX idx_resposta_tentativas_questao
    ON resposta_tentativas(questao_id);

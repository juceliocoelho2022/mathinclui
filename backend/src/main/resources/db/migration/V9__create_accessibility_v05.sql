CREATE TABLE preferencias_interface_aluno (
    aluno_id BIGINT PRIMARY KEY REFERENCES alunos(id) ON DELETE CASCADE,
    tamanho_fonte VARCHAR(20) NOT NULL DEFAULT 'PADRAO'
        CHECK (tamanho_fonte IN ('PADRAO', 'GRANDE', 'EXTRA_GRANDE')),
    leitura_voz BOOLEAN NOT NULL DEFAULT FALSE,
    apoio_visual BOOLEAN NOT NULL DEFAULT FALSE,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE adaptacoes_atribuicao (
    atribuicao_id BIGINT PRIMARY KEY REFERENCES atribuicoes(id) ON DELETE CASCADE,
    tamanho_fonte_minima VARCHAR(20)
        CHECK (tamanho_fonte_minima IS NULL OR tamanho_fonte_minima IN ('PADRAO', 'GRANDE', 'EXTRA_GRANDE')),
    leitura_voz_minima BOOLEAN,
    apoio_visual_minimo BOOLEAN,
    dicas_passo_a_passo BOOLEAN,
    repeticao_guiada BOOLEAN,
    questoes_por_tela SMALLINT
        CHECK (questoes_por_tela IS NULL OR questoes_por_tela BETWEEN 1 AND 10),
    tempo_ampliado BOOLEAN,
    nivel_apoio VARCHAR(20)
        CHECK (nivel_apoio IS NULL OR nivel_apoio IN ('PADRAO', 'MODERADO', 'AMPLIADO')),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE adaptacoes_aluno_atribuicao (
    id BIGSERIAL PRIMARY KEY,
    atribuicao_id BIGINT NOT NULL REFERENCES atribuicoes(id) ON DELETE CASCADE,
    aluno_id BIGINT NOT NULL REFERENCES alunos(id) ON DELETE CASCADE,
    tamanho_fonte_minima VARCHAR(20)
        CHECK (tamanho_fonte_minima IS NULL OR tamanho_fonte_minima IN ('PADRAO', 'GRANDE', 'EXTRA_GRANDE')),
    leitura_voz_minima BOOLEAN,
    apoio_visual_minimo BOOLEAN,
    dicas_passo_a_passo BOOLEAN,
    repeticao_guiada BOOLEAN,
    questoes_por_tela SMALLINT
        CHECK (questoes_por_tela IS NULL OR questoes_por_tela BETWEEN 1 AND 10),
    tempo_ampliado BOOLEAN,
    nivel_apoio VARCHAR(20)
        CHECK (nivel_apoio IS NULL OR nivel_apoio IN ('PADRAO', 'MODERADO', 'AMPLIADO')),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_adaptacao_aluno_atribuicao UNIQUE (atribuicao_id, aluno_id)
);

CREATE INDEX idx_adaptacoes_aluno_atribuicao_aluno
    ON adaptacoes_aluno_atribuicao(aluno_id);

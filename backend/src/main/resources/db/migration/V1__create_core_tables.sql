CREATE TABLE professores (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE turmas (
    id BIGSERIAL PRIMARY KEY,
    professor_id BIGINT NOT NULL REFERENCES professores(id),
    nome VARCHAR(100) NOT NULL,
    ano_escolar SMALLINT NOT NULL CHECK (ano_escolar BETWEEN 6 AND 8),
    codigo VARCHAR(32) NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_turma_professor_codigo UNIQUE (professor_id, codigo)
);

CREATE INDEX idx_turmas_professor ON turmas(professor_id);

CREATE TABLE alunos (
    id BIGSERIAL PRIMARY KEY,
    turma_id BIGINT NOT NULL REFERENCES turmas(id),
    nome VARCHAR(120) NOT NULL,
    username VARCHAR(80) NOT NULL UNIQUE,
    pin_hash VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_alunos_turma ON alunos(turma_id);

CREATE TABLE perfis_pedagogicos (
    id BIGSERIAL PRIMARY KEY,
    aluno_id BIGINT NOT NULL UNIQUE REFERENCES alunos(id) ON DELETE CASCADE,
    tamanho_fonte VARCHAR(20) NOT NULL DEFAULT 'PADRAO' CHECK (tamanho_fonte IN ('PADRAO', 'GRANDE', 'EXTRA_GRANDE')),
    leitura_voz BOOLEAN NOT NULL DEFAULT FALSE,
    apoio_visual BOOLEAN NOT NULL DEFAULT FALSE,
    dicas_passo_a_passo BOOLEAN NOT NULL DEFAULT FALSE,
    repeticao_guiada BOOLEAN NOT NULL DEFAULT FALSE,
    questoes_por_tela SMALLINT NOT NULL DEFAULT 1 CHECK (questoes_por_tela BETWEEN 1 AND 10),
    tempo_ampliado BOOLEAN NOT NULL DEFAULT FALSE,
    nivel_apoio VARCHAR(20) NOT NULL DEFAULT 'PADRAO' CHECK (nivel_apoio IN ('PADRAO', 'MODERADO', 'AMPLIADO'))
);

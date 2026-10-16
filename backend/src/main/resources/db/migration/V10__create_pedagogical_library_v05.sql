CREATE TABLE habilidades_bncc (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(24) NOT NULL UNIQUE,
    ano_escolar SMALLINT NOT NULL CHECK (ano_escolar BETWEEN 6 AND 8),
    unidade_tematica VARCHAR(120) NOT NULL,
    objeto_conhecimento VARCHAR(240) NOT NULL,
    descricao TEXT NOT NULL
);
CREATE INDEX idx_habilidades_bncc_ano_unidade ON habilidades_bncc(ano_escolar, unidade_tematica);

CREATE TABLE tags_professor (
    id BIGSERIAL PRIMARY KEY,
    professor_id BIGINT NOT NULL REFERENCES professores(id) ON DELETE CASCADE,
    nome VARCHAR(80) NOT NULL,
    nome_normalizado VARCHAR(80) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tag_professor_normalizada UNIQUE (professor_id, nome_normalizado)
);
CREATE INDEX idx_tags_professor_professor ON tags_professor(professor_id);

CREATE TABLE questoes_modelo (
    id BIGSERIAL PRIMARY KEY,
    professor_id BIGINT REFERENCES professores(id) ON DELETE CASCADE,
    tipo_origem VARCHAR(20) NOT NULL CHECK (tipo_origem IN ('SISTEMA', 'PROFESSOR')),
    enunciado TEXT NOT NULL,
    tipo VARCHAR(30) NOT NULL CHECK (tipo IN ('MULTIPLA_ESCOLHA', 'NUMERICA', 'TEXTO_CURTO')),
    resposta_correta TEXT,
    explicacao TEXT,
    dica_1 TEXT,
    dica_2 TEXT,
    dica_3 TEXT,
    imagem_url TEXT,
    limite_tentativas INTEGER CHECK (limite_tentativas IS NULL OR limite_tentativas >= 1),
    ano_escolar SMALLINT NOT NULL CHECK (ano_escolar BETWEEN 6 AND 8),
    conteudo VARCHAR(60) NOT NULL,
    dificuldade VARCHAR(20) NOT NULL CHECK (dificuldade IN ('BASICA', 'INTERMEDIARIA', 'AVANCADA')),
    origem_modelo_id BIGINT,
    copiado_em TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_questao_modelo_origem_professor
        CHECK ((tipo_origem = 'SISTEMA' AND professor_id IS NULL) OR (tipo_origem = 'PROFESSOR' AND professor_id IS NOT NULL)),
    CONSTRAINT ck_questao_modelo_resposta
        CHECK (tipo = 'MULTIPLA_ESCOLHA' OR resposta_correta IS NOT NULL)
);
ALTER TABLE questoes_modelo
    ADD CONSTRAINT fk_questao_modelo_origem
    FOREIGN KEY (origem_modelo_id) REFERENCES questoes_modelo(id) ON DELETE SET NULL;
CREATE INDEX idx_questoes_modelo_origem_professor ON questoes_modelo(tipo_origem, professor_id);
CREATE INDEX idx_questoes_modelo_ano_conteudo ON questoes_modelo(ano_escolar, conteudo);
CREATE INDEX idx_questoes_modelo_dificuldade ON questoes_modelo(dificuldade);
CREATE INDEX idx_questoes_modelo_origem_modelo ON questoes_modelo(origem_modelo_id);

CREATE TABLE alternativas_modelo (
    id BIGSERIAL PRIMARY KEY,
    questao_modelo_id BIGINT NOT NULL REFERENCES questoes_modelo(id) ON DELETE CASCADE,
    texto TEXT NOT NULL,
    correta BOOLEAN NOT NULL DEFAULT FALSE,
    ordem INTEGER NOT NULL CHECK (ordem > 0),
    CONSTRAINT uk_alternativa_modelo_ordem UNIQUE (questao_modelo_id, ordem)
);
CREATE UNIQUE INDEX uk_alternativa_modelo_correta
    ON alternativas_modelo(questao_modelo_id)
    WHERE correta = TRUE;

CREATE TABLE atividade_habilidade_bncc (
    atividade_id BIGINT NOT NULL REFERENCES atividades(id) ON DELETE CASCADE,
    habilidade_id BIGINT NOT NULL REFERENCES habilidades_bncc(id) ON DELETE RESTRICT,
    PRIMARY KEY (atividade_id, habilidade_id)
);
CREATE INDEX idx_atividade_habilidade_habilidade ON atividade_habilidade_bncc(habilidade_id);

CREATE TABLE atividade_tag (
    atividade_id BIGINT NOT NULL REFERENCES atividades(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tags_professor(id) ON DELETE CASCADE,
    PRIMARY KEY (atividade_id, tag_id)
);
CREATE INDEX idx_atividade_tag_tag ON atividade_tag(tag_id);

CREATE TABLE questao_modelo_habilidade_bncc (
    questao_modelo_id BIGINT NOT NULL REFERENCES questoes_modelo(id) ON DELETE CASCADE,
    habilidade_id BIGINT NOT NULL REFERENCES habilidades_bncc(id) ON DELETE RESTRICT,
    PRIMARY KEY (questao_modelo_id, habilidade_id)
);
CREATE INDEX idx_qm_habilidade_habilidade ON questao_modelo_habilidade_bncc(habilidade_id);

CREATE TABLE questao_modelo_tag (
    questao_modelo_id BIGINT NOT NULL REFERENCES questoes_modelo(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tags_professor(id) ON DELETE CASCADE,
    PRIMARY KEY (questao_modelo_id, tag_id)
);
CREATE INDEX idx_qm_tag_tag ON questao_modelo_tag(tag_id);

ALTER TABLE atividades
    ADD COLUMN origem_atividade_id BIGINT,
    ADD COLUMN copiado_em TIMESTAMPTZ,
    ADD CONSTRAINT fk_atividade_origem
        FOREIGN KEY (origem_atividade_id) REFERENCES atividades(id) ON DELETE SET NULL;
CREATE INDEX idx_atividades_origem_atividade ON atividades(origem_atividade_id);

ALTER TABLE questoes
    ADD COLUMN origem_questao_modelo_id BIGINT,
    ADD COLUMN copiado_em TIMESTAMPTZ,
    ADD CONSTRAINT fk_questao_origem_modelo
        FOREIGN KEY (origem_questao_modelo_id) REFERENCES questoes_modelo(id) ON DELETE SET NULL;
CREATE INDEX idx_questoes_origem_modelo ON questoes(origem_questao_modelo_id);

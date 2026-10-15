CREATE TABLE atividades (
    id BIGSERIAL PRIMARY KEY,
    professor_id BIGINT REFERENCES professores(id),
    titulo VARCHAR(160) NOT NULL,
    descricao TEXT,
    ano_escolar SMALLINT NOT NULL CHECK (ano_escolar BETWEEN 6 AND 8),
    conteudo VARCHAR(60) NOT NULL,
    dificuldade VARCHAR(20) NOT NULL CHECK (dificuldade IN ('BASICA', 'INTERMEDIARIA', 'AVANCADA')),
    tipo_origem VARCHAR(20) NOT NULL CHECK (tipo_origem IN ('SISTEMA', 'PROFESSOR')),
    publicada BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_atividade_origem_professor CHECK ((tipo_origem = 'SISTEMA' AND professor_id IS NULL) OR (tipo_origem = 'PROFESSOR' AND professor_id IS NOT NULL))
);
CREATE INDEX idx_atividades_professor ON atividades(professor_id);
CREATE INDEX idx_atividades_ano_conteudo ON atividades(ano_escolar, conteudo);
CREATE TABLE questoes (
    id BIGSERIAL PRIMARY KEY,
    atividade_id BIGINT NOT NULL REFERENCES atividades(id) ON DELETE CASCADE,
    enunciado TEXT NOT NULL,
    tipo VARCHAR(30) NOT NULL CHECK (tipo IN ('MULTIPLA_ESCOLHA', 'NUMERICA', 'TEXTO_CURTO')),
    resposta_correta TEXT NOT NULL,
    explicacao TEXT,
    dica_1 TEXT,
    dica_2 TEXT,
    dica_3 TEXT,
    imagem_url TEXT,
    ordem INTEGER NOT NULL CHECK (ordem > 0),
    CONSTRAINT uk_questao_atividade_ordem UNIQUE (atividade_id, ordem)
);
CREATE TABLE alternativas (
    id BIGSERIAL PRIMARY KEY,
    questao_id BIGINT NOT NULL REFERENCES questoes(id) ON DELETE CASCADE,
    texto TEXT NOT NULL,
    correta BOOLEAN NOT NULL DEFAULT FALSE,
    ordem INTEGER NOT NULL CHECK (ordem > 0),
    CONSTRAINT uk_alternativa_questao_ordem UNIQUE (questao_id, ordem)
);

ALTER TABLE atribuicoes
    DROP CONSTRAINT atribuicoes_atividade_id_fkey;

ALTER TABLE atribuicoes
    ADD CONSTRAINT fk_atribuicoes_atividade
    FOREIGN KEY (atividade_id)
    REFERENCES atividades(id)
    ON DELETE CASCADE;

-- Biblioteca Oficial MathInclui v0.5
-- Pequena amostra representativa para validar busca, BNCC, tipos de questão,
-- dicas progressivas e reutilização. O catálogo curricular completo fica fora da v0.5.

INSERT INTO atividades (professor_id, titulo, descricao, ano_escolar, conteudo, dificuldade, tipo_origem, publicada, limite_tentativas) VALUES
(NULL, 'Biblioteca v0.5 - Frações equivalentes', 'Reconhecer frações equivalentes em representações numéricas simples.', 6, 'FRACOES', 'BASICA', 'SISTEMA', TRUE, 3),
(NULL, 'Biblioteca v0.5 - Fração de uma quantidade', 'Calcular a fração de uma quantidade natural em situação direta.', 6, 'FRACOES', 'INTERMEDIARIA', 'SISTEMA', TRUE, 3),
(NULL, 'Biblioteca v0.5 - Proporcionalidade direta', 'Resolver situação de proporcionalidade direta entre quantidade e preço.', 7, 'PROPORCIONALIDADE', 'INTERMEDIARIA', 'SISTEMA', TRUE, 3),
(NULL, 'Biblioteca v0.5 - Equação de primeiro grau', 'Resolver uma equação simples do primeiro grau usando propriedades da igualdade.', 7, 'EQUACOES', 'BASICA', 'SISTEMA', TRUE, 3),
(NULL, 'Biblioteca v0.5 - Porcentagens', 'Calcular porcentagem de uma quantidade em contexto numérico.', 8, 'PORCENTAGEM', 'INTERMEDIARIA', 'SISTEMA', TRUE, 3),
(NULL, 'Biblioteca v0.5 - Valor numérico', 'Calcular o valor numérico de uma expressão algébrica a partir de um valor dado.', 8, 'ALGEBRA', 'AVANCADA', 'SISTEMA', TRUE, 3);

INSERT INTO atividade_habilidade_bncc (atividade_id, habilidade_id)
SELECT a.id, h.id FROM atividades a JOIN habilidades_bncc h ON h.codigo = 'EF06MA07'
WHERE a.titulo = 'Biblioteca v0.5 - Frações equivalentes';
INSERT INTO atividade_habilidade_bncc (atividade_id, habilidade_id)
SELECT a.id, h.id FROM atividades a JOIN habilidades_bncc h ON h.codigo = 'EF06MA09'
WHERE a.titulo = 'Biblioteca v0.5 - Fração de uma quantidade';
INSERT INTO atividade_habilidade_bncc (atividade_id, habilidade_id)
SELECT a.id, h.id FROM atividades a JOIN habilidades_bncc h ON h.codigo = 'EF07MA17'
WHERE a.titulo = 'Biblioteca v0.5 - Proporcionalidade direta';
INSERT INTO atividade_habilidade_bncc (atividade_id, habilidade_id)
SELECT a.id, h.id FROM atividades a JOIN habilidades_bncc h ON h.codigo = 'EF07MA18'
WHERE a.titulo = 'Biblioteca v0.5 - Equação de primeiro grau';
INSERT INTO atividade_habilidade_bncc (atividade_id, habilidade_id)
SELECT a.id, h.id FROM atividades a JOIN habilidades_bncc h ON h.codigo = 'EF08MA04'
WHERE a.titulo = 'Biblioteca v0.5 - Porcentagens';
INSERT INTO atividade_habilidade_bncc (atividade_id, habilidade_id)
SELECT a.id, h.id FROM atividades a JOIN habilidades_bncc h ON h.codigo = 'EF08MA06'
WHERE a.titulo = 'Biblioteca v0.5 - Valor numérico';

-- Questões concretas das seis atividades oficiais.
INSERT INTO questoes (atividade_id, enunciado, tipo, resposta_correta, explicacao, dica_1, dica_2, dica_3, ordem, limite_tentativas)
SELECT id,
       'Qual fração é equivalente a 1/2?',
       'MULTIPLA_ESCOLHA', NULL,
       'Duas frações são equivalentes quando representam a mesma parte do inteiro. Multiplicar numerador e denominador de 1/2 por 2 resulta em 2/4.',
       'Pense em multiplicar numerador e denominador pelo mesmo número.',
       'Multiplique 1 e 2 por 2.',
       '1 × 2 = 2 e 2 × 2 = 4.',
       1, 3
FROM atividades WHERE titulo = 'Biblioteca v0.5 - Frações equivalentes';

INSERT INTO alternativas (questao_id, texto, correta, ordem)
SELECT q.id, v.texto, v.correta, v.ordem
FROM questoes q
JOIN atividades a ON a.id = q.atividade_id
CROSS JOIN (VALUES
    ('2/4', TRUE, 1),
    ('2/3', FALSE, 2),
    ('3/5', FALSE, 3)
) AS v(texto, correta, ordem)
WHERE a.titulo = 'Biblioteca v0.5 - Frações equivalentes'
  AND q.enunciado = 'Qual fração é equivalente a 1/2?';

INSERT INTO questoes (atividade_id, enunciado, tipo, resposta_correta, explicacao, dica_1, dica_2, dica_3, ordem, limite_tentativas)
SELECT id,
       'Qual é 1/4 de 20?',
       'NUMERICA', '5',
       'Calcular 1/4 de 20 significa dividir 20 em quatro partes iguais: 20 ÷ 4 = 5.',
       'Pense em dividir a quantidade em quatro partes iguais.',
       'Use a operação 20 ÷ 4.',
       '20 ÷ 4 = 5.',
       1, 3
FROM atividades WHERE titulo = 'Biblioteca v0.5 - Fração de uma quantidade';

INSERT INTO questoes (atividade_id, enunciado, tipo, resposta_correta, explicacao, dica_1, dica_2, dica_3, ordem, limite_tentativas)
SELECT id,
       'Se 3 cadernos custam 18 reais e o preço por caderno é constante, quanto custam 5 cadernos?',
       'NUMERICA', '30',
       'Se 3 cadernos custam 18 reais, cada um custa 6 reais. Logo, 5 cadernos custam 5 × 6 = 30 reais.',
       'Descubra primeiro o preço de um caderno.',
       'Calcule 18 ÷ 3.',
       'Cada caderno custa 6 reais; agora calcule 5 × 6.',
       1, 3
FROM atividades WHERE titulo = 'Biblioteca v0.5 - Proporcionalidade direta';

INSERT INTO questoes (atividade_id, enunciado, tipo, resposta_correta, explicacao, dica_1, dica_2, dica_3, ordem, limite_tentativas)
SELECT id,
       'Na equação x + 5 = 9, qual é o valor de x? Escreva somente o número.',
       'TEXTO_CURTO', '4',
       'Para manter a igualdade, subtraia 5 dos dois lados: x = 9 - 5 = 4.',
       'Pense na operação inversa da adição de 5.',
       'Subtraia 5 dos dois lados da igualdade.',
       '9 - 5 = 4.',
       1, 3
FROM atividades WHERE titulo = 'Biblioteca v0.5 - Equação de primeiro grau';

INSERT INTO questoes (atividade_id, enunciado, tipo, resposta_correta, explicacao, dica_1, dica_2, dica_3, ordem, limite_tentativas)
SELECT id,
       'Quanto é 20% de 150?',
       'NUMERICA', '30',
       '20% corresponde a 20/100. Multiplicando 150 por 0,20 obtemos 30.',
       'Transforme 20% em uma fração de 100 ou em decimal.',
       '20% = 0,20.',
       'Calcule 150 × 0,20.',
       1, 3
FROM atividades WHERE titulo = 'Biblioteca v0.5 - Porcentagens';

INSERT INTO questoes (atividade_id, enunciado, tipo, resposta_correta, explicacao, dica_1, dica_2, dica_3, ordem, limite_tentativas)
SELECT id,
       'Qual é o valor de 3x + 2 quando x = 4? Escreva somente o número.',
       'TEXTO_CURTO', '14',
       'Substitua x por 4: 3 × 4 + 2 = 12 + 2 = 14.',
       'Substitua a letra x pelo valor 4.',
       'A expressão fica 3 × 4 + 2.',
       'Calcule primeiro a multiplicação: 12 + 2.',
       1, 3
FROM atividades WHERE titulo = 'Biblioteca v0.5 - Valor numérico';

-- Banco oficial de questões reutilizáveis. São cópias independentes dos itens concretos.
INSERT INTO questoes_modelo (professor_id, tipo_origem, enunciado, tipo, resposta_correta, explicacao, dica_1, dica_2, dica_3, limite_tentativas, ano_escolar, conteudo, dificuldade) VALUES
(NULL, 'SISTEMA', 'Qual fração é equivalente a 1/2?', 'MULTIPLA_ESCOLHA', NULL,
 'Duas frações são equivalentes quando representam a mesma parte do inteiro. 2/4 é equivalente a 1/2.',
 'Multiplique numerador e denominador pelo mesmo número.', 'Use o número 2.', '1 × 2 = 2 e 2 × 2 = 4.', 3, 6, 'FRACOES', 'BASICA'),
(NULL, 'SISTEMA', 'Qual é 1/4 de 20?', 'NUMERICA', '5',
 '20 ÷ 4 = 5.', 'Divida a quantidade em quatro partes iguais.', 'Use 20 ÷ 4.', 'O resultado é 5.', 3, 6, 'FRACOES', 'INTERMEDIARIA'),
(NULL, 'SISTEMA', 'Se 3 cadernos custam 18 reais, quanto custam 5 cadernos pelo mesmo preço unitário?', 'NUMERICA', '30',
 'Cada caderno custa 6 reais; 5 × 6 = 30.', 'Descubra o preço unitário.', '18 ÷ 3 = 6.', 'Agora faça 5 × 6.', 3, 7, 'PROPORCIONALIDADE', 'INTERMEDIARIA'),
(NULL, 'SISTEMA', 'Na equação x + 5 = 9, qual é o valor de x? Escreva somente o número.', 'TEXTO_CURTO', '4',
 'Subtraindo 5 dos dois lados, x = 4.', 'Use a operação inversa.', 'Subtraia 5 de 9.', '9 - 5 = 4.', 3, 7, 'EQUACOES', 'BASICA'),
(NULL, 'SISTEMA', 'Quanto é 20% de 150?', 'NUMERICA', '30',
 '150 × 0,20 = 30.', 'Transforme a porcentagem.', '20% = 0,20.', 'Multiplique 150 por 0,20.', 3, 8, 'PORCENTAGEM', 'INTERMEDIARIA'),
(NULL, 'SISTEMA', 'Qual é o valor de 3x + 2 quando x = 4? Escreva somente o número.', 'TEXTO_CURTO', '14',
 '3 × 4 + 2 = 14.', 'Substitua x por 4.', 'Calcule 3 × 4.', 'Some 2 ao resultado.', 3, 8, 'ALGEBRA', 'AVANCADA');

INSERT INTO alternativas_modelo (questao_modelo_id, texto, correta, ordem)
SELECT qm.id, v.texto, v.correta, v.ordem
FROM questoes_modelo qm
CROSS JOIN (VALUES
    ('2/4', TRUE, 1),
    ('2/3', FALSE, 2),
    ('3/5', FALSE, 3)
) AS v(texto, correta, ordem)
WHERE qm.tipo_origem = 'SISTEMA'
  AND qm.enunciado = 'Qual fração é equivalente a 1/2?';

INSERT INTO questao_modelo_habilidade_bncc (questao_modelo_id, habilidade_id)
SELECT qm.id, h.id FROM questoes_modelo qm JOIN habilidades_bncc h ON h.codigo='EF06MA07'
WHERE qm.tipo_origem='SISTEMA' AND qm.enunciado='Qual fração é equivalente a 1/2?';
INSERT INTO questao_modelo_habilidade_bncc (questao_modelo_id, habilidade_id)
SELECT qm.id, h.id FROM questoes_modelo qm JOIN habilidades_bncc h ON h.codigo='EF06MA09'
WHERE qm.tipo_origem='SISTEMA' AND qm.enunciado='Qual é 1/4 de 20?';
INSERT INTO questao_modelo_habilidade_bncc (questao_modelo_id, habilidade_id)
SELECT qm.id, h.id FROM questoes_modelo qm JOIN habilidades_bncc h ON h.codigo='EF07MA17'
WHERE qm.tipo_origem='SISTEMA' AND qm.enunciado='Se 3 cadernos custam 18 reais, quanto custam 5 cadernos pelo mesmo preço unitário?';
INSERT INTO questao_modelo_habilidade_bncc (questao_modelo_id, habilidade_id)
SELECT qm.id, h.id FROM questoes_modelo qm JOIN habilidades_bncc h ON h.codigo='EF07MA18'
WHERE qm.tipo_origem='SISTEMA' AND qm.enunciado='Na equação x + 5 = 9, qual é o valor de x? Escreva somente o número.';
INSERT INTO questao_modelo_habilidade_bncc (questao_modelo_id, habilidade_id)
SELECT qm.id, h.id FROM questoes_modelo qm JOIN habilidades_bncc h ON h.codigo='EF08MA04'
WHERE qm.tipo_origem='SISTEMA' AND qm.enunciado='Quanto é 20% de 150?';
INSERT INTO questao_modelo_habilidade_bncc (questao_modelo_id, habilidade_id)
SELECT qm.id, h.id FROM questoes_modelo qm JOIN habilidades_bncc h ON h.codigo='EF08MA06'
WHERE qm.tipo_origem='SISTEMA' AND qm.enunciado='Qual é o valor de 3x + 2 quando x = 4? Escreva somente o número.';

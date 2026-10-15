# MathInclui v0.4 — Atividades, Questões e Progresso

**Data:** 2026-10-15  
**Status:** aprovado em design; aguardando revisão da especificação escrita  
**Branch:** `feature/v0.4-atividades-questoes`

## 1. Objetivo

A v0.4 transforma o MathInclui em um fluxo completo de aprendizagem de Matemática: o professor cria uma atividade, adiciona questões, publica, atribui para turma ou aluno, acompanha a resolução e consulta resultados; o aluno visualiza atividades atribuídas, responde, recebe dicas progressivas, pode tentar novamente antes da revelação da resposta correta e tem seu progresso registrado.

A versão mantém o princípio central do produto: adaptar a experiência com base em necessidades pedagógicas observáveis, sem armazenar laudos ou diagnósticos clínicos.

## 2. Escopo funcional

### 2.1 Professor

O professor poderá:

- criar, listar, visualizar, editar e excluir atividades próprias enquanto não houver histórico de aluno a preservar;
- informar título, descrição, ano escolar (6º, 7º ou 8º), conteúdo e dificuldade;
- definir um limite padrão de tentativas por questão;
- criar questões dos tipos `MULTIPLA_ESCOLHA`, `NUMERICA` e `TEXTO_CURTO`;
- configurar resposta correta, explicação, até três dicas e limite específico de tentativas por questão;
- publicar uma atividade;
- atribuir atividade para uma turma inteira ou para um aluno específico;
- informar data de início e data limite;
- acompanhar participação, acertos, erros, percentual, tempo, tentativas e dicas utilizadas;
- visualizar questões com maior índice de erro;
- visualizar alunos que demandaram mais tentativas ou apoio.

### 2.2 Aluno

O aluno poderá:

- visualizar apenas atividades atribuídas diretamente a ele ou à sua turma;
- distinguir atividades para fazer, em andamento e concluídas;
- iniciar uma atividade;
- retomar uma atividade já iniciada sem perder progresso;
- responder uma questão por vez;
- tentar novamente enquanto houver tentativas disponíveis;
- receber dicas progressivas após erros;
- visualizar explicação e resposta correta quando acertar ou quando atingir o limite de tentativas;
- concluir a atividade e visualizar seu resultado.

Na v0.4 haverá uma única tentativa da atividade por atribuição/aluno. Se existir uma tentativa `EM_ANDAMENTO`, ela será retomada; se estiver `FINALIZADA`, não será criada uma nova tentativa automaticamente.

## 3. Arquitetura

A v0.4 seguirá o modelo de **monólito modular**, preservando a stack atual:

- Java 21;
- Spring Boot;
- Spring Security/JWT;
- Spring Data JPA;
- PostgreSQL 17;
- Flyway;
- React 19 + TypeScript + Vite.

A implementação será organizada por domínio, evitando concentrar todas as responsabilidades em um único serviço. Os módulos principais serão:

- `atividade`;
- `questao`;
- `atribuicao`;
- `tentativa`;
- `resposta`;
- `progresso`/consulta de resultados.

Não serão introduzidos Kafka, microsserviços ou processamento assíncrono nesta versão.

## 4. Modelo de domínio

### 4.1 Atividade

Uma atividade pertence a um professor quando `tipo_origem = PROFESSOR`, ou ao sistema quando `tipo_origem = SISTEMA`.

Campos relevantes já existentes:

- `professor_id`;
- `titulo`;
- `descricao`;
- `ano_escolar`;
- `conteudo`;
- `dificuldade`;
- `tipo_origem`;
- `publicada`;
- `criado_em`.

Novo campo:

- `limite_tentativas` — obrigatório, padrão 3, valor mínimo 1.

### 4.2 Questão

Campos já existentes:

- `atividade_id`;
- `enunciado`;
- `tipo`;
- `resposta_correta`;
- `explicacao`;
- `dica_1`;
- `dica_2`;
- `dica_3`;
- `imagem_url`;
- `ordem`.

Novo campo:

- `limite_tentativas` — opcional. Quando nulo, herda o limite da atividade; quando informado, valor mínimo 1.

Para `MULTIPLA_ESCOLHA`, a fonte de verdade da correção será `alternativas.correta`. A migration da v0.4 deverá permitir que `questoes.resposta_correta` seja nula apenas nesse tipo e continuar exigindo valor para `NUMERICA` e `TEXTO_CURTO`. Também deverá existir garantia de no máximo uma alternativa correta por questão; a regra de exatamente uma alternativa correta será validada no serviço ao publicar a atividade.

### 4.3 Atribuição

A atribuição continuará permitindo exatamente um destino:

- uma turma; ou
- um aluno.

Nunca os dois simultaneamente.

Estados existentes permanecem:

- `RASCUNHO`;
- `ATIVA`;
- `ENCERRADA`;
- `CANCELADA`.

### 4.4 Tentativa

A tentativa representa a execução de uma atividade por um aluno.

Campos já existentes incluem:

- `atribuicao_id`;
- `aluno_id`;
- `iniciada_em`;
- `finalizada_em`;
- `quantidade_acertos`;
- `quantidade_erros`;
- `percentual`;
- `tempo_total_segundos`.

A v0.4 adicionará status explícito:

- `EM_ANDAMENTO`;
- `FINALIZADA`.

Deverá existir no máximo uma tentativa por `atribuicao_id + aluno_id` na v0.4.

### 4.5 Resposta final por questão

A tabela `respostas` continuará representando o estado consolidado de uma questão dentro de uma tentativa, com uma linha por `tentativa_id + questao_id`.

Ela deverá refletir:

- última resposta enviada;
- se a questão foi resolvida corretamente;
- quantidade total de dicas liberadas/usadas;
- tempo acumulado da questão;
- data da última resposta.

### 4.6 Histórico de submissões por questão

Como a v0.4 permite várias tentativas na mesma questão, será criada uma tabela de histórico, denominada `resposta_tentativas`, contendo no mínimo:

- `id`;
- `tentativa_id`;
- `questao_id`;
- `numero_tentativa`;
- `resposta`;
- `correta`;
- `dica_liberada` (0 a 3);
- `tempo_segundos`;
- `respondida_em`.

Deverá existir unicidade em `tentativa_id + questao_id + numero_tentativa`.

A cada envio do aluno, o backend insere uma linha em `resposta_tentativas` e atualiza a linha consolidada de `respostas`.

## 5. Regra de tentativas e dicas

O professor define um limite padrão por atividade. Cada questão pode sobrescrever esse limite.

Exemplo:

- atividade: 3 tentativas por questão;
- questão 1: herda 3;
- questão 2: limite próprio 2;
- questão 3: limite próprio 5.

Fluxo:

1. aluno envia resposta;
2. backend valida;
3. se correta, registra a submissão, marca a questão como resolvida e libera avanço;
4. se incorreta e ainda há tentativas, registra a submissão e libera a próxima dica disponível;
5. se incorreta e o limite foi atingido, registra a submissão, encerra a questão e libera explicação + resposta correta.

As dicas são progressivas e podem existir em até três níveis. A quantidade de tentativas permitida pode ser maior que três; nesse caso, após a terceira dica não há nova dica adicional, apenas novas tentativas até o limite.

Uma questão encerrada, por acerto ou por esgotamento do limite, não aceita novas respostas na v0.4.

## 6. Regras de correção

A correção é feita exclusivamente no backend.

### 6.1 Múltipla escolha

O aluno envia o identificador da alternativa escolhida. O backend valida que a alternativa pertence à questão e consulta `alternativas.correta`.

### 6.2 Numérica

A resposta será convertida para `BigDecimal`. O backend aceitará ponto ou vírgula como separador decimal, rejeitará formatos ambíguos e comparará valores com `BigDecimal.compareTo`, de modo que `2`, `2.0` e `2.00` sejam equivalentes. Tolerância numérica aproximada não faz parte da v0.4.

### 6.3 Texto curto

A comparação será determinística:

- `trim` nas extremidades;
- colapso de espaços consecutivos;
- comparação sem distinção entre maiúsculas e minúsculas;
- normalização Unicode consistente;
- acentos não serão removidos automaticamente.

IA semântica não faz parte do escopo.

## 7. Cálculo do resultado

Cada questão vale uma unidade para fins de acerto/erro.

- `quantidade_acertos`: número de questões resolvidas corretamente dentro do limite;
- `quantidade_erros`: número de questões encerradas sem acerto após atingir o limite;
- respostas erradas intermediárias não aumentam `quantidade_erros`; elas ficam registradas apenas no histórico de submissões;
- `percentual = quantidade_acertos / total_de_questoes * 100`.

O número de tentativas e o uso de dicas serão métricas separadas do percentual.

## 8. Segurança e autorização

A v0.4 preservará a separação de roles já existente:

- `ROLE_PROFESSOR` para APIs administrativas;
- `ROLE_ALUNO` para APIs de resolução.

### 8.1 Regras do professor

Um professor só poderá:

- ler, editar, publicar e atribuir atividades próprias;
- consultar suas próprias turmas e alunos;
- acessar resultados de atribuições vinculadas às suas atividades/turmas/alunos.

Atividades de origem `SISTEMA` poderão ser visualizadas e atribuídas quando publicadas, mas não modificadas como se fossem propriedade do professor.

### 8.2 Regras do aluno

Um aluno só poderá:

- visualizar atribuições destinadas diretamente a ele;
- visualizar atribuições destinadas à turma em que está matriculado;
- iniciar e continuar tentativas próprias;
- responder questões de tentativas próprias.

### 8.3 Não exposição de resposta correta

Antes do acerto ou do esgotamento do limite, a API do aluno não deverá retornar:

- `resposta_correta`;
- marcação da alternativa correta;
- explicação completa que revele a solução.

O frontend nunca será a autoridade da correção.

## 9. API proposta

### 9.1 Professor

- `POST /api/v1/atividades`
- `GET /api/v1/atividades`
- `GET /api/v1/atividades/{id}`
- `PUT /api/v1/atividades/{id}`
- `DELETE /api/v1/atividades/{id}`
- `POST /api/v1/atividades/{id}/questoes`
- `PUT /api/v1/atividades/{id}/questoes/{questaoId}`
- `DELETE /api/v1/atividades/{id}/questoes/{questaoId}`
- `POST /api/v1/atividades/{id}/publicar`
- `POST /api/v1/atribuicoes`
- `GET /api/v1/atribuicoes`
- `GET /api/v1/atribuicoes/{id}/resultados`

### 9.2 Aluno

- `GET /api/v1/aluno/atividades`
- `GET /api/v1/aluno/atividades/{atribuicaoId}`
- `POST /api/v1/aluno/atribuicoes/{id}/iniciar`
- `POST /api/v1/aluno/tentativas/{tentativaId}/questoes/{questaoId}/responder`
- `GET /api/v1/aluno/tentativas/{id}/resultado`

Os contratos finais de request/response serão definidos no plano de implementação mantendo DTOs separados das entidades JPA.

## 10. Publicação e integridade histórica

Uma atividade só poderá ser publicada quando:

- possuir pelo menos uma questão;
- toda questão tiver enunciado e configuração válida;
- questões de múltipla escolha possuírem pelo menos duas alternativas e exatamente uma correta;
- questões numéricas e de texto curto possuírem `resposta_correta`;
- limites de tentativas forem válidos.

Depois que uma atividade publicada possuir qualquer tentativa de aluno, alterações destrutivas não poderão invalidar o histórico.

Regras:

- não excluir questões já utilizadas;
- não alterar resposta correta retroativamente;
- não alterar tipo da questão retroativamente;
- não alterar alternativas corretas retroativamente;
- não reordenar de forma que o histórico se torne inconsistente;
- quando necessário, o professor deverá duplicar a atividade e produzir uma nova versão.

## 11. Experiência do professor

A navegação terá uma nova área principal **Atividades**, composta por:

1. **Minhas Atividades** — listagem, filtros e estados;
2. **Editor** — dados gerais, questões e prévia;
3. **Atribuir** — seleção de turma/aluno e período;
4. **Resultados** — participação e desempenho.

O editor seguirá etapas simples e evitará uma tela única excessivamente longa.

## 12. Experiência do aluno

A área do aluno terá **Minhas Atividades** com agrupamento em:

- Para fazer;
- Em andamento;
- Concluídas.

Durante a resolução:

- uma questão por tela;
- indicador de progresso, por exemplo `Questão 3 de 10`;
- botão de resposta claro;
- dica disponível conforme progresso;
- feedback curto após cada tentativa;
- retomada da tentativa em andamento;
- conclusão com resultado final.

O perfil pedagógico criado na v0.3 deverá influenciar a experiência utilizando as configurações já existentes, como suporte visual, passo a passo, repetição guiada, quantidade de questões por tela e nível de apoio.

A v0.4 não introduzirá cronômetro máximo por questão ou atividade. Por isso, a configuração de tempo adicional do perfil pedagógico não alterará automaticamente `data_limite`; prazos continuam sob controle explícito do professor.

## 13. Dashboard de desempenho

O professor verá pelo menos:

- participação na atividade;
- percentual médio de acertos;
- questões com maior índice de erro;
- alunos que utilizaram mais tentativas;
- alunos que utilizaram mais dicas;
- tempo gasto;
- detalhamento por aluno.

A v0.4 não terá ranking competitivo entre alunos. O foco será evolução individual e identificação de dificuldades.

## 14. Banco de dados e Flyway

As migrations existentes V1–V6 não serão alteradas retroativamente.

A v0.4 adicionará migrations a partir de V7 para suportar:

- `atividades.limite_tentativas` com padrão 3 e check `>= 1`;
- `questoes.limite_tentativas` opcional com check `>= 1`;
- ajuste de `questoes.resposta_correta` para permitir nulo apenas em múltipla escolha;
- índice/constraint para no máximo uma alternativa correta por questão;
- `tentativas.status` com `EM_ANDAMENTO`/`FINALIZADA`;
- unicidade de `tentativas(atribuicao_id, aluno_id)`;
- nova tabela `resposta_tentativas` para histórico de submissões;
- índices necessários para consultas por aluno, atribuição, tentativa e questão.

As migrations não devem depender de dados locais específicos e devem preservar dados existentes.

## 15. Testes

### 15.1 Backend

Testes unitários para:

- correção de múltipla escolha;
- correção numérica normalizada;
- correção de texto curto normalizado;
- herança de limite de tentativas;
- sobrescrita de limite por questão;
- progressão de dicas;
- encerramento da questão ao atingir o limite;
- bloqueio de nova resposta em questão encerrada;
- cálculo de percentual;
- retomada da tentativa;
- autorização por professor/aluno;
- validação de publicação.

Testes de integração para:

- criar atividade → adicionar questões → publicar;
- atribuir para turma;
- atribuir para aluno;
- iniciar tentativa;
- impedir segunda tentativa da atividade para a mesma atribuição/aluno;
- responder incorretamente e receber dica;
- responder corretamente;
- atingir limite e receber explicação;
- concluir atividade;
- consultar resultados;
- preservar histórico de submissões.

Testes de controller/segurança para impedir acesso cruzado entre professores e acesso administrativo por aluno.

### 15.2 Frontend

Validar pelo menos:

- criação e edição de atividade;
- criação de questões;
- publicação e atribuição;
- listagem de atividades do aluno;
- resolução com erro → dica → nova tentativa;
- conclusão e feedback;
- bloqueio visual de rotas administrativas para aluno.

### 15.3 E2E manual

Antes do merge, executar o fluxo completo em Docker local com PostgreSQL, backend e frontend saudáveis.

## 16. Critérios de aceite

A v0.4 será considerada pronta quando o seguinte cenário funcionar de ponta a ponta:

### Professor

1. autentica;
2. cria atividade;
3. adiciona questões;
4. define tentativas e dicas;
5. publica;
6. atribui para turma ou aluno.

### Aluno

1. autentica;
2. visualiza a atividade atribuída;
3. inicia;
4. responde;
5. ao errar, recebe dica e tenta novamente;
6. ao acertar ou atingir o limite, recebe feedback apropriado;
7. conclui a atividade.

### Professor novamente

1. abre resultados;
2. visualiza participação;
3. visualiza acertos/erros;
4. visualiza tentativas;
5. visualiza dicas utilizadas;
6. visualiza tempo e percentual.

Também são critérios obrigatórios:

- aluno não acessa administração;
- professor A não acessa dados do professor B;
- resposta correta não é exposta antes da hora;
- histórico de resultados não é invalidado por edição destrutiva;
- atividade em andamento pode ser retomada;
- build do backend e frontend passa;
- testes automatizados definidos para a v0.4 passam;
- fluxo E2E manual local passa.

## 17. Fora do escopo

Não fazem parte da v0.4:

- correção por IA generativa;
- chat com IA;
- ranking entre alunos;
- gamificação avançada;
- múltiplas tentativas completas da mesma atividade após finalização;
- tolerância numérica aproximada;
- Kafka/event streaming;
- microsserviços;
- armazenamento de laudos ou diagnósticos;
- aplicativo mobile nativo;
- analytics avançado ou BI externo.

## 18. Impacto no roadmap

O design original separava atividades, experiência do aluno e dashboard em versões posteriores. A decisão aprovada para a v0.4 consolida esses fluxos em uma entrega única, porque a atividade só produz valor completo quando o professor consegue atribuí-la, o aluno consegue resolvê-la e o professor consegue acompanhar o resultado.

A implementação deve preservar simplicidade arquitetural e evitar recursos não necessários ao fluxo aprovado.

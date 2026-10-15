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

- criar, listar, visualizar, editar e excluir atividades próprias enquanto ainda não houver histórico que precise ser preservado;
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

Novo campo previsto:

- `limite_tentativas` — obrigatório, com valor padrão 3 e validação positiva.

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

Novo campo previsto:

- `limite_tentativas` — opcional. Quando nulo, herda o limite da atividade.

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

A v0.4 deverá garantir estado suficiente para diferenciar tentativa em andamento de tentativa finalizada. Isso poderá ser representado por campo explícito de status ou derivado de `finalizada_em`, desde que a regra seja inequívoca e testada.

### 4.5 Resposta e tentativas por questão

A estrutura atual de `respostas` registra:

- questão;
- resposta enviada;
- correta/incorreta;
- quantidade de dicas;
- tempo gasto;
- data da resposta.

Como a v0.4 permitirá várias tentativas na mesma questão antes de encerrá-la, a implementação deverá preservar o histórico necessário para saber quantas tentativas ocorreram. A solução preferida é introduzir persistência explícita por tentativa de resposta ou um contador consistente associado à questão dentro da tentativa da atividade. A decisão concreta será detalhada no plano de implementação, sem perder o requisito de auditoria pedagógica.

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
3. se correta, registra acerto, encerra a questão e libera avanço;
4. se incorreta e ainda há tentativas, registra erro e libera a próxima dica disponível;
5. se incorreta e o limite foi atingido, encerra a questão e libera explicação + resposta correta.

As dicas são progressivas e podem existir em até três níveis. A quantidade de tentativas permitida pode ser maior que três; nesse caso, após a terceira dica não há nova dica adicional, apenas novas tentativas até o limite.

## 6. Regras de correção

A correção é feita exclusivamente no backend.

### 6.1 Múltipla escolha

A resposta enviada referencia a alternativa escolhida. O backend verifica se ela é a alternativa marcada como correta.

### 6.2 Numérica

A entrada deve ser normalizada antes da comparação, tratando representação decimal de forma consistente. A normalização deve aceitar formatos usuais como vírgula ou ponto decimal sem alterar o valor matemático.

### 6.3 Texto curto

Na v0.4 será usada comparação determinística simples, com normalização de caixa e espaços. IA semântica não faz parte do escopo.

## 7. Segurança e autorização

A v0.4 preservará a separação de roles já existente:

- `ROLE_PROFESSOR` para APIs administrativas;
- `ROLE_ALUNO` para APIs de resolução.

### 7.1 Regras do professor

Um professor só poderá:

- ler, editar, publicar e atribuir atividades próprias;
- consultar suas próprias turmas e alunos;
- acessar resultados de atribuições vinculadas às suas atividades/turmas/alunos.

Atividades de origem `SISTEMA` poderão ser disponibilizadas para uso, mas não modificadas como se fossem propriedade do professor.

### 7.2 Regras do aluno

Um aluno só poderá:

- visualizar atribuições destinadas diretamente a ele;
- visualizar atribuições destinadas à turma em que está matriculado;
- iniciar e continuar tentativas próprias;
- responder questões de tentativas próprias.

### 7.3 Não exposição de resposta correta

Antes do acerto ou do esgotamento do limite, a API do aluno não deverá retornar `resposta_correta`, marcação da alternativa correta ou explicação completa que revele a solução.

O frontend nunca será a autoridade da correção.

## 8. API proposta

### 8.1 Professor

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

### 8.2 Aluno

- `GET /api/v1/aluno/atividades`
- `GET /api/v1/aluno/atividades/{atribuicaoId}`
- `POST /api/v1/aluno/atribuicoes/{id}/iniciar`
- `POST /api/v1/aluno/tentativas/{tentativaId}/questoes/{questaoId}/responder`
- `GET /api/v1/aluno/tentativas/{id}/resultado`

Os contratos finais de request/response serão definidos no plano de implementação mantendo DTOs separados das entidades JPA.

## 9. Integridade histórica

Depois de uma atividade publicada possuir tentativas de alunos, alterações destrutivas não poderão invalidar o histórico.

Regras:

- não excluir questões já utilizadas em tentativas;
- não alterar resposta correta de forma retroativa em atividade com histórico;
- não reordenar de forma que o resultado histórico se torne inconsistente;
- quando necessário, o professor deverá duplicar a atividade e produzir uma nova versão.

## 10. Experiência do professor

A navegação terá uma nova área principal **Atividades**, composta por:

1. **Minhas Atividades** — listagem, filtros e estados;
2. **Editor** — dados gerais, questões e prévia;
3. **Atribuir** — seleção de turma/aluno e período;
4. **Resultados** — participação e desempenho.

O editor seguirá etapas simples e evitará uma tela única excessivamente longa.

## 11. Experiência do aluno

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

O perfil pedagógico criado na v0.3 deverá influenciar a experiência, utilizando apenas configurações já previstas no produto, como suporte visual, passo a passo, repetição guiada, quantidade de questões por tela, tempo adicional e nível de apoio.

## 12. Dashboard de desempenho

O professor verá pelo menos:

- participação na atividade;
- percentual médio de acertos;
- questões com maior índice de erro;
- alunos que utilizaram mais tentativas;
- alunos que utilizaram mais dicas;
- tempo gasto;
- detalhamento por aluno.

A v0.4 não terá ranking competitivo entre alunos. O foco será evolução individual e identificação de dificuldades.

## 13. Banco de dados e Flyway

As migrations existentes V1–V6 não serão alteradas retroativamente.

A v0.4 deverá adicionar novas migrations a partir de V7 para suportar, no mínimo:

- limite padrão de tentativas na atividade;
- limite opcional por questão;
- persistência consistente do número/histórico de tentativas por questão;
- eventual status explícito da tentativa, se escolhido na implementação;
- índices adicionais necessários aos fluxos de consulta.

Toda migration deve ser reversivelmente compreensível, idempotência não é exigida pelo Flyway, e não deve depender de dados locais específicos.

## 14. Testes

### 14.1 Backend

Testes unitários para:

- correção de múltipla escolha;
- correção numérica normalizada;
- correção de texto curto normalizado;
- herança de limite de tentativas;
- sobrescrita de limite por questão;
- progressão de dicas;
- encerramento da questão ao atingir o limite;
- cálculo de percentual;
- retomada da tentativa;
- autorização por professor/aluno.

Testes de integração para:

- criar atividade → adicionar questões → publicar;
- atribuir para turma;
- atribuir para aluno;
- iniciar tentativa;
- responder incorretamente e receber dica;
- responder corretamente;
- atingir limite e receber explicação;
- concluir atividade;
- consultar resultados.

Testes de controller/segurança para impedir acesso cruzado entre professores e acesso administrativo por aluno.

### 14.2 Frontend

Validar pelo menos:

- criação e edição de atividade;
- criação de questões;
- publicação e atribuição;
- listagem de atividades do aluno;
- resolução com erro → dica → nova tentativa;
- conclusão e feedback;
- bloqueio visual de rotas administrativas para aluno.

### 14.3 E2E manual

Antes do merge, executar o fluxo completo em Docker local com PostgreSQL, backend e frontend saudáveis.

## 15. Critérios de aceite

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

## 16. Fora do escopo

Não fazem parte da v0.4:

- correção por IA generativa;
- chat com IA;
- ranking entre alunos;
- gamificação avançada;
- Kafka/event streaming;
- microsserviços;
- armazenamento de laudos ou diagnósticos;
- aplicativo mobile nativo;
- analytics avançado ou BI externo.

## 17. Impacto no roadmap

O design original separava atividades, experiência do aluno e dashboard em versões posteriores. A decisão aprovada para a v0.4 consolida esses fluxos em uma entrega única, porque a atividade só produz valor completo quando o professor consegue atribuí-la, o aluno consegue resolvê-la e o professor consegue acompanhar o resultado.

A implementação deve preservar simplicidade arquitetural e evitar recursos não necessários ao fluxo aprovado.

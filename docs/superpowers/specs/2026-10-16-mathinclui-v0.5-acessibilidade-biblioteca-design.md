# MathInclui v0.5 — Acessibilidade Adaptativa e Banco Pedagógico

**Data:** 2026-10-16  
**Status:** design aprovado em conversa; aguardando revisão da especificação escrita  
**Branch:** `feature/v0.5-acessibilidade-biblioteca`

## 1. Objetivo

A v0.5 amplia o MathInclui em dois eixos integrados:

1. transformar o perfil pedagógico em adaptações efetivas de interface e comportamento durante a resolução das atividades;
2. criar um banco pedagógico reutilizável com biblioteca oficial MathInclui e biblioteca pessoal do professor.

A versão preserva o princípio do produto: **“Matemática por diferentes caminhos.”** O mesmo objetivo matemático pode ser apresentado com diferentes níveis de apoio sem rotular o estudante e sem armazenar diagnósticos, laudos ou outras informações clínicas.

A v0.5 deve ser compatível com o fluxo completo já implementado na v0.4: atividades, questões, publicação, atribuição, tentativas, dicas progressivas, correção e resultados continuam existindo e não serão reescritos.

## 2. Decisões de escopo aprovadas

### 2.1 Acessibilidade

A v0.5 deverá:

- aplicar adaptações automáticas a partir do perfil pedagógico do aluno;
- permitir adaptações adicionais por atribuição/turma;
- permitir sobrescritas individuais por aluno dentro de uma atribuição;
- permitir ao aluno alterar apenas preferências de interface;
- persistir essas preferências entre sessões;
- impedir que o aluno reduza o mínimo de acessibilidade definido pedagogicamente;
- manter decisões pedagógicas sob controle do professor/sistema.

### 2.2 Banco pedagógico

A v0.5 deverá oferecer:

- Biblioteca Oficial MathInclui;
- Biblioteca Pessoal do Professor;
- reutilização de atividades inteiras;
- reutilização de questões individuais;
- classificação curricular por ano, unidade temática, objeto de conhecimento e habilidade BNCC;
- tags pessoais do professor;
- conteúdo oficial somente leitura;
- cópia independente do conteúdo oficial antes da edição pelo professor.

A biblioteca oficial inicial será propositalmente pequena, porém representativa dos 6º, 7º e 8º anos e dos três tipos de questão já existentes.

## 3. Arquitetura

A v0.5 mantém o **monólito modular** atual em Java 21 + Spring Boot + PostgreSQL + React/TypeScript.

Serão adicionados dois módulos de domínio:

- `acessibilidade`;
- `biblioteca`.

Os módulos existentes permanecem responsáveis por seu fluxo original, especialmente:

- `atividade`;
- `questao`;
- `atribuicao`;
- `tentativa`;
- `resposta`;
- `progresso`;
- `perfilpedagogico`.

Nenhuma regra de acessibilidade deverá ser duplicada em controllers, componentes React ou serviços de atividade. O cálculo ficará centralizado em um serviço próprio.

## 4. Modelo de acessibilidade

### 4.1 Fontes de configuração

A configuração efetiva será derivada de quatro fontes:

1. perfil pedagógico do aluno;
2. adaptação da atribuição;
3. adaptação individual da atribuição para o aluno;
4. preferência pessoal de interface do aluno.

A regra não será “último valor vence”. Para recursos que representam apoio pedagógico, a composição será monotônica: uma camada posterior pode aumentar o apoio, mas não reduzi-lo abaixo do mínimo já estabelecido.

### 4.2 Configuração efetiva

Será introduzido um objeto de domínio, não necessariamente persistido, chamado conceitualmente `ConfiguracaoAcessibilidadeEfetiva`.

Ele deverá expor, no mínimo:

- `tamanhoFonte`;
- `leituraVoz`;
- `apoioVisual`;
- `dicasPassoAPasso`;
- `repeticaoGuiada`;
- `questoesPorTela`;
- `tempoAmpliado`;
- `nivelApoio`.

O backend será a fonte de verdade desse cálculo.

### 4.3 Preferências controladas pelo aluno

O aluno poderá persistir somente:

- tamanho da fonte;
- leitura em voz;
- apoio visual.

O aluno não poderá alterar:

- limite de tentativas;
- política de dicas;
- repetição guiada;
- número pedagógico de questões visíveis;
- tempo pedagógico;
- nível de apoio.

### 4.4 Regra de mínimo pedagógico

A preferência do aluno nunca poderá reduzir o mínimo definido pelas camadas pedagógicas.

Exemplos:

- se a fonte mínima efetiva for `GRANDE`, o aluno poderá escolher `EXTRA_GRANDE`, mas não `NORMAL`;
- se `leituraVoz` for obrigatória, o aluno não poderá desligá-la;
- se `apoioVisual` for obrigatório, o aluno não poderá desligá-lo.

### 4.5 Regras de merge por tipo de campo

#### Booleanos de apoio

Para campos como `leituraVoz`, `apoioVisual`, `dicasPassoAPasso`, `repeticaoGuiada` e `tempoAmpliado`, `true` representa maior apoio. `null` representa herança nas tabelas de override.

O valor efetivo será verdadeiro se qualquer camada pedagógica aplicável exigir o recurso. A preferência do aluno poderá ativar recursos de interface opcionais, mas não desligar os obrigatórios.

#### Tamanho de fonte

A ordem será:

`NORMAL < GRANDE < EXTRA_GRANDE`.

O valor efetivo será o maior nível entre o mínimo pedagógico e a preferência do aluno.

#### Questões por tela

Valores menores representam maior redução de carga visual. O valor efetivo será o menor valor pedagógico aplicável.

A resolução continuará com uma única questão ativa por vez para preservar o fluxo de correção imediata da v0.4. `questoesPorTela` controlará a quantidade de itens simultaneamente apresentados em elementos de navegação, revisão ou visão geral, não múltiplas respostas ativas em paralelo.

#### Nível de apoio

A ordem será:

`BAIXO < MEDIO < ALTO`.

O valor efetivo será o maior nível pedagógico aplicável.

### 4.6 Tempo ampliado

Na v0.5, `tempoAmpliado` continuará booleano. Não será introduzido um novo cronômetro de prova ou cálculo de minutos adicionais. O campo servirá como política de experiência e preparação para evolução futura sem criar um subsistema de temporização nesta versão.

### 4.7 Leitura em voz

A leitura em voz será exposta por ação explícita do aluno, sem autoplay obrigatório. A primeira implementação deverá preferir capacidade nativa do navegador quando disponível, sem dependência de serviço externo de síntese.

Se o navegador não suportar o recurso, a atividade continuará funcional.

### 4.8 Apoio visual

Na v0.5, apoio visual significa adaptações determinísticas de apresentação, incluindo:

- separação clara de blocos;
- mais espaçamento;
- hierarquia tipográfica;
- destaque de informações importantes;
- ícones funcionais;
- redução de informação simultânea.

A v0.5 não gerará imagens automaticamente.

### 4.9 Dicas e repetição guiada

A v0.5 reutilizará as dicas progressivas já existentes nas questões.

Quando a adaptação exigir dicas passo a passo, sua exposição será mais visível e orientada, sem revelar o gabarito antecipadamente.

A repetição guiada seguirá o fluxo:

`erro → feedback curto → dica → passo orientado → nova tentativa`.

Ela não cria uma segunda tentativa global da atividade e não usa IA generativa.

## 5. Persistência de acessibilidade

### 5.1 `preferencias_interface_aluno`

Campos conceituais:

- `aluno_id` — PK/FK;
- `tamanho_fonte`;
- `leitura_voz`;
- `apoio_visual`;
- `atualizado_em`.

### 5.2 `adaptacoes_atribuicao`

Campos conceituais:

- `atribuicao_id` — PK/FK;
- `tamanho_fonte_minima` nullable;
- `leitura_voz_minima` nullable;
- `apoio_visual_minimo` nullable;
- `dicas_passo_a_passo` nullable;
- `repeticao_guiada` nullable;
- `questoes_por_tela` nullable;
- `tempo_ampliado` nullable;
- `nivel_apoio` nullable.

`null` significa herdar.

### 5.3 `adaptacoes_aluno_atribuicao`

Campos conceituais:

- `atribuicao_id`;
- `aluno_id`;
- os mesmos campos opcionais de adaptação;
- constraint única em `atribuicao_id + aluno_id`.

Uma adaptação individual pode aumentar o apoio em relação à adaptação geral, mas não reduzir o mínimo já estabelecido pelo perfil ou pela atribuição.

## 6. Serviço de acessibilidade

Será criado um serviço conceitualmente chamado `AcessibilidadeEfetivaService`.

Responsabilidades:

- carregar perfil pedagógico;
- carregar adaptação da atribuição;
- carregar adaptação individual;
- carregar preferência do aluno;
- validar que a atribuição pertence ao aluno;
- aplicar as regras de precedência;
- devolver apenas a configuração efetiva final.

O frontend não deverá reproduzir o algoritmo de merge.

## 7. APIs de acessibilidade

### 7.1 Professor

Rotas previstas:

- `GET /api/v1/atribuicoes/{id}/adaptacao`;
- `PUT /api/v1/atribuicoes/{id}/adaptacao`;
- `GET /api/v1/atribuicoes/{id}/alunos/{alunoId}/adaptacao`;
- `PUT /api/v1/atribuicoes/{id}/alunos/{alunoId}/adaptacao`;
- `DELETE /api/v1/atribuicoes/{id}/alunos/{alunoId}/adaptacao`.

O professor só poderá operar em atribuições sob seu domínio.

### 7.2 Aluno

Rotas previstas:

- `GET /api/v1/aluno/atribuicoes/{id}/acessibilidade`;
- `GET /api/v1/aluno/preferencias-interface`;
- `PUT /api/v1/aluno/preferencias-interface`.

O aluno não envia um `alunoId` arbitrário para selecionar identidade. O backend obtém a identidade autenticada do JWT.

## 8. Banco pedagógico

### 8.1 Bibliotecas

A interface do professor terá duas origens lógicas:

- Biblioteca Oficial MathInclui;
- Biblioteca Pessoal.

Ambas usarão o mesmo núcleo de domínio, diferenciando propriedade e origem.

### 8.2 Conteúdo oficial

Conteúdo `SISTEMA` será global e somente leitura para professores.

O professor poderá:

- listar;
- filtrar;
- visualizar;
- copiar.

Não poderá:

- editar;
- excluir;
- alterar gabarito;
- modificar metadados oficiais.

Não haverá perfil `ADMIN` nem fluxo colaborativo de publicação na v0.5.

### 8.3 Cópia de atividades

Ao selecionar “Usar atividade”, o backend fará uma cópia profunda e transacional da atividade oficial ou pessoal permitida.

A cópia incluirá:

- metadados da atividade;
- classificações curriculares;
- questões;
- alternativas;
- respostas corretas;
- dicas;
- explicações;
- limites de tentativa;
- ordem das questões.

A nova atividade:

- pertence ao professor autenticado;
- nasce com `publicada = false`;
- é independente da origem;
- pode ser editada antes de publicação;
- mantém rastreabilidade da origem, sem sincronização automática.

Alterações futuras na fonte não modificam cópias já criadas.

### 8.4 Questões reutilizáveis

A v0.5 introduzirá `QuestaoModelo` para representar questões reutilizáveis fora de uma atividade específica.

`Questao` continuará representando a questão concreta pertencente a uma atividade.

O fluxo será:

`QuestaoModelo → cópia profunda → Questao`.

Uma mesma linha de questão concreta nunca será compartilhada por várias atividades.

### 8.5 `QuestaoModelo`

Campos conceituais mínimos:

- `id`;
- `professor_id` nullable;
- `tipo_origem` (`SISTEMA` ou `PROFESSOR`);
- `enunciado`;
- `tipo_questao`;
- `resposta_esperada` quando aplicável;
- `explicacao`;
- `dica_1`;
- `dica_2`;
- `dica_3`;
- `imagem_url` quando aplicável;
- `limite_tentativas`;
- `ano_escolar`;
- `conteudo`;
- `dificuldade`;
- `criado_em`.

Para múltipla escolha haverá `alternativas_modelo`.

### 8.6 Criação da biblioteca pessoal de questões

Uma questão de professor só entra na biblioteca pessoal de questões quando o professor executar explicitamente a ação “Salvar no banco”. Não haverá criação automática de `QuestaoModelo` para toda questão usada em atividades.

Salvar no banco cria uma cópia independente da questão atual. Edições futuras na atividade não alteram o modelo salvo, e alterações no modelo não modificam atividades já existentes.

## 9. BNCC e classificação curricular

### 9.1 Catálogo BNCC

Será criado um catálogo global de habilidades com campos conceituais:

- `id`;
- `codigo`;
- `ano_escolar`;
- `unidade_tematica`;
- `objeto_conhecimento`;
- `descricao`.

Os códigos e descrições do seed oficial deverão vir de fonte curricular validada. Códigos BNCC não serão inventados durante implementação.

### 9.2 Relações

Uma atividade poderá possuir várias habilidades por meio de relação N:N.

Uma `QuestaoModelo` também poderá possuir uma ou mais habilidades.

Essas relações prepararão a v0.6 para relatórios por habilidade sem alterar o motor de tentativas.

## 10. Tags pessoais

Tags pertencem ao professor e são independentes da BNCC.

Exemplos:

- recuperação;
- reforço;
- revisão;
- avaliação diagnóstica.

Estruturas conceituais:

- `tags_professor`;
- `atividade_tag`;
- `questao_modelo_tag`.

Tags de um professor nunca ficam visíveis ou editáveis por outro professor.

A unicidade deverá ser aplicada por professor de forma normalizada para evitar duplicatas equivalentes de nome.

## 11. APIs da biblioteca

Consultas previstas:

- `GET /api/v1/biblioteca/atividades`;
- `GET /api/v1/biblioteca/atividades/{id}`;
- `GET /api/v1/biblioteca/questoes`;
- `GET /api/v1/biblioteca/questoes/{id}`.

Filtros suportados deverão incluir, quando aplicável:

- origem;
- ano escolar;
- unidade temática;
- objeto de conhecimento;
- habilidade BNCC;
- conteúdo;
- dificuldade;
- tipo de questão;
- tags;
- busca textual;
- paginação.

Cópia e importação:

- `POST /api/v1/biblioteca/atividades/{id}/copiar`;
- `POST /api/v1/biblioteca/questoes/{id}/copiar`;
- `POST /api/v1/atividades/{atividadeId}/questoes/importar`.

Para salvar uma questão concreta na biblioteca pessoal, haverá comando explícito de serviço/API a ser nomeado no plano de implementação, preservando a semântica “Salvar no banco”.

PostgreSQL será suficiente para busca na v0.5. Não será introduzido Elasticsearch nem busca semântica.

## 12. UX do professor

### 12.1 Navegação

Nova rota principal:

- `/app/biblioteca`.

A biblioteca terá visões para:

- atividades;
- questões;
- biblioteca pessoal.

### 12.2 Editor existente

O editor de atividades da v0.4 será ampliado, não duplicado.

Ganhará:

- classificação curricular;
- habilidades BNCC;
- tags;
- ação “Buscar na biblioteca” para questões;
- ação explícita “Salvar no banco” para questão pessoal quando aplicável.

### 12.3 Atribuição

O fluxo de atribuição ganhará uma etapa opcional de adaptações.

Por padrão, a atribuição usa os perfis pedagógicos individuais dos alunos.

O professor poderá acrescentar apoio geral à atribuição e configurar exceções individuais.

Essas configurações representam mínimos de apoio e não reduzem o perfil pedagógico.

## 13. UX do aluno

### 13.1 Aplicação automática

Ao abrir uma atividade, o frontend deverá receber a configuração efetiva calculada pelo backend e adaptar a experiência.

### 13.2 Barra de acessibilidade

O aluno poderá controlar:

- aumentar tamanho do texto;
- ligar leitura em voz quando opcional;
- ligar apoio visual quando opcional.

Se um recurso for obrigatório, a interface deverá explicar que ele faz parte do apoio definido, em vez de apresentar apenas um controle desabilitado sem contexto.

### 13.3 Preferências globais

Será disponibilizada uma tela de preferências, conceitualmente em:

- `/app/minhas-preferencias`.

As escolhas persistem para próximos acessos, sempre limitadas pelo mínimo pedagógico efetivo de cada atividade.

### 13.4 Linguagem inclusiva

A interface não exibirá rótulos como:

- “modo deficiência”;
- “aluno especial”;
- “nível alto de dificuldade do aluno”.

Usará linguagem funcional, por exemplo:

- “Ouvir”;
- “Aumentar texto”;
- “Ver dica”;
- “Vamos por partes”;
- “Tentar novamente”.

## 14. Segurança e autorização

A v0.5 preservará JWT e as roles existentes.

### 14.1 Biblioteca

`/api/v1/biblioteca/**` será restrito a `ROLE_PROFESSOR`.

Conteúdo oficial é legível por qualquer professor autenticado.

Conteúdo pessoal exige ownership no service.

### 14.2 Acessibilidade

Rotas em `/api/v1/aluno/**` continuam restritas a `ROLE_ALUNO`.

A autorização de URL não substitui autorização de domínio. Serviços deverão impedir:

- professor A alterar atribuição do professor B;
- professor A ler/editar tags privadas do professor B;
- professor A copiar conteúdo pessoal do professor B sem permissão;
- aluno A ler preferências ou adaptação de aluno B;
- aluno acessar biblioteca administrativa;
- professor editar conteúdo `SISTEMA`.

### 14.3 Gabarito

A área de biblioteca pode expor gabarito para professor autenticado.

O fluxo do aluno mantém a regra da v0.4: respostas corretas e explicações bloqueadas não serão reveladas antes do acerto ou esgotamento do limite.

A API de acessibilidade nunca deverá transportar gabarito.

## 15. Integridade transacional

Cópias de atividade e importações de várias questões serão transacionais.

Se qualquer etapa falhar, nenhuma cópia parcial permanecerá.

Atividades com histórico de aluno continuam sujeitas às proteções destrutivas da v0.4. A reutilização de conteúdo não enfraquece a integridade histórica.

## 16. Tratamento de erros

A API deverá responder de forma consistente:

- `400` para configuração inválida, ano fora da faixa ou payload inválido;
- `403` para acesso a recurso de outro usuário ou role inadequada;
- `404` para recurso inexistente ou inacessível quando apropriado;
- `409` para conflito de estado, duplicidade ou tentativa de alteração destrutiva com histórico.

Detalhes SQL, stack traces e informações internas não serão expostos ao frontend.

## 17. Migrations

As migrations existentes `V1` a `V8` permanecem intocadas.

A v0.5 começará em `V9`.

A decomposição inicial prevista é:

- `V9` — estruturas de acessibilidade;
- `V10` — biblioteca, modelos e metadados curriculares;
- `V11` — catálogo BNCC validado;
- `V12` — conteúdo oficial inicial.

Os nomes exatos podem ser refinados no plano, preservando essa ordem lógica.

As migrations deverão funcionar:

- em banco vazio;
- sobre um banco válido da v0.4;
- com PostgreSQL 17 real via Testcontainers.

## 18. Biblioteca oficial inicial

A primeira carga deverá ser pequena e representativa.

Deverá incluir exemplos dos 6º, 7º e 8º anos, cobrindo:

- mais de uma unidade/conteúdo curricular;
- `MULTIPLA_ESCOLHA`;
- `NUMERICA`;
- `TEXTO_CURTO`;
- diferentes dificuldades;
- dicas progressivas;
- explicações;
- associações BNCC validadas.

O objetivo da v0.5 é provar o modelo técnico e pedagógico, não cobrir todo o currículo.

## 19. Estratégia de testes

A implementação seguirá TDD.

### 19.1 Backend unitário

`AcessibilidadeEfetivaService` deverá provar, no mínimo:

- aluno não reduz fonte mínima;
- aluno pode aumentar fonte;
- leitura obrigatória não pode ser desligada;
- apoio visual obrigatório prevalece;
- `questoesPorTela` escolhe maior apoio;
- `nivelApoio` escolhe maior apoio;
- `null` herda;
- adaptação individual pode aumentar apoio;
- preferência de interface não modifica regra pedagógica.

Serviços da biblioteca deverão provar:

- cópia de atividade oficial;
- propriedade correta da cópia;
- cópia nasce não publicada;
- questões e alternativas são copiadas;
- origem é rastreada;
- edição da cópia não modifica o original;
- importação de questão cria nova questão concreta independente;
- “Salvar no banco” cria `QuestaoModelo` independente.

### 19.2 Integração PostgreSQL

Testcontainers deverá validar:

- migrations completas;
- FKs das preferências;
- unicidade de adaptação individual;
- isolamento de tags;
- relações BNCC;
- cópia transacional;
- constraints de `QuestaoModelo` e alternativas;
- preservação de histórico.

### 19.3 Segurança

Testes deverão provar:

- `ROLE_ALUNO` não acessa biblioteca;
- professor não acessa recursos de outro professor;
- aluno não acessa recursos de outro aluno;
- conteúdo `SISTEMA` não pode ser alterado por professor;
- APIs de aluno não aceitam identidade arbitrária para trocar o sujeito autenticado.

### 19.4 Frontend

Vitest + Testing Library deverão cobrir:

- página da biblioteca e filtros;
- paginação e estados de erro;
- cópia de atividade;
- seleção/importação de múltiplas questões;
- prevenção de dupla submissão no frontend;
- barra de acessibilidade;
- persistência de preferência;
- bloqueio visual de redução abaixo do mínimo;
- aplicação da configuração efetiva na tela do aluno;
- regressão do fluxo de tentativas da v0.4.

## 20. Critério E2E

O cenário de aceite da v0.5 será:

1. professor autentica;
2. abre a biblioteca;
3. filtra conteúdo oficial;
4. visualiza e copia uma atividade;
5. adiciona uma ou mais questões reutilizáveis;
6. edita e publica a cópia;
7. atribui à turma;
8. define apoio adicional geral;
9. configura uma adaptação individual para um aluno;
10. aluno autentica e abre a atividade;
11. backend entrega configuração efetiva;
12. interface se adapta;
13. aluno aumenta um recurso de interface permitido;
14. preferência é persistida;
15. aluno erra, recebe apoio progressivo e conclui;
16. aluno sai e entra novamente e a preferência permanece;
17. conteúdo oficial e modelos de origem permanecem intactos.

## 21. Compatibilidade

Atividades criadas na v0.4 devem continuar funcionando sem BNCC, tags ou overrides da v0.5.

Ausência de configuração nova deverá resultar em defaults seguros e no uso do perfil pedagógico já existente.

A v0.5 não exige migração manual de cada atividade antiga.

## 22. Fora do escopo

Ficam explicitamente fora da v0.5:

- IA generativa;
- geração automática de questões;
- recomendação automática de atividades;
- busca semântica;
- Elasticsearch;
- perfil `ADMIN`;
- workflow de professores submetendo conteúdo oficial;
- sincronização automática de cópias com a origem;
- ranking de alunos;
- armazenamento de diagnóstico clínico;
- relatórios longitudinais completos;
- novo subsistema de cronômetro/prova temporizada.

Relatórios pedagógicos longitudinais e evolução individual permanecem planejados para a v0.6.

## 23. Critérios de aceite da release

A v0.5 só será considerada pronta quando:

- o fluxo da v0.4 continuar funcional;
- professor puder reutilizar atividade inteira;
- professor puder reutilizar questões individuais;
- professor puder salvar questões pessoais no banco de modelos;
- biblioteca oficial permanecer imutável para professor;
- cópias forem independentes da origem;
- filtros curriculares e tags funcionarem;
- dados BNCC do seed forem validados;
- configuração efetiva for calculada no backend;
- aluno puder aumentar acessibilidade, nunca reduzir o mínimo;
- preferências persistirem entre sessões;
- ownership impedir acesso cruzado;
- gabarito não vazar para o aluno;
- migrations passarem em PostgreSQL 17;
- testes backend e frontend passarem;
- verificadores estruturais passarem;
- imagens Docker de backend e frontend construírem com sucesso.

## 24. Princípio final

A v0.5 não cria “uma atividade especial para cada aluno”. Ela mantém um único objetivo pedagógico e calcula a experiência adequada a cada estudante.

O banco pedagógico reduz retrabalho do professor; a acessibilidade adaptativa reduz barreiras do aluno. Os dois eixos se encontram no mesmo fluxo:

`Biblioteca → criação/reutilização → atribuição → política de acessibilidade → experiência adaptada`.

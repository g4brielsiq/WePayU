# WePayU - Sistema de Folha de Pagamento

Projeto prático desenvolvido para a disciplina **Programação 2 (COMP372)** do curso de Ciência da Computação da **Universidade Federal de Alagoas (UFAL)**, ministrada pelo professor Mário Hozano.

---

## Contexto e Objetivos da Disciplina

A disciplina de Programação 2 tem como foco os fundamentos de **Programação Orientada a Objetos (POO)** em Java, abordando temas como qualidade de software, testes de aceitação, boas práticas de modelagem e padrões de projeto.

A proposta deste projeto é praticar construindo, de forma incremental, a lógica de negócio de um sistema de folha de pagamento. O projeto implementa as User stories US1 a US8, incluindo cadastro de empregados, cartões de ponto, vendas, sindicato, processamento de pagamentos e undo/redo.

Os testes de aceitação são executados pelo EasyAccept e acessam o sistema por meio da classe `br.ufal.ic.p2.wepayu.Facade`. Por esse motivo, a `Facade` e os nomes/assinaturas de seus métodos e comandos são mantidos como ponto de entrada para os testes.

O objetivo principal foi aplicar os pilares teóricos discutidos em sala de aula na solução de um problema com regras de negócio densas e dependências temporais:

1. **Abstração e Modelagem do Mundo Real:** Representação das diferentes modalidades de contratação de uma empresa em classes e métodos com responsabilidades bem definidas.
2. **Encapsulamento e Estado Consistente:** Proteção dos atributos internos das entidades, garantindo que mutações e cálculos obedeçam estritamente às regras do domínio.
3. **Delegação e Coesão:** Transferência de lógica de cálculo para quem é dono dos dados (por exemplo, cada tipo de empregado sabe como seu contracheque é composto), evitando classes inchadas ou métodos que concentrem regras alheias.
4. **Composição vs. Herança:** Utilização criteriosa de herança para relações do tipo *"é-um"* e de composição para coleções dinâmicas de dados associados.
5. **Tratamento de Exceções:** Sinalização e propagação estruturada de violações de negócio (datas inválidas, valores negativos, entidades não localizadas).
6. **Desenvolvimento Guiado por Testes de Aceitação:** Validação funcional rigorosa e contínua por meio do framework **EasyAccept**, cobrindo os cenários das histórias de usuário US1 a US8.

---

## Escopo Implementado (US1 a US8)

O sistema cobre o ciclo de vida completo de administração e remuneração de pessoas:

- **US1 & US1.1 — Gestão de Empregados:** Criação, recuperação de atributos e remoção de empregados dos tipos horista, assalariado e comissionado.
- **US2 & US2.1 — Cartões de Ponto:** Registro diário de horas trabalhadas para empregados horistas, com segregação entre horas normais (até 8h) e horas extras com adicional de 50%.
- **US3 & US3.1 — Vendas e Comissões:** Lançamento de resultados de vendas para comissionados, com cálculo proporcional sobre as vendas apuradas.
- **US4 & US4.1 — Vínculo Sindical e Taxas de Serviço:** Associação de empregados a sindicatos, retenção de taxa sindical diária e lançamento de taxas de serviço específicas.
- **US5 & US5.1 — Alteração de Dados Cadastrais:** Modificação dinâmica de métodos de pagamento (em mãos, correios ou depósito bancário), status sindical e categoria contratual.
- **US6 & US6.1 — Prévia da Folha:** Apuração prévia dos valores brutos a serem desembolsados em uma data específica (`totalFolha`).
- **US7 — Processamento e Emissão da Folha:** Verificação do calendário de pagamento de cada modalidade (sextas-feiras para horistas, quinzenal para comissionados e último dia útil do mês para assalariados), consolidação de descontos sindicais retroativos por período trabalhado, cálculo de valores líquidos e gravação do relatório tabular padronizado.
- **US8 — Mecanismo de Desfazer e Refazer (Undo/Redo):** Rastreamento linear de comandos mutantes por meio de *snapshots*, permitindo reverter e reaplicar operações de cadastro, lançamentos contábeis, fechamentos de folha e resets de sistema.

---

## Arquitetura e Organização do Projeto

Seguindo a recomendação de modularização trabalhada na disciplina, o código foi organizado em pacotes coesos:

```text
src/
├── Main.java
└── br/ufal/ic/p2/wepayu/
    ├── Facade.java
    ├── entidades/
    │   ├── Empregado.java                 (Superclasse abstrata)
    │   ├── EmpregadoHorista.java
    │   ├── EmpregadoAssalariado.java
    │   ├── EmpregadoComissionado.java
    │   ├── CartaoDePonto.java
    │   ├── ResultadoVenda.java
    │   └── TaxaServico.java
    ├── repositorio/
    │   ├── RepositoryEmpregados.java      (Empregados)
    │   ├── RepositoryCartoesDePonto.java  (Cartões)
    │   ├── RepositoryVendas.java           (Vendas)
    │   ├── RepositoryTaxasServico.java     (Taxas sindicais extras)
    │   └── RepositoryPagamentos.java       (Datas de processamento)
    ├── servicos/
    │   ├── ServicoEmpregados.java         (Regras de validação e cadastros)
    │   └── ProcessadorFolhaPagamento.java (Cálculos de proventos, descontos e datas)
    └── excecoes/
        └── EmpregadoNaoExisteException.java

### `Facade`

É a porta de entrada exigida pelo EasyAccept. Traduz os comandos de teste para chamadas do sistema e mantém o histórico de undo/redo. Ela não concentra as regras de cadastro nem os cálculos da folha: encaminha cada operação para o serviço correspondente.

### `entidades`

Contém `Empregado` e os tipos `EmpregadoHorista`, `EmpregadoAssalariado` e `EmpregadoComissionado`, além de `CartaoDePonto`, `ResultadoVenda` e `TaxaServico`. As entidades guardam seus próprios dados cadastrais e usam os repositórios adequados para suas coleções associadas: o empregado sindicalizado mantém um repositório de taxas, o horista mantém um de cartões, e o comissionado mantém um de vendas. Cada subtipo sabe produzir uma cópia dos seus dados e dessas coleções, usada pelo histórico.

### `repositorio`

Os repositórios mantêm coleções com `ArrayList` e oferecem operações de inclusão, consulta e cópia. `RepositoryEmpregados` cuida do cadastro e da busca de empregados. `RepositoryCartoesDePonto`, `RepositoryVendas` e `RepositoryTaxasServico` guardam os lançamentos associados aos empregados e somam valores por período. `RepositoryPagamentos` guarda a última data de pagamento e de processamento de cada empregado. A escolha de listas mantém a implementação didática: as buscas percorrem os elementos em sequência, sem depender de `Map`/`HashMap`.

### `servicos`

`ServicoEmpregados` concentra as regras de negócio de cadastro e consulta, como lançar cartões, vendas e taxas de serviço. A `Facade` faz verificações iniciais de campos obrigatórios, por estar na fronteira dos comandos, e encaminha as demais regras ao serviço. `ProcessadorFolhaPagamento` concentra as regras de calendário, cálculo e geração da folha; seu estado auxiliar fica em `RepositoryPagamentos`.

### `excecoes`

Reúne exceções específicas do domínio, mantendo-as separadas das entidades e dos serviços.

```

## Decisões de projeto

A organização foi mantida intencionalmente simples: pacotes representam responsabilidades reconhecíveis no conteúdo da disciplina, sem introduzir camadas ou padrões que não tragam benefício para este escopo. O histórico guarda cópias do repositório e do estado do processador da folha; por isso undo/redo restaura tanto os dados dos empregados quanto o controle dos pagamentos já processados.

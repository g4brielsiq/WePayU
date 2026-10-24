# WePayU - Sistema de Folha de Pagamento

Projeto prático desenvolvido para a disciplina **Programação 2 (COMP372)** do curso de Ciência da Computação da **Universidade Federal de Alagoas (UFAL)**, ministrada pelo professor Mário Hozano.

---

## Contexto e Objetivos da Disciplina

A disciplina de Programação 2 tem como foco os fundamentos de **Programação Orientada a Objetos (POO)** em Java, abordando temas como qualidade de software, testes de aceitação, boas práticas de modelagem, encapsulamento, polimorfismo e padrões arquiteturais fundamentais.

A proposta deste projeto é praticar construindo, de forma incremental, a lógica de negócio completa de um sistema de folha de pagamento. O projeto implementa integralmente as User Stories **US1 a US10_1**, incluindo cadastro e gestão de empregados, cartões de ponto, vendas, taxas sindicais, prévia e emissão da folha de pagamento, mecanismo de desfazer e refazer (undo/redo), criação de agendas personalizadas e persistência de dados em arquivos XML.

Os testes de aceitação são executados pelo **EasyAccept** e acessam o sistema por meio da classe `br.ufal.ic.p2.wepayu.Facade`. Por esse motivo, a `Facade` e as assinaturas de seus métodos são estritamente mantidas como ponto de entrada para os testes automatizados.

O objetivo principal foi aplicar os pilares teóricos discutidos em sala de aula na solução de um problema com regras de negócio densas e dependências temporais:

1. **Abstração e Modelagem do Mundo Real:** Representação das diferentes modalidades de contratação de uma empresa em classes e métodos com responsabilidades bem definidas.
2. **Encapsulamento e Estado Consistente:** Proteção dos atributos internos das entidades, garantindo que mutações e cálculos obedeçam estritamente às regras do domínio.
3. **Polimorfismo e Delegação Coesa:** Transferência da lógica de cálculo para quem detém os dados (cada subtipo de empregado calcula seu próprio salário bruto e avalia seus dias de pagamento), eliminando cadeias excessivas de verificações de tipo (`instanceof`) e concentradores de regras alheias.
4. **Composição vs. Herança:** Utilização criteriosa de herança para relações autênticas do tipo *"é-um"* (`EmpregadoHorista`, `EmpregadoAssalariado` e `EmpregadoComissionado`) e de composição para coleções dinâmicas de lançamentos contábeis.
5. **Hierarquia de Exceções Customizadas:** Sinalização e propagação estruturada de violações de negócio por meio de tipos específicos derivados de uma exceção base comum (`WePayUException`), preservando com precisão as mensagens exigidas pelo EasyAccept.
6. **Persistência Declarativa em XML:** Serialização e restauração do estado integral do sistema com `java.beans.XMLEncoder` e `java.beans.XMLDecoder`, seguindo a convenção de JavaBeans sem dependências de frameworks externos ou bancos de dados SQL.
7. **Desenvolvimento Guiado por Testes de Aceitação:** Validação funcional rigorosa e contínua cobrindo 100% dos cenários das histórias de usuário US1 a US10_1.

---

## Escopo Implementado (US1 a US10_1)

O sistema cobre o ciclo de vida completo de administração, remuneração, auditoria e persistência:

- **US1 & US1.1 - Gestão de Empregados:** Criação, recuperação de atributos, busca por nome/índice e remoção de empregados dos tipos horista, assalariado e comissionado.
- **US2 & US2.1 - Cartões de Ponto:** Registro diário de horas trabalhadas para horistas, com segregação entre horas normais (até 8h) e horas extras com adicional de 50%.
- **US3 & US3.1 - Vendas e Comissões:** Lançamento de ordens de vendas para comissionados, com apuração proporcional e acumulada de comissões por período.
- **US4 & US4.1 - Vínculo Sindical e Taxas de Serviço:** Associação de empregados a sindicatos, retenção diária de taxa sindical por período e lançamento de taxas de serviço avulsas.
- **US5 & US5.1 - Alteração de Dados Cadastrais:** Modificação dinâmica de métodos de pagamento (em mãos, correios ou depósito bancário com banco, agência e conta corrente), status sindical, categoria contratual e agenda de pagamento.
- **US6 & US6.1 - Prévia da Folha:** Apuração prévia e consolidada dos valores brutos a serem desembolsados em uma data específica (`totalFolha`).
- **US7 - Processamento e Emissão da Folha:** Verificação do calendário de pagamento de cada modalidade, consolidação retroativa de descontos sindicais e taxas de serviço por período efetivamente trabalhado, apuração de salários líquidos e geração do relatório tabular padronizado (`rodaFolha`).
- **US8 - Mecanismo de Desfazer e Refazer (Undo/Redo):** Rastreamento linear de comandos mutantes por meio de *snapshots* do repositório de empregados, histórico de pagamentos e agendas disponíveis, permitindo reverter e reaplicar operações em qualquer profundidade.
- **US9 & US9.1 - Criação e Gestão de Agendas de Pagamento:** Validação rigorosa e inclusão de novas agendas personalizadas semanais (`semanal <dia>`, `semanal <k> <dia>`) e mensais (`mensal <dia>`, `mensal $`), permitindo sua associação a qualquer funcionário.
- **US10 & US10.1 - Persistência de Dados em XML:** Persistência do estado global do sistema no encerramento (`encerrarSistema`) gravando em `wepayu_dados.xml`, com restauração automática e transparente na reinicialização da aplicação.

---

## Arquitetura e Organização do Projeto

Seguindo as diretrizes de modularização trabalhadas na disciplina, o código foi organizado em pacotes coesos:

```text
src/
├── Main.java
└── br/ufal/ic/p2/wepayu/
    ├── Facade.java
    ├── entidades/
    │   ├── Empregado.java                 (Superclasse abstrata base)
    │   ├── EmpregadoHorista.java          (Cálculo de horas normais/extras)
    │   ├── EmpregadoAssalariado.java      (Cálculo de salário fixo e proporcional)
    │   ├── EmpregadoComissionado.java     (Cálculo de comissões e base fixa)
    │   ├── CartaoDePonto.java
    │   ├── ResultadoVenda.java
    │   └── TaxaServico.java
    ├── repositorio/
    │   ├── RepositoryEmpregados.java      (Cadastro e busca de empregados)
    │   ├── RepositoryCartoesDePonto.java  (Histórico de cartões de ponto)
    │   ├── RepositoryVendas.java           (Histórico de vendas)
    │   ├── RepositoryTaxasServico.java     (Histórico de taxas sindicais avulsas)
    │   ├── RepositoryPagamentos.java       (Datas de processamento e histórico)
    │   └── RepositoryAgendas.java          (Catálogo e validação de agendas)
    ├── servicos/
    │   ├── ServicoEmpregados.java         (Regras de validação e cadastros)
    │   └── ProcessadorFolhaPagamento.java (Coordenação e emissão da folha)
    ├── persistencia/
    │   ├── DadosAplicacao.java            (Contêiner JavaBeans para serialização)
    │   └── PersistenciaXML.java           (Gravador/leitor XMLEncoder e XMLDecoder)
    ├── util/
    │   └── FormatoUtil.java               (Parsing estrito, formatações e precisão numérica)
    └── excecoes/
        ├── WePayUException.java           (Classe base da hierarquia)
        ├── AtributoNaoExisteException.java
        ├── EmpregadoNaoExisteException.java
        ├── FolhaException.java
        ├── IdentificacaoNulaException.java
        └── ValorInvalidoException.java
```

## Componentes Principais

### `Facade`
É a porta de entrada única exigida pelo EasyAccept. Valida parâmetros iniciais de comando, delega as chamadas de negócio para os serviços correspondentes, gerencia as pilhas de *undo* e *redo* e orquestra o encerramento e a recuperação do estado persistido em XML.

### `entidades`
Representa o modelo de domínio do sistema. A superclasse abstrata `Empregado` define contratos polimórficos como `getSalario()`, `setSalario(double)`, `calcularSalarioBruto(...)`, `isDiaDePagamento(...)` e `getDescricaoMetodoPagamento()`. Suas subclasses concretas (`EmpregadoHorista`, `EmpregadoAssalariado` e `EmpregadoComissionado`) implementam suas regras financeiras de forma autônoma e encapsulam suas coleções dinâmicas de cartões, vendas e taxas sindicais.

### `repositorio`
Gerencia coleções em memória usando `ArrayList` e métodos expressivos de consulta, adição, filtragem temporal e clonagem profunda (`copia()`). A escolha de coleções sequenciais mantém a didática do projeto clara, simples e livre de dependências complexas.

### `servicos`
- `ServicoEmpregados`: Concentra a orquestração do ciclo de vida dos empregados, validação cadastral, lançamentos de cartões, vendas, taxas e mutações de atributos.
- `ProcessadorFolhaPagamento`: Coordena a apuração prévia e a emissão tabular da folha de pagamento, delegando o cálculo bruto a cada entidade, consolidando descontos e registrando as datas de processamento.

### `persistencia`
Implementa o mecanismo de persistência em arquivo XML local (`wepayu_dados.xml`) através de `java.beans.XMLEncoder` e `java.beans.XMLDecoder`. Utiliza `PersistenceDelegate` para a serialização segura e canônica de tipos temporais modernos da API `java.time.LocalDate` e segue as convenções de JavaBeans (construtores padrão e pares de *getters*/*setters*).

### `util`
`FormatoUtil` centraliza operações transversais de parsing estrito de datas (`d/M/uuuu`), arredondamento monetário (`BigDecimal` com `RoundingMode.HALF_UP`), truncamento de comissões/salários fracionados (`RoundingMode.DOWN`) e formatação monetária e de horas.

### `excecoes`
Contém a árvore de exceções customizadas do sistema encabeçada por `WePayUException`, assegurando que 100% dos erros de validação e de negócio sejam sinalizados com tipos semânticos precisos e compatíveis com os scripts do EasyAccept.
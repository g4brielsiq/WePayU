# WePayU - Sistema de Folha de Pagamento

Projeto acadêmico desenvolvido para a disciplina de Programação 2 da Universidade Federal de Alagoas (UFAL). A proposta é praticar programação orientada a objetos construindo, de forma incremental, a lógica de negócio de um sistema de folha de pagamento. O projeto implementa as histórias de usuário US1 a US8, incluindo cadastro de empregados, cartões de ponto, vendas, sindicato, processamento de pagamentos e undo/redo.

Os testes de aceitação são executados pelo EasyAccept e acessam o sistema por meio da classe `br.ufal.ic.p2.wepayu.Facade`. Por esse motivo, a `Facade` e os nomes/assinaturas de seus comandos públicos são mantidos como ponto de entrada para os testes.

## Como executar

1. Importe esta pasta como projeto Java existente no Eclipse.
2. Confirme que `src` é uma pasta de código-fonte e que `lib/easyaccept.jar` está no Build Path.
3. Execute `Main.java`. A classe executa os arquivos de teste das US1 a US8.

## Organização e responsabilidades

```text
src/
├── Main.java
└── br/ufal/ic/p2/wepayu/
    ├── Facade.java
    ├── entidades/
    ├── excecoes/
    ├── repositorio/
    └── servicos/
        ├── ProcessadorFolhaPagamento.java
        └── ServicoEmpregados.java
```

### `Facade`

É a porta de entrada exigida pelo EasyAccept. Traduz os comandos de teste para chamadas do sistema e mantém o histórico de undo/redo. Ela não concentra as regras de cadastro nem os cálculos da folha: encaminha cada operação para o serviço correspondente.

### `entidades`

Contém `Empregado` e os tipos `EmpregadoHorista`, `EmpregadoAssalariado` e `EmpregadoComissionado`, além de `CartaoDePonto`, `ResultadoVenda` e `TaxaServico`. As entidades guardam seus próprios dados e oferecem operações simples para alterá-los. Cada subtipo de empregado sabe produzir uma cópia dos seus dados, usada pelo histórico.

### `repositorio`

`RepositoryEmpregados` é responsável pela coleção de empregados: gera identificadores, localiza por ID/nome/sindicato, adiciona e remove elementos e cria uma cópia do conjunto. Assim, a `Facade` não precisa manipular diretamente a estrutura da coleção.

### `servicos`

`ServicoEmpregados` concentra validações e operações de cadastro e consulta relacionadas aos empregados, como lançar cartões, vendas e taxas de serviço. `ProcessadorFolhaPagamento` concentra as regras de calendário, cálculo e geração da folha. A divisão evita misturar essas regras com o adaptador dos testes.

### `excecoes`

Reúne exceções específicas do domínio, mantendo-as separadas das entidades e dos serviços.

## Decisões de projeto

A organização foi mantida intencionalmente simples: pacotes representam responsabilidades reconhecíveis no conteúdo da disciplina, sem introduzir camadas ou padrões que não tragam benefício para este escopo. O histórico guarda cópias do repositório e do estado do processador da folha; por isso undo/redo restaura tanto os dados dos empregados quanto o controle dos pagamentos já processados.

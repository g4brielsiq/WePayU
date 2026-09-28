# Organização do WePayU até a US8

A `Facade` permanece como ponto de entrada porque o EasyAccept procura nela os comandos descritos nos testes. Ela valida e encaminha as chamadas; não precisa ser a dona de todos os cálculos do sistema.

## Responsabilidades principais

- `Facade`: adapta os nomes e parâmetros dos comandos EasyAccept e mantém o histórico de `undo` e `redo`.
- `RepositoryEmpregados`: mantém a coleção, gera identificadores, localiza e remove empregados e cria uma cópia do conjunto.
- `Empregado` e seus subtipos: guardam os dados do empregado. Cada subtipo sabe copiar seus próprios dados específicos; a classe base copia os dados compartilhados.
- `ProcessadorFolhaPagamento`: cuida das datas de pagamento, dos cálculos da folha, da geração do arquivo e do estado de processamento dos pagamentos.

O histórico guarda cópias do repositório e do processador da folha. Assim, desfazer e refazer também restaura os dados específicos desses objetos.

## Ideia de POO usada

Cada classe fica responsável pelos dados que possui e pelas operações diretamente relacionadas a eles. A `Facade` continua tendo vários métodos públicos por exigência dos testes, mas agora encaminha o processamento da folha e pede ao repositório para copiar, contar e remover empregados.

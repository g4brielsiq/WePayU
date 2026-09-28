package br.ufal.ic.p2.wepayu;

import java.util.Stack;

import br.ufal.ic.p2.wepayu.repositorio.RepositoryEmpregados;
import br.ufal.ic.p2.wepayu.servicos.ProcessadorFolhaPagamento;
import br.ufal.ic.p2.wepayu.servicos.ServicoEmpregados;

public class Facade {
	private static class Snapshot {
		private final RepositoryEmpregados empregados;
		private final ProcessadorFolhaPagamento folha;

		Snapshot(RepositoryEmpregados empregados, ProcessadorFolhaPagamento folha) {
			this.empregados = empregados;
			this.folha = new ProcessadorFolhaPagamento(folha);
		}
	}

	private final Stack<Snapshot> historicoUndo = new Stack<>();
	private final Stack<Snapshot> historicoRedo = new Stack<>();
	private ProcessadorFolhaPagamento folhaPagamento = new ProcessadorFolhaPagamento();
	private boolean sistemaEncerrado = false;
	private RepositoryEmpregados listaEmpregados = new RepositoryEmpregados();

	public void zerarSistema() {
		salvarEstado();
		listaEmpregados.zerarSistema();
		folhaPagamento.zerar();
		sistemaEncerrado = false;
	}

	public void encerrarSistema() {
		sistemaEncerrado = true;
	}

	private void salvarEstado() {
		historicoUndo.push(new Snapshot(listaEmpregados.copia(), folhaPagamento));
		historicoRedo.clear();
	}

	public void undo() throws Exception {
		if (sistemaEncerrado) throw new Exception("Nao pode dar comandos depois de encerrarSistema.");
		if (historicoUndo.isEmpty()) throw new Exception("Nao ha comando a desfazer.");
		historicoRedo.push(new Snapshot(listaEmpregados.copia(), folhaPagamento));
		Snapshot anterior = historicoUndo.pop();
		listaEmpregados = anterior.empregados.copia();
		folhaPagamento = new ProcessadorFolhaPagamento(anterior.folha);
	}

	public void redo() throws Exception {
		if (sistemaEncerrado) throw new Exception("Nao pode dar comandos depois de encerrarSistema.");
		if (historicoRedo.isEmpty()) throw new Exception("Nao ha comando a refazer.");
		historicoUndo.push(new Snapshot(listaEmpregados.copia(), folhaPagamento));
		Snapshot seguinte = historicoRedo.pop();
		listaEmpregados = seguinte.empregados.copia();
		folhaPagamento = new ProcessadorFolhaPagamento(seguinte.folha);
	}

	private ServicoEmpregados servicoEmpregados() {
		return new ServicoEmpregados(listaEmpregados, this::salvarEstado);
	}

	public String getNumeroDeEmpregados() {
		return servicoEmpregados().getNumeroDeEmpregados();
	}

	public String getAtributoEmpregado(String id, String atributo) throws Exception {
		validarObrigatorio(id, "Identificacao do empregado nao pode ser nula.");
		return servicoEmpregados().getAtributoEmpregado(id, atributo);
	}

	public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
		validarObrigatorio(nome, "Nome nao pode ser nulo.");
		validarObrigatorio(endereco, "Endereco nao pode ser nulo.");
		validarObrigatorio(tipo, "Tipo nao pode ser nulo.");
		validarObrigatorio(salario, "Salario nao pode ser nulo.");
		return servicoEmpregados().criarEmpregado(nome, endereco, tipo, salario);
	}

	public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws Exception {
		validarObrigatorio(nome, "Nome nao pode ser nulo.");
		validarObrigatorio(endereco, "Endereco nao pode ser nulo.");
		validarObrigatorio(tipo, "Tipo nao pode ser nulo.");
		validarObrigatorio(salario, "Salario nao pode ser nulo.");
		validarObrigatorio(comissao, "Comissao nao pode ser nula.");
		return servicoEmpregados().criarEmpregado(nome, endereco, tipo, salario, comissao);
	}

	public String getEmpregadoPorNome(String nome, int indice) throws Exception {
		return servicoEmpregados().getEmpregadoPorNome(nome, indice);
	}

	public void removerEmpregado(String id) throws Exception {
		validarObrigatorio(id, "Identificacao do empregado nao pode ser nula.");
		servicoEmpregados().removerEmpregado(id);
	}

	public void lancaCartao(String id, String data, String horas) throws Exception {
		validarObrigatorio(id, "Identificacao do empregado nao pode ser nula.");
		servicoEmpregados().lancaCartao(id, data, horas);
	}

	public String getHorasNormaisTrabalhadas(String id, String inicio, String fim) throws Exception {
		return servicoEmpregados().getHorasNormaisTrabalhadas(id, inicio, fim);
	}

	public String getHorasExtrasTrabalhadas(String id, String inicio, String fim) throws Exception {
		return servicoEmpregados().getHorasExtrasTrabalhadas(id, inicio, fim);
	}

	public void lancaVenda(String id, String data, String valor) throws Exception {
		validarObrigatorio(id, "Identificacao do empregado nao pode ser nula.");
		servicoEmpregados().lancaVenda(id, data, valor);
	}

	public String getVendasRealizadas(String id, String inicio, String fim) throws Exception {
		return servicoEmpregados().getVendasRealizadas(id, inicio, fim);
	}

	public void lancaTaxaServico(String idSindicato, String data, String valor) throws Exception {
		validarObrigatorio(idSindicato, "Identificacao do membro nao pode ser nula.");
		servicoEmpregados().lancaTaxaServico(idSindicato, data, valor);
	}

	public String getTaxasServico(String id, String inicio, String fim) throws Exception {
		return servicoEmpregados().getTaxasServico(id, inicio, fim);
	}

	public void alteraEmpregado(String id, String atributo, String valor) throws Exception {
		servicoEmpregados().alteraEmpregado(id, atributo, valor);
	}

	public void alteraEmpregado(String id, String atributo, String valor, String extra) throws Exception {
		servicoEmpregados().alteraEmpregado(id, atributo, valor, extra);
	}

	public void alteraEmpregado(String id, String atributo, String valor, String idSindicato, String taxaSindical) throws Exception {
		servicoEmpregados().alteraEmpregado(id, atributo, valor, idSindicato, taxaSindical);
	}

	public void alteraEmpregado(String id, String atributo, String valor, String banco, String agencia, String conta) throws Exception {
		servicoEmpregados().alteraEmpregado(id, atributo, valor, banco, agencia, conta);
	}

	public String totalFolha(String data) throws Exception {
		return folhaPagamento.totalFolha(listaEmpregados, data);
	}

	public void rodaFolha(String data, String saida) throws Exception {
		salvarEstado();
		folhaPagamento.rodaFolha(listaEmpregados, data, saida);
	}

	private void validarObrigatorio(String valor, String mensagem) throws Exception {
		if (valor == null || valor.isEmpty()) {
			throw new Exception(mensagem);
		}
	}

}

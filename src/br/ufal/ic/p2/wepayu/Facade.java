package br.ufal.ic.p2.wepayu;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import br.ufal.ic.p2.wepayu.models.CartaoDePonto;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.EmpregadoAssalariado;
import br.ufal.ic.p2.wepayu.models.EmpregadoComissionado;
import br.ufal.ic.p2.wepayu.models.EmpregadoHorista;
import br.ufal.ic.p2.wepayu.models.RepositoryEmpregados;
import br.ufal.ic.p2.wepayu.models.ResultadoVenda;
import br.ufal.ic.p2.wepayu.models.TaxaServico;

public class Facade {

	private static class Snapshot {
		RepositoryEmpregados repo;
		Map<String, LocalDate> pagamentos;
		Map<String, LocalDate> ultimoProcessamentoFolha;

		Snapshot(RepositoryEmpregados repo, Map<String, LocalDate> pagamentos, Map<String, LocalDate> ultimoProcessamentoFolha) {
			this.repo = repo;
			this.pagamentos = new HashMap<>(pagamentos);
			this.ultimoProcessamentoFolha = new HashMap<>(ultimoProcessamentoFolha);
		}
	}

	private Stack<Snapshot> historicoUndo = new Stack<>();
	private Stack<Snapshot> historicoRedo = new Stack<>();
	private Map<String, LocalDate> ultimoPagamento = new HashMap<>();
	private Map<String, LocalDate> ultimoProcessamentoFolha = new HashMap<>();
	private boolean sistemaEncerrado = false;
	private boolean inicializado = false;

	RepositoryEmpregados listaEmpregados = new RepositoryEmpregados();

	public void zerarSistema() {
		salvarEstado();
		listaEmpregados.zerarSistema();
		ultimoPagamento.clear();
		ultimoProcessamentoFolha.clear();
		sistemaEncerrado = false;
		inicializado = true;
	}

	public void encerrarSistema() {
		sistemaEncerrado = true;
		inicializado = false;
	}

	private void salvarEstado() {
		historicoUndo.push(new Snapshot(clonarRepositorio(listaEmpregados), ultimoPagamento, ultimoProcessamentoFolha));
		historicoRedo.clear();
		inicializado = true;
	}

	public void undo() throws Exception {
		if (sistemaEncerrado) throw new Exception("Nao pode dar comandos depois de encerrarSistema.");
		if (historicoUndo.isEmpty()) throw new Exception("Nao ha comando a desfazer.");
		historicoRedo.push(new Snapshot(clonarRepositorio(listaEmpregados), ultimoPagamento, ultimoProcessamentoFolha));
		Snapshot anterior = historicoUndo.pop();
		listaEmpregados = clonarRepositorio(anterior.repo);
		ultimoPagamento = new HashMap<>(anterior.pagamentos);
		ultimoProcessamentoFolha = new HashMap<>(anterior.ultimoProcessamentoFolha);
	}

	public void redo() throws Exception {
		if (sistemaEncerrado) throw new Exception("Nao pode dar comandos depois de encerrarSistema.");
		if (historicoRedo.isEmpty()) throw new Exception("Nao ha comando a refazer.");
		historicoUndo.push(new Snapshot(clonarRepositorio(listaEmpregados), ultimoPagamento, ultimoProcessamentoFolha));
		Snapshot seguinte = historicoRedo.pop();
		listaEmpregados = clonarRepositorio(seguinte.repo);
		ultimoPagamento = new HashMap<>(seguinte.pagamentos);
		ultimoProcessamentoFolha = new HashMap<>(seguinte.ultimoProcessamentoFolha);
	}

	private RepositoryEmpregados clonarRepositorio(RepositoryEmpregados original) {
		RepositoryEmpregados clone = new RepositoryEmpregados();
		clone.setIdValido(original.getIdValido());

		for (Empregado e : original.getListaEmpregados()) {
			Empregado novo = null;

			if (e instanceof EmpregadoComissionado) {
				EmpregadoComissionado com = (EmpregadoComissionado) e;
				EmpregadoComissionado novoCom = new EmpregadoComissionado(
						e.getId(), e.getNome(), e.getEndereco(), com.getSalarioMensal(), com.getTaxaComissao());
				novoCom.getHistoricoVendas().clear();
				for (ResultadoVenda v : com.getHistoricoVendas()) {
					novoCom.adicionarVenda(new ResultadoVenda(v.getData(), v.getValor()));
				}
				novo = novoCom;
			} else if (e instanceof EmpregadoHorista) {
				EmpregadoHorista hor = (EmpregadoHorista) e;
				EmpregadoHorista novoHor = new EmpregadoHorista(
						e.getId(), e.getNome(), e.getEndereco(), hor.getSalarioPorHora());
				novoHor.getCartoesDePonto().clear();
				for (CartaoDePonto c : hor.getCartoesDePonto()) {
					novoHor.adicionarCartaoDePonto(new CartaoDePonto(c.getData(), c.getHoras()));
				}
				novo = novoHor;
			} else if (e instanceof EmpregadoAssalariado) {
				EmpregadoAssalariado ass = (EmpregadoAssalariado) e;
				novo = new EmpregadoAssalariado(e.getId(), e.getNome(), e.getEndereco(), ass.getSalarioMensal());
			}

			if (novo != null) {
				novo.setTipo(e.getTipo());
				novo.setMetodoPagamento(e.getMetodoPagamento());
				novo.setSindicalizado(e.isSindicalizado());
				novo.setIdSindicato(e.getIdSindicato());
				novo.setTaxaSindical(e.getTaxaSindical());
				novo.setBanco(e.getBanco());
				novo.setAgencia(e.getAgencia());
				novo.setContaCorrente(e.getContaCorrente());
				novo.getTaxasServico().clear();
				for (TaxaServico t : e.getTaxasServico()) {
					novo.adicionarTaxaServico(new TaxaServico(t.getData(), t.getValor()));
				}
				clone.adicionarEmpregado(novo);
			}
		}
		return clone;
	}

	public String getNumeroDeEmpregados() {
		return String.valueOf(listaEmpregados.getListaEmpregados().size());
	}

	public String getAtributoEmpregado(String id, String atributo) throws Exception {
		if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
		Empregado emp = listaEmpregados.getIdEmpregado(id);
		if (emp == null) throw new Exception("Empregado nao existe.");

		switch (atributo) {
		case "nome": return emp.getNome();
		case "endereco": return emp.getEndereco();
		case "tipo": return emp.getTipo();
		case "sindicalizado": return String.valueOf(emp.isSindicalizado());
		case "salario":
			if (emp instanceof EmpregadoHorista) return formatarMonetario(((EmpregadoHorista) emp).getSalarioPorHora());
			if (emp instanceof EmpregadoComissionado) return formatarMonetario(((EmpregadoComissionado) emp).getSalarioMensal());
			if (emp instanceof EmpregadoAssalariado) return formatarMonetario(((EmpregadoAssalariado) emp).getSalarioMensal());
			throw new Exception("Atributo nao existe.");
		case "comissao":
			if (!(emp instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");
			return formatarMonetario(((EmpregadoComissionado) emp).getTaxaComissao());
		case "metodoPagamento": return emp.getMetodoPagamento();
		case "banco":
			if (emp.getMetodoPagamento() == null || !emp.getMetodoPagamento().equals("banco")) throw new Exception("Empregado nao recebe em banco.");
			return emp.getBanco();
		case "agencia":
			if (emp.getMetodoPagamento() == null || !emp.getMetodoPagamento().equals("banco")) throw new Exception("Empregado nao recebe em banco.");
			return emp.getAgencia();
		case "contaCorrente":
			if (emp.getMetodoPagamento() == null || !emp.getMetodoPagamento().equals("banco")) throw new Exception("Empregado nao recebe em banco.");
			return emp.getContaCorrente();
		case "idSindicato":
			if (!emp.isSindicalizado()) throw new Exception("Empregado nao eh sindicalizado.");
			return emp.getIdSindicato();
		case "taxaSindical":
			if (!emp.isSindicalizado()) throw new Exception("Empregado nao eh sindicalizado.");
			return formatarMonetario(emp.getTaxaSindical());
		default: throw new Exception("Atributo nao existe.");
		}
	}

	public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
		if (nome == null || nome.isEmpty()) throw new Exception("Nome nao pode ser nulo.");
		if (endereco == null || endereco.isEmpty()) throw new Exception("Endereco nao pode ser nulo.");
		if (tipo == null || tipo.isEmpty()) throw new Exception("Tipo nao pode ser nulo.");
		if (tipo.equals("comissionado")) throw new Exception("Tipo nao aplicavel.");
		if (!tipo.equals("horista") && !tipo.equals("assalariado")) throw new Exception("Tipo invalido.");
		if (salario == null || salario.isEmpty()) throw new Exception("Salario nao pode ser nulo.");

		double salarioConvertido;
		try { salarioConvertido = Double.parseDouble(salario.replace(",", ".")); } 
		catch (NumberFormatException e) { throw new Exception("Salario deve ser numerico."); }
		if (salarioConvertido < 0) throw new Exception("Salario deve ser nao-negativo.");

		salvarEstado();

		String idValiado = listaEmpregados.gerarIdValido();
		Empregado emp = null;
		if (tipo.equals("horista")) emp = new EmpregadoHorista(idValiado, nome, endereco, salarioConvertido);
		else if (tipo.equals("assalariado")) emp = new EmpregadoAssalariado(idValiado, nome, endereco, salarioConvertido);

		listaEmpregados.adicionarEmpregado(emp);
		return idValiado;
	}

	public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws Exception {
		if (nome == null || nome.isEmpty()) throw new Exception("Nome nao pode ser nulo.");
		if (endereco == null || endereco.isEmpty()) throw new Exception("Endereco nao pode ser nulo.");
		if (tipo == null || tipo.isEmpty()) throw new Exception("Tipo nao pode ser nulo.");
		if (tipo.equals("horista") || tipo.equals("assalariado")) throw new Exception("Tipo nao aplicavel.");
		if (!tipo.equals("comissionado")) throw new Exception("Tipo invalido.");
		if (salario == null || salario.isEmpty()) throw new Exception("Salario nao pode ser nulo.");
		if (comissao == null || comissao.isEmpty()) throw new Exception("Comissao nao pode ser nula.");

		double salarioConvertido;
		try { salarioConvertido = Double.parseDouble(salario.replace(",", ".")); } 
		catch (NumberFormatException e) { throw new Exception("Salario deve ser numerico."); }
		if (salarioConvertido < 0) throw new Exception("Salario deve ser nao-negativo.");

		double comissaoConvertida;
		try { comissaoConvertida = Double.parseDouble(comissao.replace(",", ".")); } 
		catch (NumberFormatException e) { throw new Exception("Comissao deve ser numerica."); }
		if (comissaoConvertida < 0) throw new Exception("Comissao deve ser nao-negativa.");

		salvarEstado();

		String idValiado = listaEmpregados.gerarIdValido();
		Empregado emp = new EmpregadoComissionado(idValiado, nome, endereco, salarioConvertido, comissaoConvertida);

		listaEmpregados.adicionarEmpregado(emp);
		return idValiado;
	}

	public String getEmpregadoPorNome(String nome, int indice) throws Exception {
		Empregado emp = listaEmpregados.buscarEmpregadoPorNome(nome, indice);
		if (emp == null) throw new Exception("Nao ha empregado com esse nome.");
		return emp.getId();
	}

	public void removerEmpregado(String emp) throws Exception {
		if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
		Empregado empRemover = listaEmpregados.getIdEmpregado(emp);
		if (empRemover == null) throw new Exception("Empregado nao existe.");
		
		salvarEstado();
		listaEmpregados.getListaEmpregados().remove(empRemover);	
	}

	public void lancaCartao(String id, String data, String horas) throws Exception {
		if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
		Empregado emp = listaEmpregados.getIdEmpregado(id);
		if (emp == null) throw new Exception("Empregado nao existe.");
		if (!(emp instanceof EmpregadoHorista)) throw new Exception("Empregado nao eh horista.");

		LocalDate dataConvertida = converterData(data, "Data invalida.");
		double horasConvertidas;
		try { horasConvertidas = Double.parseDouble(horas.replace(",", ".")); } 
		catch (NumberFormatException e) { throw new Exception("Horas devem ser numericas."); }
		if (horasConvertidas <= 0) throw new Exception("Horas devem ser positivas.");

		salvarEstado();
		CartaoDePonto cartao = new CartaoDePonto(dataConvertida, horasConvertidas);
		((EmpregadoHorista) emp).adicionarCartaoDePonto(cartao);
	}

	public String getHorasNormaisTrabalhadas(String id, String dataInicial, String dataFinal) throws Exception {
		Empregado emp = listaEmpregados.getIdEmpregado(id);
		if (emp == null) throw new Exception("Empregado nao existe.");
		if (!(emp instanceof EmpregadoHorista)) throw new Exception("Empregado nao eh horista.");

		LocalDate inicio = converterData(dataInicial, "Data inicial invalida.");
		LocalDate fim = converterData(dataFinal, "Data final invalida.");
		if (inicio.isAfter(fim)) throw new Exception("Data inicial nao pode ser posterior aa data final.");

		double horasNormais = 0;
		for (CartaoDePonto cartao : ((EmpregadoHorista) emp).getCartoesDePonto()) {
			if (!cartao.getData().isBefore(inicio) && cartao.getData().isBefore(fim)) {
				horasNormais += Math.min(8.0, cartao.getHoras());
			}
		}
		return formatarHoras(horasNormais);
	}

	public String getHorasExtrasTrabalhadas(String id, String dataInicial, String dataFinal) throws Exception {
		Empregado emp = listaEmpregados.getIdEmpregado(id);
		if (emp == null) throw new Exception("Empregado nao existe.");
		if (!(emp instanceof EmpregadoHorista)) throw new Exception("Empregado nao eh horista.");

		LocalDate inicio = converterData(dataInicial, "Data inicial invalida.");
		LocalDate fim = converterData(dataFinal, "Data final invalida.");
		if (inicio.isAfter(fim)) throw new Exception("Data inicial nao pode ser posterior aa data final.");

		double horasExtras = 0;
		for (CartaoDePonto cartao : ((EmpregadoHorista) emp).getCartoesDePonto()) {
			if (!cartao.getData().isBefore(inicio) && cartao.getData().isBefore(fim)) {
				if (cartao.getHoras() > 8.0) horasExtras += (cartao.getHoras() - 8.0);
			}
		}
		return formatarHoras(horasExtras);
	}

	public void lancaVenda(String id, String data, String valor) throws Exception {
		if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
		Empregado emp = listaEmpregados.getIdEmpregado(id);
		if (emp == null) throw new Exception("Empregado nao existe.");
		if (!(emp instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");

		LocalDate dataConvertida = converterData(data, "Data invalida.");
		double valorConvertido;
		try { valorConvertido = Double.parseDouble(valor.replace(",", ".")); } 
		catch (NumberFormatException e) { throw new Exception("Valor deve ser numerico."); }
		if (valorConvertido <= 0) throw new Exception("Valor deve ser positivo.");

		salvarEstado();
		ResultadoVenda venda = new ResultadoVenda(dataConvertida, valorConvertido);
		((EmpregadoComissionado) emp).adicionarVenda(venda);
	}

	public String getVendasRealizadas(String id, String dataInicial, String dataFinal) throws Exception {
		Empregado emp = listaEmpregados.getIdEmpregado(id);
		if (emp == null) throw new Exception("Empregado nao existe.");
		if (!(emp instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");

		LocalDate inicio = converterData(dataInicial, "Data inicial invalida.");
		LocalDate fim = converterData(dataFinal, "Data final invalida.");
		if (inicio.isAfter(fim)) throw new Exception("Data inicial nao pode ser posterior aa data final.");

		double totalVendas = 0;
		for (ResultadoVenda venda : ((EmpregadoComissionado) emp).getHistoricoVendas()) {
			if (!venda.getData().isBefore(inicio) && venda.getData().isBefore(fim)) {
				totalVendas += venda.getValor();
			}
		}
		return formatarMonetario(totalVendas);
	}

	public void lancaTaxaServico(String idSindicato, String data, String valor) throws Exception {
		if (idSindicato == null || idSindicato.isEmpty()) throw new Exception("Identificacao do membro nao pode ser nula.");
		Empregado emp = listaEmpregados.getEmpregadoPorSindicato(idSindicato);
		if (emp == null) throw new Exception("Membro nao existe.");

		LocalDate dataConvertida = converterData(data, "Data invalida.");
		double valorConvertido;
		try { valorConvertido = Double.parseDouble(valor.replace(",", ".")); } 
		catch (NumberFormatException e) { throw new Exception("Valor deve ser numerico."); }
		if (valorConvertido <= 0) throw new Exception("Valor deve ser positivo.");

		salvarEstado();
		TaxaServico taxa = new TaxaServico(dataConvertida, valorConvertido);
		emp.adicionarTaxaServico(taxa);
	}

	public String getTaxasServico(String id, String dataInicial, String dataFinal) throws Exception {
		Empregado emp = listaEmpregados.getIdEmpregado(id);
		if (emp == null) throw new Exception("Empregado nao existe.");
		if (!emp.isSindicalizado()) throw new Exception("Empregado nao eh sindicalizado.");

		LocalDate inicio = converterData(dataInicial, "Data inicial invalida.");
		LocalDate fim = converterData(dataFinal, "Data final invalida.");
		if (inicio.isAfter(fim)) throw new Exception("Data inicial nao pode ser posterior aa data final.");

		double totalTaxas = 0;
		for (TaxaServico taxa : emp.getTaxasServico()) {
			if (!taxa.getData().isBefore(inicio) && taxa.getData().isBefore(fim)) {
				totalTaxas += taxa.getValor();
			}
		}
		return formatarMonetario(totalTaxas);
	}

	public void alteraEmpregado(String id, String atributo, String valor) throws Exception {
		if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
		Empregado emp = listaEmpregados.getIdEmpregado(id);
		if (emp == null) throw new Exception("Empregado nao existe.");

		switch (atributo) {
		case "nome":
			if (valor == null || valor.isEmpty()) throw new Exception("Nome nao pode ser nulo.");
			salvarEstado();
			emp.setNome(valor);
			break;
		case "endereco":
			if (valor == null || valor.isEmpty()) throw new Exception("Endereco nao pode ser nulo.");
			salvarEstado();
			emp.setEndereco(valor);
			break;
		case "tipo":
			if (!valor.equals("horista") && !valor.equals("assalariado") && !valor.equals("comissionado")) {
				throw new Exception("Tipo invalido.");
			}
			salvarEstado();
			String idAtual = emp.getId();
			String nomeAtual = emp.getNome();
			String endAtual = emp.getEndereco();
			boolean sind = emp.isSindicalizado();
			String idSind = emp.getIdSindicato();
			double taxa = emp.getTaxaSindical();
			String metodo = emp.getMetodoPagamento();
			String banco = emp.getBanco();
			String agencia = emp.getAgencia();
			String conta = emp.getContaCorrente();

			listaEmpregados.getListaEmpregados().remove(emp);
			Empregado novoEmp = null;
			if (valor.equals("horista")) novoEmp = new EmpregadoHorista(idAtual, nomeAtual, endAtual, 0.0);
			else if (valor.equals("assalariado")) novoEmp = new EmpregadoAssalariado(idAtual, nomeAtual, endAtual, 0.0);
			else novoEmp = new EmpregadoComissionado(idAtual, nomeAtual, endAtual, 0.0, 0.0);

			novoEmp.setSindicalizado(sind);
			novoEmp.setIdSindicato(idSind);
			novoEmp.setTaxaSindical(taxa);
			novoEmp.setMetodoPagamento(metodo);
			novoEmp.setBanco(banco);
			novoEmp.setAgencia(agencia);
			novoEmp.setContaCorrente(conta);
			listaEmpregados.adicionarEmpregado(novoEmp);
			break;
		case "salario":
			if (valor == null || valor.isEmpty()) throw new Exception("Salario nao pode ser nulo.");
			double novoSalario;
			try { novoSalario = Double.parseDouble(valor.replace(",", ".")); } 
			catch (NumberFormatException e) { throw new Exception("Salario deve ser numerico."); }
			if (novoSalario < 0) throw new Exception("Salario deve ser nao-negativo.");
			salvarEstado();
			if (emp instanceof EmpregadoHorista) ((EmpregadoHorista) emp).setSalarioPorHora(novoSalario);
			else if (emp instanceof EmpregadoAssalariado) ((EmpregadoAssalariado) emp).setSalarioMensal(novoSalario);
			else if (emp instanceof EmpregadoComissionado) ((EmpregadoComissionado) emp).setSalarioMensal(novoSalario);
			break;
		case "comissao":
			if (valor == null || valor.isEmpty()) throw new Exception("Comissao nao pode ser nula.");
			double novaComissao;
			try { novaComissao = Double.parseDouble(valor.replace(",", ".")); } 
			catch (NumberFormatException e) { throw new Exception("Comissao deve ser numerica."); }
			if (novaComissao < 0) throw new Exception("Comissao deve ser nao-negativa.");
			if (!(emp instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");
			salvarEstado();
			((EmpregadoComissionado) emp).setTaxaComissao(novaComissao);
			break;
		case "metodoPagamento":
			if (!valor.equals("emMaos") && !valor.equals("correios") && !valor.equals("banco")) {
				throw new Exception("Metodo de pagamento invalido.");
			}
			salvarEstado();
			emp.setMetodoPagamento(valor);
			break;
		case "sindicalizado":
			if (!valor.equals("true") && !valor.equals("false")) throw new Exception("Valor deve ser true ou false.");
			salvarEstado();
			boolean status = Boolean.parseBoolean(valor);
			emp.setSindicalizado(status);
			if (!status) {
				emp.setIdSindicato(null);
				emp.setTaxaSindical(0);
			}
			break;
		default: throw new Exception("Atributo nao existe.");
		}
	}

	public void alteraEmpregado(String id, String atributo, String valor, String valorExtra) throws Exception {
		if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
		Empregado emp = listaEmpregados.getIdEmpregado(id);
		if (emp == null) throw new Exception("Empregado nao existe.");

		if (atributo.equals("tipo")) {
			salvarEstado();
			String idAtual = emp.getId();
			String nomeAtual = emp.getNome();
			String endAtual = emp.getEndereco();
			boolean sind = emp.isSindicalizado();
			String idSind = emp.getIdSindicato();
			double taxa = emp.getTaxaSindical();
			String metodo = emp.getMetodoPagamento();
			String banco = emp.getBanco();
			String agencia = emp.getAgencia();
			String conta = emp.getContaCorrente();

			listaEmpregados.getListaEmpregados().remove(emp);
			Empregado novoEmp = null;
			double valorNumerico = Double.parseDouble(valorExtra.replace(",", "."));

			if (valor.equals("horista")) novoEmp = new EmpregadoHorista(idAtual, nomeAtual, endAtual, valorNumerico);
			else if (valor.equals("assalariado")) novoEmp = new EmpregadoAssalariado(idAtual, nomeAtual, endAtual, valorNumerico);
			else if (valor.equals("comissionado")) {
				double salBase = 0.0;
				if (emp instanceof EmpregadoAssalariado) salBase = ((EmpregadoAssalariado) emp).getSalarioMensal();
				else if (emp instanceof EmpregadoHorista) salBase = ((EmpregadoHorista) emp).getSalarioPorHora();
				novoEmp = new EmpregadoComissionado(idAtual, nomeAtual, endAtual, salBase, valorNumerico);
			}

			novoEmp.setSindicalizado(sind);
			novoEmp.setIdSindicato(idSind);
			novoEmp.setTaxaSindical(taxa);
			novoEmp.setMetodoPagamento(metodo);
			novoEmp.setBanco(banco);
			novoEmp.setAgencia(agencia);
			novoEmp.setContaCorrente(conta);
			listaEmpregados.adicionarEmpregado(novoEmp);
		}
	}

	public void alteraEmpregado(String id, String atributo, String valor, String idSindicato, String taxaSindical) throws Exception {
		if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
		Empregado emp = listaEmpregados.getIdEmpregado(id);
		if (emp == null) throw new Exception("Empregado nao existe.");

		if (atributo.equals("sindicalizado") && valor.equals("true")) {
			if (idSindicato == null || idSindicato.isEmpty()) throw new Exception("Identificacao do sindicato nao pode ser nula.");
			if (taxaSindical == null || taxaSindical.isEmpty()) throw new Exception("Taxa sindical nao pode ser nula.");

			double taxa;
			try { taxa = Double.parseDouble(taxaSindical.replace(",", ".")); } 
			catch (NumberFormatException e) { throw new Exception("Taxa sindical deve ser numerica."); }
			if (taxa < 0) throw new Exception("Taxa sindical deve ser nao-negativa.");

			Empregado outroEmp = listaEmpregados.getEmpregadoPorSindicato(idSindicato);
			if (outroEmp != null && !outroEmp.getId().equals(id)) throw new Exception("Ha outro empregado com esta identificacao de sindicato");

			salvarEstado();
			emp.setSindicalizado(true);
			emp.setIdSindicato(idSindicato);
			emp.setTaxaSindical(taxa);
		}
	}

	public void alteraEmpregado(String id, String atributo, String valor, String banco, String agencia, String contaCorrente) throws Exception {
		if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
		Empregado emp = listaEmpregados.getIdEmpregado(id);
		if (emp == null) throw new Exception("Empregado nao existe.");

		if (atributo.equals("metodoPagamento") && valor.equals("banco")) {
			if (banco == null || banco.isEmpty()) throw new Exception("Banco nao pode ser nulo.");
			if (agencia == null || agencia.isEmpty()) throw new Exception("Agencia nao pode ser nulo.");
			if (contaCorrente == null || contaCorrente.isEmpty()) throw new Exception("Conta corrente nao pode ser nulo.");

			salvarEstado();
			emp.setMetodoPagamento(valor);
			emp.setBanco(banco);
			emp.setAgencia(agencia);
			emp.setContaCorrente(contaCorrente);
		}
	}

	public String totalFolha(String data) throws Exception {
		LocalDate dataFolha = converterData(data, "Data invalida.");
		double totalBruto = 0.0;
		for (Empregado emp : listaEmpregados.getListaEmpregados()) {
			if (isDiaDePagamento(emp, dataFolha)) {
				totalBruto += calcularSalarioBruto(emp, dataFolha);
			}
		}
		return formatarMonetario(arredondar(totalBruto));
	}

	public void rodaFolha(String data, String saida) throws Exception {
		salvarEstado();
		LocalDate dataFolha = converterData(data, "Data invalida.");

		List<EmpregadoHorista> horistas = new ArrayList<>();
		List<EmpregadoAssalariado> assalariados = new ArrayList<>();
		List<EmpregadoComissionado> comissionados = new ArrayList<>();

		for (Empregado emp : listaEmpregados.getListaEmpregados()) {
			if (isDiaDePagamento(emp, dataFolha)) {
				if (emp instanceof EmpregadoComissionado) comissionados.add((EmpregadoComissionado) emp);
				else if (emp instanceof EmpregadoHorista) horistas.add((EmpregadoHorista) emp);
				else if (emp instanceof EmpregadoAssalariado) assalariados.add((EmpregadoAssalariado) emp);
			}
		}

		horistas.sort(Comparator.comparing(Empregado::getNome));
		assalariados.sort(Comparator.comparing(Empregado::getNome));
		comissionados.sort(Comparator.comparing(Empregado::getNome));

		boolean haPagamentos = !horistas.isEmpty() || !assalariados.isEmpty() || !comissionados.isEmpty();

		try (PrintWriter writer = new PrintWriter(new FileWriter(saida))) {
			writer.println("FOLHA DE PAGAMENTO DO DIA " + dataFolha.toString());

			if (!haPagamentos) {
				writer.println("====================================");
				writer.println("Nao ha funcionarios a serem pagos hoje.");
				writer.println("====================================");
				writer.println();
				writer.print("TOTAL FOLHA: 0,00");
			} else {
				writer.println("====================================");
				writer.println();

				// --- BLOCO HORISTAS ---
				writer.println("===============================================================================================================================");
				writer.println("===================== HORISTAS ================================================================================================");
				writer.println("===============================================================================================================================");
				writer.println("Nome                                 Horas Extra Salario Bruto Descontos Salario Liquido Metodo");
				writer.println("==================================== ===== ===== ============= ========= =============== ======================================");

				int totalHoras = 0;
				int totalExtras = 0;
				double totalBrutoHoristas = 0.0;
				double totalDescHoristas = 0.0;
				double totalLiqHoristas = 0.0;

				for (EmpregadoHorista h : horistas) {
					double[] hs = obterHoras(h, dataFolha);
					int hn = (int) hs[0];
					int he = (int) hs[1];
					double bruto = arredondar(calcularSalarioBruto(h, dataFolha));
					double desc = arredondar(calcularDescontos(h, dataFolha));
					double liq = arredondar(bruto - desc);

					totalHoras += hn;
					totalExtras += he;
					totalBrutoHoristas += bruto;
					totalDescHoristas += desc;
					totalLiqHoristas += liq;

					writer.printf("%-36s %5d %5d %13s %9s %15s %s\n",
							h.getNome(), hn, he,
							formatarMonetario(bruto),
							formatarMonetario(desc),
							formatarMonetario(liq),
							formatarMetodo(h));
				}
				writer.println();
				writer.printf("%-36s %5d %5d %13s %9s %15s\n",
						"TOTAL HORISTAS", totalHoras, totalExtras,
						formatarMonetario(totalBrutoHoristas),
						formatarMonetario(totalDescHoristas),
						formatarMonetario(totalLiqHoristas));
				writer.println();

				// --- BLOCO ASSALARIADOS ---
				writer.println("===============================================================================================================================");
				writer.println("===================== ASSALARIADOS ============================================================================================");
				writer.println("===============================================================================================================================");
				writer.println("Nome                                             Salario Bruto Descontos Salario Liquido Metodo");
				writer.println("================================================ ============= ========= =============== ======================================");

				double totalBrutoAss = 0.0;
				double totalDescAss = 0.0;
				double totalLiqAss = 0.0;

				for (EmpregadoAssalariado a : assalariados) {
					double bruto = arredondar(calcularSalarioBruto(a, dataFolha));
					double desc = arredondar(calcularDescontos(a, dataFolha));
					double liq = arredondar(bruto - desc);

					totalBrutoAss += bruto;
					totalDescAss += desc;
					totalLiqAss += liq;

					writer.printf("%-48s %13s %9s %15s %s\n",
							a.getNome(),
							formatarMonetario(bruto),
							formatarMonetario(desc),
							formatarMonetario(liq),
							formatarMetodo(a));
				}
				writer.println();
				writer.printf("%-48s %13s %9s %15s\n",
						"TOTAL ASSALARIADOS",
						formatarMonetario(totalBrutoAss),
						formatarMonetario(totalDescAss),
						formatarMonetario(totalLiqAss));
				writer.println();

				// --- BLOCO COMISSIONADOS ---
				writer.println("===============================================================================================================================");
				writer.println("===================== COMISSIONADOS ===========================================================================================");
				writer.println("===============================================================================================================================");
				writer.println("Nome                  Fixo     Vendas   Comissao Salario Bruto Descontos Salario Liquido Metodo");
				writer.println("===================== ======== ======== ======== ============= ========= =============== ======================================");

				double totalFixoCom = 0.0;
				double totalVendasCom = 0.0;
				double totalComissaoCom = 0.0;
				double totalBrutoCom = 0.0;
				double totalDescCom = 0.0;
				double totalLiqCom = 0.0;

				for (EmpregadoComissionado c : comissionados) {
					double fixo = truncar((c.getSalarioMensal() * 12.0) / 26.0);
					double vendas = 0.0;
					LocalDate ultimoProc = ultimoProcessamentoFolha.get(c.getId());
					for (ResultadoVenda v : c.getHistoricoVendas()) {
						if (!v.getData().isAfter(dataFolha) && (ultimoProc == null || v.getData().isAfter(ultimoProc))) {
							vendas += v.getValor();
						}
					}
					vendas = arredondar(vendas);
					double comissaoValor = truncar(vendas * c.getTaxaComissao());
					double bruto = arredondar(fixo + comissaoValor);
					double desc = arredondar(calcularDescontos(c, dataFolha));
					double liq = arredondar(bruto - desc);

					totalFixoCom += fixo;
					totalVendasCom += vendas;
					totalComissaoCom += comissaoValor;
					totalBrutoCom += bruto;
					totalDescCom += desc;
					totalLiqCom += liq;

					writer.printf("%-21s %8s %8s %8s %13s %9s %15s %s\n",
							c.getNome(),
							formatarMonetario(fixo),
							formatarMonetario(vendas),
							formatarMonetario(comissaoValor),
							formatarMonetario(bruto),
							formatarMonetario(desc),
							formatarMonetario(liq),
							formatarMetodo(c));
				}
				writer.println();
				writer.printf("%-21s %8s %8s %8s %13s %9s %15s\n",
						"TOTAL COMISSIONADOS",
						formatarMonetario(totalFixoCom),
						formatarMonetario(totalVendasCom),
						formatarMonetario(totalComissaoCom),
						formatarMonetario(totalBrutoCom),
						formatarMonetario(totalDescCom),
						formatarMonetario(totalLiqCom));
				writer.println();

				double totalGeralBruto = arredondar(totalBrutoHoristas + totalBrutoAss + totalBrutoCom);
				writer.print("TOTAL FOLHA: " + formatarMonetario(totalGeralBruto));
			}
		} catch (Exception e) {
			throw new Exception("Erro ao gerar folha.");
		}

		for (EmpregadoHorista h : horistas) {
			double bruto = calcularSalarioBruto(h, dataFolha);
			if (bruto > 0) {
				ultimoPagamento.put(h.getId(), dataFolha);
			}
			ultimoProcessamentoFolha.put(h.getId(), dataFolha);
		}
		for (EmpregadoComissionado c : comissionados) {
			ultimoPagamento.put(c.getId(), dataFolha);
			ultimoProcessamentoFolha.put(c.getId(), dataFolha);
		}
		for (EmpregadoAssalariado a : assalariados) {
			ultimoPagamento.put(a.getId(), dataFolha);
			ultimoProcessamentoFolha.put(a.getId(), dataFolha);
		}
	}

	private String formatarMetodo(Empregado emp) {
		String metodo = emp.getMetodoPagamento();
		if (metodo.equals("emMaos")) return "Em maos";
		if (metodo.equals("correios")) return "Correios, " + emp.getEndereco();
		if (metodo.equals("banco")) {
			return String.format("%s, Ag. %s CC %s", emp.getBanco(), emp.getAgencia(), emp.getContaCorrente());
		}
		return metodo;
	}

	private LocalDate converterData(String data, String mensagemErro) throws Exception {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT);
			return LocalDate.parse(data, formatter);
		} catch (DateTimeParseException e) {
			throw new Exception(mensagemErro);
		}
	}

	private double arredondar(double valor) {
		return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	private double truncar(double valor) {
		return BigDecimal.valueOf(valor).setScale(2, RoundingMode.DOWN).doubleValue();
	}

	private String formatarMonetario(double valor) {
		return String.format("%.2f", valor).replace(".", ",");
	}

	private String formatarHoras(double valor) {
		if (valor % 1 == 0) return String.valueOf((long) valor);
		String formatado = String.valueOf(valor).replace(".", ",");
		if (formatado.endsWith(",0")) return formatado.substring(0, formatado.length() - 2);
		return formatado;
	}

	private boolean isDiaDePagamento(Empregado emp, LocalDate data) {
		String tipo = emp.getTipo();
		if (tipo.equals("assalariado")) {
			LocalDate ultimoDia = data.with(TemporalAdjusters.lastDayOfMonth());
			while (ultimoDia.getDayOfWeek() == DayOfWeek.SATURDAY || ultimoDia.getDayOfWeek() == DayOfWeek.SUNDAY) {
				ultimoDia = ultimoDia.minusDays(1);
			}
			return data.equals(ultimoDia);
		} else if (tipo.equals("horista")) {
			return data.getDayOfWeek() == DayOfWeek.FRIDAY;
		} else if (tipo.equals("comissionado")) {
			if (data.getDayOfWeek() != DayOfWeek.FRIDAY) return false;
			long semanas = ChronoUnit.WEEKS.between(LocalDate.of(2005, 1, 14), data);
			return Math.abs(semanas) % 2 == 0;
		}
		return false;
	}

	private double[] obterHoras(Empregado emp, LocalDate dataFolha) {
		double hNormais = 0.0;
		double hExtras = 0.0;
		if (emp instanceof EmpregadoHorista) {
			LocalDate ultimoProc = ultimoProcessamentoFolha.get(emp.getId());
			for (CartaoDePonto c : ((EmpregadoHorista) emp).getCartoesDePonto()) {
				if (!c.getData().isAfter(dataFolha) && (ultimoProc == null || c.getData().isAfter(ultimoProc))) {
					hNormais += Math.min(8.0, c.getHoras());
					if (c.getHoras() > 8.0) hExtras += (c.getHoras() - 8.0);
				}
			}
		}
		return new double[]{hNormais, hExtras};
	}

	private double calcularSalarioBruto(Empregado emp, LocalDate dataFolha) {
		double bruto = 0.0;
		if (emp instanceof EmpregadoHorista) {
			EmpregadoHorista h = (EmpregadoHorista) emp;
			double[] horas = obterHoras(h, dataFolha);
			bruto = (horas[0] * h.getSalarioPorHora()) + (horas[1] * h.getSalarioPorHora() * 1.5);
		} else if (emp instanceof EmpregadoComissionado) {
			EmpregadoComissionado c = (EmpregadoComissionado) emp;
			double fixo = truncar((c.getSalarioMensal() * 12.0) / 26.0);
			double vendas = 0.0;
			LocalDate ultimoProc = ultimoProcessamentoFolha.get(c.getId());
			for (ResultadoVenda v : c.getHistoricoVendas()) {
				if (!v.getData().isAfter(dataFolha) && (ultimoProc == null || v.getData().isAfter(ultimoProc))) {
					vendas += v.getValor();
				}
			}
			double comissaoValor = truncar(arredondar(vendas) * c.getTaxaComissao());
			bruto = fixo + comissaoValor;
		} else if (emp instanceof EmpregadoAssalariado) {
			bruto = ((EmpregadoAssalariado) emp).getSalarioMensal();
		}
		return bruto;
	}

	private double calcularDescontos(Empregado emp, LocalDate dataFolha) {
		double descontos = 0.0;
		if (emp.isSindicalizado()) {
			double bruto = calcularSalarioBruto(emp, dataFolha);
			if (bruto > 0) {
				long diasPeriodo;
				if (emp instanceof EmpregadoHorista) {
					LocalDate ultimo = ultimoPagamento.get(emp.getId());
					if (ultimo != null) {
						diasPeriodo = ChronoUnit.DAYS.between(ultimo, dataFolha);
					} else {
						diasPeriodo = 7;
					}
				} else if (emp instanceof EmpregadoComissionado) {
					diasPeriodo = 14;
				} else {
					diasPeriodo = dataFolha.lengthOfMonth();
				}
				descontos += (emp.getTaxaSindical() * diasPeriodo);
			}
			LocalDate ultimoProc = ultimoProcessamentoFolha.get(emp.getId());
			for (TaxaServico t : emp.getTaxasServico()) {
				if (!t.getData().isAfter(dataFolha) && (ultimoProc == null || t.getData().isAfter(ultimoProc))) {
					descontos += t.getValor();
				}
			}
		}
		return descontos;
	}
}

package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.EmpregadoAssalariado;
import br.ufal.ic.p2.wepayu.models.EmpregadoComissionado;
import br.ufal.ic.p2.wepayu.models.EmpregadoHorista;
import br.ufal.ic.p2.wepayu.models.RepositoryEmpregados;
import br.ufal.ic.p2.wepayu.models.CartaoDePonto;
import br.ufal.ic.p2.wepayu.models.ResultadoVenda;
import br.ufal.ic.p2.wepayu.models.TaxaServico;

public class Facade {

	RepositoryEmpregados listaEmpregados = new RepositoryEmpregados();

	public void zerarSistema() {

		listaEmpregados.zerarSistema();
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
			if (emp instanceof EmpregadoHorista) {
				return String.format("%.2f", ((EmpregadoHorista) emp).getSalarioPorHora()).replace(".", ",");
			} else if (emp instanceof EmpregadoAssalariado) {
				return String.format("%.2f", ((EmpregadoAssalariado) emp).getSalarioMensal()).replace(".", ",");
			} else if (emp instanceof EmpregadoComissionado) {
				return String.format("%.2f", ((EmpregadoComissionado) emp).getSalarioMensal()).replace(".", ",");
			}
			throw new Exception("Atributo nao existe.");
		case "comissao":
			if (!(emp instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");
			return String.format("%.2f", ((EmpregadoComissionado) emp).getTaxaComissao()).replace(".", ",");
		case "metodoPagamento":
			return emp.getMetodoPagamento();
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
			return String.format("%.2f", emp.getTaxaSindical()).replace(".", ",");
		default:
			throw new Exception("Atributo nao existe.");
		}
	}

	// Para Assalariados e Horistas
	public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {

		// Verifica os parametros
		if (nome == null || nome.isEmpty()) 
			throw new Exception("Nome nao pode ser nulo.");

		if (endereco == null || endereco.isEmpty()) 
			throw new Exception("Endereco nao pode ser nulo.");

		if (tipo == null || tipo.isEmpty())
			throw new Exception("Tipo nao pode ser nulo.");

		if (tipo.equals("comissionado"))
			throw new Exception("Tipo nao aplicavel.");

		if (!tipo.equals("horista") && !tipo.equals("assalariado"))
			throw new Exception("Tipo invalido.");

		if (salario == null || salario.isEmpty()) 
			throw new Exception("Salario nao pode ser nulo.");

		double salarioConvertido;

		try {
			// Troca a vírgula por ponto e converte
			salarioConvertido = Double.valueOf(salario.replace(",", "."));

		} catch (NumberFormatException e) {

			// Se falhar (por exemplo, vier "abc"), joga exceção
			throw new Exception("Salario deve ser numerico.");
		}

		// Checa salário negativo
		if (salarioConvertido < 0) {

			throw new Exception("Salario deve ser nao-negativo.");
		}

		Empregado emp = null;
		String idValiado = listaEmpregados.gerarIdValido();

		switch (tipo) {

		case "horista" : emp = new EmpregadoHorista(idValiado, nome, endereco, salarioConvertido);
		break;

		case "assalariado" : emp = new EmpregadoAssalariado(idValiado, nome, endereco, salarioConvertido);
		break;

		//case "comissionado" : emp = new EmpregadoComissionado(idValiado, nome, endereco, salarioConvertido, salarioConvertido);

		}

		listaEmpregados.adicionarEmpregado(emp);

		return idValiado;
	}

	public String criarEmpregado(String nome, String endereco, String tipo, 
			String salario, String comissao) throws Exception {

		// Verifica os parametros
		if (nome == null || nome.isEmpty()) 
			throw new Exception("Nome nao pode ser nulo.");

		if (endereco == null || endereco.isEmpty()) 
			throw new Exception("Endereco nao pode ser nulo.");

		if (tipo == null || tipo.isEmpty())
			throw new Exception("Tipo nao pode ser nulo.");

		if (tipo.equals("horista") || tipo.equals("assalariado"))
			throw new Exception("Tipo nao aplicavel.");

		if (!tipo.equals("comissionado"))
			throw new Exception("Tipo invalido.");

		if (salario == null || salario.isEmpty()) 
			throw new Exception("Salario nao pode ser nulo.");

		if (comissao == null || comissao.isEmpty()) 
			throw new Exception("Comissao nao pode ser nula.");

		// Verificao de salario
		double salarioConvertido;

		try {
			salarioConvertido = Double.valueOf(salario.replace(",", ".")); 
		} 

		catch (NumberFormatException e) {
			throw new Exception("Salario deve ser numerico.");
		}

		if (salarioConvertido < 0) {
			throw new Exception("Salario deve ser nao-negativo.");
		}

		// Verificacao de comissao
		double comissaoConvertida;

		try {
			comissaoConvertida = Double.valueOf(comissao.replace(",", "."));
		} 

		catch (NumberFormatException e) {

			throw new Exception("Comissao deve ser numerica.");
		}

		if (comissaoConvertida < 0) {
			throw new Exception("Comissao deve ser nao-negativa.");
		}

		String idValiado = listaEmpregados.gerarIdValido();

		Empregado emp = new EmpregadoComissionado(idValiado, nome, endereco, salarioConvertido, comissaoConvertida);

		listaEmpregados.adicionarEmpregado(emp);

		return idValiado;
	}

	public String getEmpregadoPorNome(String nome, int indice) throws Exception {

		Empregado emp = listaEmpregados.buscarEmpregadoPorNome(nome, indice);

		if (emp == null) {

			throw new Exception("Nao ha empregado com esse nome.");
		}

		return emp.getId();
	}

	public void encerrarSistema() {

	}

	public void removerEmpregado(String emp) throws Exception {

		if (emp == null || emp.isEmpty())
			throw new Exception("Identificacao do empregado nao pode ser nula.");

		Empregado empRemover = listaEmpregados.getIdEmpregado(emp);

		if (empRemover == null)
			throw new Exception("Empregado nao existe.");

		listaEmpregados.removerEmpregado(empRemover);		
	}

	public void lancaCartao(String id, String data, String horas) throws Exception {
		if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");

		Empregado emp = listaEmpregados.getIdEmpregado(id);
		if (emp == null) throw new Exception("Empregado nao existe.");
		if (!(emp instanceof EmpregadoHorista)) throw new Exception("Empregado nao eh horista.");

		LocalDate dataConvertida = converterData(data, "Data invalida.");

		double horasConvertidas;
		try {
			horasConvertidas = Double.parseDouble(horas.replace(",", "."));
		} catch (NumberFormatException e) {
			throw new Exception("Horas devem ser numericas.");
		}

		if (horasConvertidas <= 0) throw new Exception("Horas devem ser positivas.");

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

		if (horasNormais % 1 == 0) return String.valueOf((int) horasNormais);

		return formatarDouble(horasNormais);
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

				if (cartao.getHoras() > 8.0) {
					horasExtras += (cartao.getHoras() - 8.0);
				}
			}
		}

		if (horasExtras % 1 == 0) return String.valueOf((int) horasExtras);
		return formatarDouble(horasExtras);
	}

	public void lancaVenda(String id, String data, String valor) throws Exception {
		if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");

		Empregado emp = listaEmpregados.getIdEmpregado(id);
		if (emp == null) throw new Exception("Empregado nao existe.");
		if (!(emp instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");

		LocalDate dataConvertida = converterData(data, "Data invalida.");

		double valorConvertido;
		try {
			valorConvertido = Double.parseDouble(valor.replace(",", "."));
		} catch (NumberFormatException e) {
			throw new Exception("Valor deve ser numerico.");
		}

		if (valorConvertido <= 0) throw new Exception("Valor deve ser positivo.");

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

		return String.format("%.2f", totalVendas).replace(".", ",");
	}

	public void lancaTaxaServico(String idSindicato, String data, String valor) throws Exception {
		if (idSindicato == null || idSindicato.isEmpty()) throw new Exception("Identificacao do membro nao pode ser nula.");

		Empregado emp = listaEmpregados.getEmpregadoPorSindicato(idSindicato);
		if (emp == null) throw new Exception("Membro nao existe.");

		LocalDate dataConvertida = converterData(data, "Data invalida.");

		double valorConvertido;
		try {
			valorConvertido = Double.parseDouble(valor.replace(",", "."));
		} catch (NumberFormatException e) {
			throw new Exception("Valor deve ser numerico.");
		}

		if (valorConvertido <= 0) throw new Exception("Valor deve ser positivo.");

		TaxaServico taxa = new TaxaServico(dataConvertida, valorConvertido);
		emp.adicionarTaxaServico(taxa);
	}

	public String getTaxasServico(String id, String dataInicial, String dataFinal) throws Exception {
		Empregado emp = listaEmpregados.getIdEmpregado(id);
		if (emp == null) throw new Exception("Empregado nao existe.");

		// Nova trava de segurança do Sindicato
		if (!emp.isSindicalizado()) {
			throw new Exception("Empregado nao eh sindicalizado.");
		}

		LocalDate inicio = converterData(dataInicial, "Data inicial invalida.");
		LocalDate fim = converterData(dataFinal, "Data final invalida.");

		if (inicio.isAfter(fim)) throw new Exception("Data inicial nao pode ser posterior aa data final.");

		double totalTaxas = 0;
		for (TaxaServico taxa : emp.getTaxasServico()) {
			// Correção do intervalo de datas aplicada aqui
			if (!taxa.getData().isBefore(inicio) && taxa.getData().isBefore(fim)) {
				totalTaxas += taxa.getValor();
			}
		}

		return String.format("%.2f", totalTaxas).replace(".", ",");
	}

	// alteraEmpregado de 3 paramêtros - Identificação
	public void alteraEmpregado(String id, String atributo, String valor) throws Exception {
		if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
		Empregado emp = listaEmpregados.getIdEmpregado(id);
		if (emp == null) throw new Exception("Empregado nao existe.");

		switch (atributo) {
		case "nome":
			if (valor == null || valor.isEmpty()) throw new Exception("Nome nao pode ser nulo.");
			emp.setNome(valor);
			break;
		case "endereco":
			if (valor == null || valor.isEmpty()) throw new Exception("Endereco nao pode ser nulo.");
			emp.setEndereco(valor);
			break;
		case "tipo":
			if (!valor.equals("horista") && !valor.equals("assalariado") && !valor.equals("comissionado")) {
				throw new Exception("Tipo invalido.");
			}

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

			listaEmpregados.removerEmpregado(emp);
			Empregado novoEmp = null;

			if (valor.equals("horista")) {
				novoEmp = new EmpregadoHorista(idAtual, nomeAtual, endAtual, 0.0);
			} else if (valor.equals("assalariado")) {
				novoEmp = new EmpregadoAssalariado(idAtual, nomeAtual, endAtual, 0.0);
			} else {
				novoEmp = new EmpregadoComissionado(idAtual, nomeAtual, endAtual, 0.0, 0.0);
			}

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
			((EmpregadoComissionado) emp).setTaxaComissao(novaComissao);
			break;
		case "metodoPagamento":
			if (!valor.equals("emMaos") && !valor.equals("correios") && !valor.equals("banco")) {
				throw new Exception("Metodo de pagamento invalido.");
			}
			emp.setMetodoPagamento(valor);
			break;
		case "sindicalizado":
			if (!valor.equals("true") && !valor.equals("false")) throw new Exception("Valor deve ser true ou false.");
			boolean status = Boolean.parseBoolean(valor);
			emp.setSindicalizado(status);
			if (!status) {
				emp.setIdSindicato(null);
				emp.setTaxaSindical(0);
			}
			break;
		default:
			throw new Exception("Atributo nao existe.");
		}
	}

	// alteraEmpregado de 4 paramêtros - Sobrecarga para sindicalização com ID e Taxa
	public void alteraEmpregado(String id, String atributo, String valor, String valorExtra) throws Exception {
		if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
		Empregado emp = listaEmpregados.getIdEmpregado(id);
		if (emp == null) throw new Exception("Empregado nao existe.");

		if (atributo.equals("tipo")) {
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

			listaEmpregados.removerEmpregado(emp);
			Empregado novoEmp = null;
			double valorNumerico = Double.parseDouble(valorExtra.replace(",", "."));

			if (valor.equals("horista")) {
				novoEmp = new EmpregadoHorista(idAtual, nomeAtual, endAtual, valorNumerico);
			} else if (valor.equals("assalariado")) {
				novoEmp = new EmpregadoAssalariado(idAtual, nomeAtual, endAtual, valorNumerico);
			} else if (valor.equals("comissionado")) {
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

	// alteraEmpregado de 5 Parâmetros - Sindicalização
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
			if (outroEmp != null && !outroEmp.getId().equals(id)) {
				throw new Exception("Ha outro empregado com esta identificacao de sindicato");
			}

			emp.setSindicalizado(true);
			emp.setIdSindicato(idSindicato);
			emp.setTaxaSindical(taxa);
		}
	}

	// alteraEmpregado de 6 Parâmetros - Conta Bancária
	public void alteraEmpregado(String id, String atributo, String valor, String banco, String agencia, String contaCorrente) throws Exception {
		if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
		Empregado emp = listaEmpregados.getIdEmpregado(id);
		if (emp == null) throw new Exception("Empregado nao existe.");

		if (atributo.equals("metodoPagamento") && valor.equals("banco")) {
			if (banco == null || banco.isEmpty()) throw new Exception("Banco nao pode ser nulo.");
			if (agencia == null || agencia.isEmpty()) throw new Exception("Agencia nao pode ser nulo.");
			if (contaCorrente == null || contaCorrente.isEmpty()) throw new Exception("Conta corrente nao pode ser nulo.");

			emp.setMetodoPagamento(valor);
			emp.setBanco(banco);
			emp.setAgencia(agencia);
			emp.setContaCorrente(contaCorrente);
		}
	}

	private LocalDate converterData(String data, String mensagemErro) throws Exception {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/uuuu")
					.withResolverStyle(java.time.format.ResolverStyle.STRICT);
			return LocalDate.parse(data, formatter);
		} catch (DateTimeParseException e) {
			throw new Exception(mensagemErro);
		}
	}

	private String formatarDouble(double valor) {
		if (valor % 1 == 0) {
			return String.valueOf((long) valor);
		}
		return String.valueOf(valor).replace(".", ",");
	}
}
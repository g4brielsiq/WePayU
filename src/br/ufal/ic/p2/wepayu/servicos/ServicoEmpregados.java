package br.ufal.ic.p2.wepayu.servicos;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import br.ufal.ic.p2.wepayu.entidades.CartaoDePonto;
import br.ufal.ic.p2.wepayu.entidades.Empregado;
import br.ufal.ic.p2.wepayu.entidades.EmpregadoAssalariado;
import br.ufal.ic.p2.wepayu.entidades.EmpregadoComissionado;
import br.ufal.ic.p2.wepayu.entidades.EmpregadoHorista;
import br.ufal.ic.p2.wepayu.entidades.ResultadoVenda;
import br.ufal.ic.p2.wepayu.entidades.TaxaServico;
import br.ufal.ic.p2.wepayu.excecoes.AtributoNaoExisteException;
import br.ufal.ic.p2.wepayu.excecoes.EmpregadoNaoExisteException;
import br.ufal.ic.p2.wepayu.excecoes.IdentificacaoNulaException;
import br.ufal.ic.p2.wepayu.excecoes.ValorInvalidoException;
import br.ufal.ic.p2.wepayu.repositorio.RepositoryAgendas;
import br.ufal.ic.p2.wepayu.repositorio.RepositoryEmpregados;
import br.ufal.ic.p2.wepayu.util.FormatoUtil;

public class ServicoEmpregados {
	private final RepositoryEmpregados empregados;
	private final Runnable registrarAlteracao;
	private final RepositoryAgendas agendas;

	public ServicoEmpregados(RepositoryEmpregados empregados, Runnable registrarAlteracao) {
		this(empregados, registrarAlteracao, new RepositoryAgendas());
	}

	public ServicoEmpregados(RepositoryEmpregados empregados, Runnable registrarAlteracao, RepositoryAgendas agendas) {
		this.empregados = empregados;
		this.registrarAlteracao = registrarAlteracao;
		this.agendas = agendas != null ? agendas : new RepositoryAgendas();
	}

	public String getNumeroDeEmpregados() {
		return String.valueOf(empregados.getNumeroDeEmpregados());
	}

	public String getAtributoEmpregado(String id, String atributo) throws Exception {
		if (id == null || id.isEmpty()) throw new IdentificacaoNulaException("Identificacao do empregado nao pode ser nula.");
		Empregado emp = empregados.getEmpregado(id);
		if (emp == null) throw new EmpregadoNaoExisteException();

		switch (atributo) {
		case "nome": return emp.getNome();
		case "endereco": return emp.getEndereco();
		case "tipo": return emp.getTipo();
		case "agendaPagamento": return emp.getAgendaPagamento();
		case "sindicalizado": return String.valueOf(emp.isSindicalizado());
		case "salario": return FormatoUtil.formatarMonetario(emp.getSalario());
		case "comissao":
			if (!(emp instanceof EmpregadoComissionado)) throw new ValorInvalidoException("Empregado nao eh comissionado.");
			return formatarMonetario(((EmpregadoComissionado) emp).getTaxaComissao());
		case "metodoPagamento": return emp.getMetodoPagamento();
		case "banco":
			if (emp.getMetodoPagamento() == null || !emp.getMetodoPagamento().equals("banco")) {
				throw new ValorInvalidoException("Empregado nao recebe em banco.");
			}
			return emp.getBanco();
		case "agencia":
			if (emp.getMetodoPagamento() == null || !emp.getMetodoPagamento().equals("banco")) {
				throw new ValorInvalidoException("Empregado nao recebe em banco.");
			}
			return emp.getAgencia();
		case "contaCorrente":
			if (emp.getMetodoPagamento() == null || !emp.getMetodoPagamento().equals("banco")) {
				throw new ValorInvalidoException("Empregado nao recebe em banco.");
			}
			return emp.getContaCorrente();
		case "idSindicato":
			if (!emp.isSindicalizado()) throw new ValorInvalidoException("Empregado nao eh sindicalizado.");
			return emp.getIdSindicato();
		case "taxaSindical":
			if (!emp.isSindicalizado()) throw new ValorInvalidoException("Empregado nao eh sindicalizado.");
			return formatarMonetario(emp.getTaxaSindical());
		default: throw new AtributoNaoExisteException();
		}
	}

	public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
		if (nome == null || nome.isEmpty()) throw new ValorInvalidoException("Nome nao pode ser nulo.");
		if (endereco == null || endereco.isEmpty()) throw new ValorInvalidoException("Endereco nao pode ser nulo.");
		if (tipo == null || tipo.isEmpty()) throw new ValorInvalidoException("Tipo nao pode ser nulo.");
		if (tipo.equals("comissionado")) throw new ValorInvalidoException("Tipo nao aplicavel.");
		if (!tipo.equals("horista") && !tipo.equals("assalariado")) throw new ValorInvalidoException("Tipo invalido.");
		if (salario == null || salario.isEmpty()) throw new ValorInvalidoException("Salario nao pode ser nulo.");

		double salarioConvertido;
		try { salarioConvertido = Double.parseDouble(salario.replace(",", ".")); } 
		catch (NumberFormatException e) { throw new ValorInvalidoException("Salario deve ser numerico."); }
		if (salarioConvertido < 0) throw new ValorInvalidoException("Salario deve ser nao-negativo.");

		registrarAlteracao.run();

		String idValido = empregados.gerarIdValido();
		Empregado emp = null;
		if (tipo.equals("horista")) emp = new EmpregadoHorista(idValido, nome, endereco, salarioConvertido);
		else if (tipo.equals("assalariado")) emp = new EmpregadoAssalariado(idValido, nome, endereco, salarioConvertido);

		empregados.adicionarEmpregado(emp);
		return idValido;
	}

	public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws Exception {
		if (nome == null || nome.isEmpty()) throw new ValorInvalidoException("Nome nao pode ser nulo.");
		if (endereco == null || endereco.isEmpty()) throw new ValorInvalidoException("Endereco nao pode ser nulo.");
		if (tipo == null || tipo.isEmpty()) throw new ValorInvalidoException("Tipo nao pode ser nulo.");
		if (tipo.equals("horista") || tipo.equals("assalariado")) throw new ValorInvalidoException("Tipo nao aplicavel.");
		if (!tipo.equals("comissionado")) throw new ValorInvalidoException("Tipo invalido.");
		if (salario == null || salario.isEmpty()) throw new ValorInvalidoException("Salario nao pode ser nulo.");
		if (comissao == null || comissao.isEmpty()) throw new ValorInvalidoException("Comissao nao pode ser nula.");

		double salarioConvertido;
		try { salarioConvertido = Double.parseDouble(salario.replace(",", ".")); } 
		catch (NumberFormatException e) { throw new ValorInvalidoException("Salario deve ser numerico."); }
		if (salarioConvertido < 0) throw new ValorInvalidoException("Salario deve ser nao-negativo.");

		double comissaoConvertida;
		try { comissaoConvertida = Double.parseDouble(comissao.replace(",", ".")); } 
		catch (NumberFormatException e) { throw new ValorInvalidoException("Comissao deve ser numerica."); }
		if (comissaoConvertida < 0) throw new ValorInvalidoException("Comissao deve ser nao-negativa.");

		registrarAlteracao.run();

		String idValido = empregados.gerarIdValido();
		Empregado emp = new EmpregadoComissionado(idValido, nome, endereco, salarioConvertido, comissaoConvertida);

		empregados.adicionarEmpregado(emp);
		return idValido;
	}

	public String getEmpregadoPorNome(String nome, int indice) throws Exception {
		Empregado emp = empregados.buscarEmpregadoPorNome(nome, indice);
		if (emp == null) throw new ValorInvalidoException("Nao ha empregado com esse nome.");
		return emp.getId();
	}

	public void removerEmpregado(String emp) throws Exception {
		if (emp == null || emp.isEmpty()) throw new IdentificacaoNulaException("Identificacao do empregado nao pode ser nula.");
		Empregado empRemover = empregados.getEmpregado(emp);
		if (empRemover == null) throw new EmpregadoNaoExisteException();
		
		registrarAlteracao.run();
		empregados.removerEmpregado(empRemover);
	}

	public void lancaCartao(String id, String data, String horas) throws Exception {
		if (id == null || id.isEmpty()) throw new IdentificacaoNulaException("Identificacao do empregado nao pode ser nula.");
		Empregado emp = empregados.getEmpregado(id);
		if (emp == null) throw new EmpregadoNaoExisteException();
		if (!(emp instanceof EmpregadoHorista)) throw new ValorInvalidoException("Empregado nao eh horista.");

		LocalDate dataConvertida = converterData(data, "Data invalida.");
		double horasConvertidas;
		try { horasConvertidas = Double.parseDouble(horas.replace(",", ".")); } 
		catch (NumberFormatException e) { throw new ValorInvalidoException("Horas devem ser numericas."); }
		if (horasConvertidas <= 0) throw new ValorInvalidoException("Horas devem ser positivas.");

		registrarAlteracao.run();
		CartaoDePonto cartao = new CartaoDePonto(dataConvertida, horasConvertidas);
		((EmpregadoHorista) emp).adicionarCartaoDePonto(cartao);
	}

	public String getHorasNormaisTrabalhadas(String id, String dataInicial, String dataFinal) throws Exception {
		Empregado emp = empregados.getEmpregado(id);
		if (emp == null) throw new EmpregadoNaoExisteException();
		if (!(emp instanceof EmpregadoHorista)) throw new ValorInvalidoException("Empregado nao eh horista.");

		LocalDate inicio = converterData(dataInicial, "Data inicial invalida.");
		LocalDate fim = converterData(dataFinal, "Data final invalida.");
		if (inicio.isAfter(fim)) throw new ValorInvalidoException("Data inicial nao pode ser posterior aa data final.");

		double horasNormais = ((EmpregadoHorista) emp).totalHorasNormaisEntre(inicio, fim);
		return formatarHoras(horasNormais);
	}

	public String getHorasExtrasTrabalhadas(String id, String dataInicial, String dataFinal) throws Exception {
		Empregado emp = empregados.getEmpregado(id);
		if (emp == null) throw new EmpregadoNaoExisteException();
		if (!(emp instanceof EmpregadoHorista)) throw new ValorInvalidoException("Empregado nao eh horista.");

		LocalDate inicio = converterData(dataInicial, "Data inicial invalida.");
		LocalDate fim = converterData(dataFinal, "Data final invalida.");
		if (inicio.isAfter(fim)) throw new ValorInvalidoException("Data inicial nao pode ser posterior aa data final.");

		double horasExtras = ((EmpregadoHorista) emp).totalHorasExtrasEntre(inicio, fim);
		return formatarHoras(horasExtras);
	}

	public void lancaVenda(String id, String data, String valor) throws Exception {
		if (id == null || id.isEmpty()) throw new IdentificacaoNulaException("Identificacao do empregado nao pode ser nula.");
		Empregado emp = empregados.getEmpregado(id);
		if (emp == null) throw new EmpregadoNaoExisteException();
		if (!(emp instanceof EmpregadoComissionado)) throw new ValorInvalidoException("Empregado nao eh comissionado.");

		LocalDate dataConvertida = converterData(data, "Data invalida.");
		double valorConvertido;
		try { valorConvertido = Double.parseDouble(valor.replace(",", ".")); } 
		catch (NumberFormatException e) { throw new ValorInvalidoException("Valor deve ser numerico."); }
		if (valorConvertido <= 0) throw new ValorInvalidoException("Valor deve ser positivo.");

		registrarAlteracao.run();
		ResultadoVenda venda = new ResultadoVenda(dataConvertida, valorConvertido);
		((EmpregadoComissionado) emp).adicionarVenda(venda);
	}

	public String getVendasRealizadas(String id, String dataInicial, String dataFinal) throws Exception {
		Empregado emp = empregados.getEmpregado(id);
		if (emp == null) throw new EmpregadoNaoExisteException();
		if (!(emp instanceof EmpregadoComissionado)) throw new ValorInvalidoException("Empregado nao eh comissionado.");

		LocalDate inicio = converterData(dataInicial, "Data inicial invalida.");
		LocalDate fim = converterData(dataFinal, "Data final invalida.");
		if (inicio.isAfter(fim)) throw new ValorInvalidoException("Data inicial nao pode ser posterior aa data final.");

		double totalVendas = ((EmpregadoComissionado) emp).totalVendasEntre(inicio, fim);
		return formatarMonetario(totalVendas);
	}

	public void lancaTaxaServico(String idSindicato, String data, String valor) throws Exception {
		if (idSindicato == null || idSindicato.isEmpty()) throw new IdentificacaoNulaException("Identificacao do membro nao pode ser nula.");
		Empregado emp = empregados.getEmpregadoPorSindicato(idSindicato);
		if (emp == null) throw new ValorInvalidoException("Membro nao existe.");

		LocalDate dataConvertida = converterData(data, "Data invalida.");
		double valorConvertido;
		try { valorConvertido = Double.parseDouble(valor.replace(",", ".")); } 
		catch (NumberFormatException e) { throw new ValorInvalidoException("Valor deve ser numerico."); }
		if (valorConvertido <= 0) throw new ValorInvalidoException("Valor deve ser positivo.");

		registrarAlteracao.run();
		TaxaServico taxa = new TaxaServico(dataConvertida, valorConvertido);
		emp.adicionarTaxaServico(taxa);
	}

	public String getTaxasServico(String id, String dataInicial, String dataFinal) throws Exception {
		Empregado emp = empregados.getEmpregado(id);
		if (emp == null) throw new EmpregadoNaoExisteException();
		if (!emp.isSindicalizado()) throw new ValorInvalidoException("Empregado nao eh sindicalizado.");

		LocalDate inicio = converterData(dataInicial, "Data inicial invalida.");
		LocalDate fim = converterData(dataFinal, "Data final invalida.");
		if (inicio.isAfter(fim)) throw new ValorInvalidoException("Data inicial nao pode ser posterior aa data final.");

		double totalTaxas = emp.totalTaxasServicoEntre(inicio, fim);
		return formatarMonetario(totalTaxas);
	}

	public void alteraEmpregado(String id, String atributo, String valor) throws Exception {
		if (id == null || id.isEmpty()) throw new IdentificacaoNulaException("Identificacao do empregado nao pode ser nula.");
		Empregado emp = empregados.getEmpregado(id);
		if (emp == null) throw new EmpregadoNaoExisteException();

		switch (atributo) {
		case "nome":
			if (valor == null || valor.isEmpty()) throw new ValorInvalidoException("Nome nao pode ser nulo.");
			registrarAlteracao.run();
			emp.setNome(valor);
			break;
		case "endereco":
			if (valor == null || valor.isEmpty()) throw new ValorInvalidoException("Endereco nao pode ser nulo.");
			registrarAlteracao.run();
			emp.setEndereco(valor);
			break;
		case "tipo":
			if (!valor.equals("horista") && !valor.equals("assalariado") && !valor.equals("comissionado")) {
				throw new ValorInvalidoException("Tipo invalido.");
			}
			// Obter salário existente para preservar em vez de zerar
			double salarioAtual = 0.0;
			if (emp instanceof EmpregadoHorista) salarioAtual = ((EmpregadoHorista) emp).getSalarioPorHora();
			else if (emp instanceof EmpregadoAssalariado) salarioAtual = ((EmpregadoAssalariado) emp).getSalarioMensal();

			Empregado novoEmp;
			if (valor.equals("horista")) {
				novoEmp = new EmpregadoHorista(emp.getId(), emp.getNome(), emp.getEndereco(), salarioAtual);
			} else if (valor.equals("assalariado")) {
				novoEmp = new EmpregadoAssalariado(emp.getId(), emp.getNome(), emp.getEndereco(), salarioAtual);
			} else {
				novoEmp = new EmpregadoComissionado(emp.getId(), emp.getNome(), emp.getEndereco(), salarioAtual, 0.0);
			}

			emp.copiarAtributosPara(novoEmp);

			registrarAlteracao.run();
			empregados.removerEmpregado(emp);
			empregados.adicionarEmpregado(novoEmp);
			break;
		case "salario":
			if (valor == null || valor.isEmpty()) throw new ValorInvalidoException("Salario nao pode ser nulo.");
			double novoSalario;
			try { novoSalario = Double.parseDouble(valor.replace(",", ".")); } 
			catch (NumberFormatException e) { throw new ValorInvalidoException("Salario deve ser numerico."); }
			if (novoSalario < 0) throw new ValorInvalidoException("Salario deve ser nao-negativo.");
			registrarAlteracao.run();
			emp.setSalario(novoSalario);
			break;
		case "comissao":
			if (valor == null || valor.isEmpty()) throw new ValorInvalidoException("Comissao nao pode ser nula.");
			double novaComissao;
			try { novaComissao = Double.parseDouble(valor.replace(",", ".")); } 
			catch (NumberFormatException e) { throw new ValorInvalidoException("Comissao deve ser numerica."); }
			if (novaComissao < 0) throw new ValorInvalidoException("Comissao deve ser nao-negativa.");
			if (!(emp instanceof EmpregadoComissionado)) throw new ValorInvalidoException("Empregado nao eh comissionado.");
			registrarAlteracao.run();
			((EmpregadoComissionado) emp).setTaxaComissao(novaComissao);
			break;
		case "metodoPagamento":
			if (!valor.equals("emMaos") && !valor.equals("correios") && !valor.equals("banco")) {
				throw new ValorInvalidoException("Metodo de pagamento invalido.");
			}
			if (valor.equals("banco")) {
				if (emp.getBanco() == null || emp.getBanco().isEmpty()) {
					throw new ValorInvalidoException("Metodo de pagamento invalido.");
				}
			}
			registrarAlteracao.run();
			emp.setMetodoPagamento(valor);
			break;
		case "sindicalizado":
			if (!valor.equals("true") && !valor.equals("false")) throw new ValorInvalidoException("Valor deve ser true ou false.");
			registrarAlteracao.run();
			boolean status = Boolean.parseBoolean(valor);
			emp.setSindicalizado(status);
			if (!status) {
				emp.setIdSindicato(null);
				emp.setTaxaSindical(0);
			}
			break;
		case "agendaPagamento":
			if (agendas == null || !agendas.isDisponivel(valor)) {
				throw new ValorInvalidoException("Agenda de pagamento nao esta disponivel");
			}
			registrarAlteracao.run();
			emp.setAgendaPagamento(valor);
			break;
		default: throw new AtributoNaoExisteException();
		}
	}

	public void alteraEmpregado(String id, String atributo, String valor, String valorExtra) throws Exception {
		if (id == null || id.isEmpty()) throw new IdentificacaoNulaException("Identificacao do empregado nao pode ser nula.");
		Empregado emp = empregados.getEmpregado(id);
		if (emp == null) throw new EmpregadoNaoExisteException();

		if (!"tipo".equals(atributo)) {
			throw new AtributoNaoExisteException();
		}

		if (!"horista".equals(valor) && !"assalariado".equals(valor) && !"comissionado".equals(valor)) {
			throw new ValorInvalidoException("Tipo invalido.");
		}

		if (valorExtra == null || valorExtra.isEmpty()) {
			if ("comissionado".equals(valor)) throw new ValorInvalidoException("Comissao nao pode ser nula.");
			throw new ValorInvalidoException("Salario nao pode ser nulo.");
		}

		double valorNumerico;
		try {
			valorNumerico = Double.parseDouble(valorExtra.replace(",", "."));
		} catch (NumberFormatException e) {
			if ("comissionado".equals(valor)) throw new ValorInvalidoException("Comissao deve ser numerica.");
			throw new ValorInvalidoException("Salario deve ser numerico.");
		}

		if (valorNumerico < 0) {
			if ("comissionado".equals(valor)) throw new ValorInvalidoException("Comissao deve ser nao-negativa.");
			throw new ValorInvalidoException("Salario deve ser nao-negativo.");
		}

		// Instanciar novo empregado antes de alterar o repositório
		Empregado novoEmp;
		if ("horista".equals(valor)) {
			novoEmp = new EmpregadoHorista(emp.getId(), emp.getNome(), emp.getEndereco(), valorNumerico);
		} else if ("assalariado".equals(valor)) {
			novoEmp = new EmpregadoAssalariado(emp.getId(), emp.getNome(), emp.getEndereco(), valorNumerico);
		} else {
			double salBase = 0.0;
			if (emp instanceof EmpregadoAssalariado) salBase = ((EmpregadoAssalariado) emp).getSalarioMensal();
			else if (emp instanceof EmpregadoHorista) salBase = ((EmpregadoHorista) emp).getSalarioPorHora();
			novoEmp = new EmpregadoComissionado(emp.getId(), emp.getNome(), emp.getEndereco(), salBase, valorNumerico);
		}

		emp.copiarAtributosPara(novoEmp);

		registrarAlteracao.run();
		empregados.removerEmpregado(emp);
		empregados.adicionarEmpregado(novoEmp);
	}

	public void alteraEmpregado(String id, String atributo, String valor, String idSindicato, String taxaSindical) throws Exception {
		if (id == null || id.isEmpty()) throw new IdentificacaoNulaException("Identificacao do empregado nao pode ser nula.");
		Empregado emp = empregados.getEmpregado(id);
		if (emp == null) throw new EmpregadoNaoExisteException();

		if (!"sindicalizado".equals(atributo)) {
			throw new AtributoNaoExisteException();
		}

		if (!"true".equals(valor) && !"false".equals(valor)) {
			throw new ValorInvalidoException("Valor deve ser true ou false.");
		}

		if ("true".equals(valor)) {
			if (idSindicato == null || idSindicato.isEmpty()) throw new ValorInvalidoException("Identificacao do sindicato nao pode ser nula.");
			if (taxaSindical == null || taxaSindical.isEmpty()) throw new ValorInvalidoException("Taxa sindical nao pode ser nula.");

			double taxa;
			try { taxa = Double.parseDouble(taxaSindical.replace(",", ".")); } 
			catch (NumberFormatException e) { throw new ValorInvalidoException("Taxa sindical deve ser numerica."); }
			if (taxa < 0) throw new ValorInvalidoException("Taxa sindical deve ser nao-negativa.");

			Empregado outroEmp = empregados.getEmpregadoPorSindicato(idSindicato);
			if (outroEmp != null && !outroEmp.getId().equals(id)) {
				throw new ValorInvalidoException("Ha outro empregado com esta identificacao de sindicato");
			}

			registrarAlteracao.run();
			emp.setSindicalizado(true);
			emp.setIdSindicato(idSindicato);
			emp.setTaxaSindical(taxa);
		} else {
			registrarAlteracao.run();
			emp.setSindicalizado(false);
			emp.setIdSindicato(null);
			emp.setTaxaSindical(0);
		}
	}

	public void alteraEmpregado(String id, String atributo, String valor, String banco, String agencia, String contaCorrente) throws Exception {
		if (id == null || id.isEmpty()) throw new IdentificacaoNulaException("Identificacao do empregado nao pode ser nula.");
		Empregado emp = empregados.getEmpregado(id);
		if (emp == null) throw new EmpregadoNaoExisteException();

		if (!"metodoPagamento".equals(atributo)) {
			throw new AtributoNaoExisteException();
		}

		if (!"banco".equals(valor)) {
			throw new ValorInvalidoException("Metodo de pagamento invalido.");
		}

		if (banco == null || banco.isEmpty()) throw new ValorInvalidoException("Banco nao pode ser nulo.");
		if (agencia == null || agencia.isEmpty()) throw new ValorInvalidoException("Agencia nao pode ser nulo.");
		if (contaCorrente == null || contaCorrente.isEmpty()) throw new ValorInvalidoException("Conta corrente nao pode ser nulo.");

		registrarAlteracao.run();
		emp.setMetodoPagamento(valor);
		emp.setBanco(banco);
		emp.setAgencia(agencia);
		emp.setContaCorrente(contaCorrente);
	}

	private LocalDate converterData(String data, String mensagemErro) throws ValorInvalidoException {
		return FormatoUtil.converterData(data, mensagemErro);
	}

	private String formatarMonetario(double valor) {
		return FormatoUtil.formatarMonetario(valor);
	}

	private String formatarHoras(double valor) {
		return FormatoUtil.formatarHoras(valor);
	}
}

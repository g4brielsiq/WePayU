package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.EmpregadoAssalariado;
import br.ufal.ic.p2.wepayu.models.EmpregadoComissionado;
import br.ufal.ic.p2.wepayu.models.EmpregadoHorista;

public class Facade2 {

	private Map<String, Empregado> empregados;
	private int proximoId;

	public Facade2() {
		this.empregados = new HashMap<>();
		this.proximoId = 1;
	}

	public void zerarSistema() {
		this.empregados.clear();
		this.proximoId = 1;
	}

	public void encerrarSistema() {
		// Apenas para não dar erro de "Unknown command" no final dos testes
	}	
	
	// Método de criação para Horistas e Assalariados (4 parâmetros)
	public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
		if (nome == null || nome.isEmpty()) throw new Exception("Nome nao pode ser nulo.");
		if (endereco == null || endereco.isEmpty()) throw new Exception("Endereco nao pode ser nulo.");
		if (tipo == null || (!tipo.equals("horista") && !tipo.equals("assalariado") && !tipo.equals("comissionado"))) {
			throw new Exception("Tipo invalido.");
		}
		if (tipo.equals("comissionado")) {
			throw new Exception("Tipo nao aplicavel."); // Comissionado exige o método com 5 parâmetros
		}
		if (salario == null || salario.isEmpty()) throw new Exception("Salario nao pode ser nulo.");

		double salarioDouble;
		try {
			salarioDouble = Double.parseDouble(salario.replace(",", "."));
		} catch (NumberFormatException e) {
			throw new Exception("Salario deve ser numerico.");
		}
		if (salarioDouble < 0) throw new Exception("Salario deve ser nao-negativo.");

		String idString = String.valueOf(this.proximoId);

		if (tipo.equals("horista")) {
			EmpregadoHorista emp = new EmpregadoHorista(idString, nome, endereco, salarioDouble);
			empregados.put(idString, emp);
		} else if (tipo.equals("assalariado")) {
			EmpregadoAssalariado emp = new EmpregadoAssalariado(idString, nome, endereco, salarioDouble);
			empregados.put(idString, emp);
		}

		this.proximoId++;
		return idString;
	}

	// Método de criação específico para Comissionados (5 parâmetros)
	public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws Exception {
		if (nome == null || nome.isEmpty()) throw new Exception("Nome nao pode ser nulo.");
		if (endereco == null || endereco.isEmpty()) throw new Exception("Endereco nao pode ser nulo.");
		if (!tipo.equals("comissionado")) throw new Exception("Tipo nao aplicavel.");

		double salarioDouble;
		try {
			salarioDouble = Double.parseDouble(salario.replace(",", "."));
		} catch (NumberFormatException e) {
			throw new Exception("Salario deve ser numerico.");
		}
		if (salarioDouble < 0) throw new Exception("Salario deve ser nao-negativo.");

		if (comissao == null || comissao.isEmpty()) throw new Exception("Comissao nao pode ser nula.");

		double comissaoDouble;
		try {
			comissaoDouble = Double.parseDouble(comissao.replace(",", "."));
		} catch (NumberFormatException e) {
			throw new Exception("Comissao deve ser numerica.");
		}
		if (comissaoDouble < 0) throw new Exception("Comissao deve ser nao-negativa.");

		String idString = String.valueOf(this.proximoId);

		EmpregadoComissionado emp = new EmpregadoComissionado(idString, nome, endereco, salarioDouble, comissaoDouble);
		empregados.put(idString, emp);

		this.proximoId++;
		return idString;
	}

	// O EasyAccept usa este método para verificar se salvamos tudo certinho
	public String getAtributoEmpregado(String emp, String atributo) throws Exception {
		if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");

		Empregado empregado = empregados.get(emp);
		if (empregado == null) throw new Exception("Empregado nao existe.");

		switch (atributo) {
		case "nome":
			return empregado.getNome();
		case "endereco":
			return empregado.getEndereco();
		case "tipo":
			return empregado.getTipo();
		case "sindicalizado":
			return String.valueOf(empregado.isSindicalizado());
			// Formatamos a saída do Double para usar vírgula e ter duas casas decimais, conforme o teste exige
		case "salario":
			if (empregado instanceof EmpregadoHorista) {
				return String.format("%.2f", ((EmpregadoHorista) empregado).getSalarioPorHora()).replace(".", ",");
			} else if (empregado instanceof EmpregadoAssalariado) {
				return String.format("%.2f", ((EmpregadoAssalariado) empregado).getSalarioMensal()).replace(".", ",");
			}
		case "comissao":
			if (empregado instanceof EmpregadoComissionado) {
				return String.format("%.2f", ((EmpregadoComissionado) empregado).getTaxaComissao()).replace(".", ",");
			}
			throw new Exception("Empregado nao eh comissionado.");
		default:
			throw new Exception("Atributo nao existe.");
		}
	}
}
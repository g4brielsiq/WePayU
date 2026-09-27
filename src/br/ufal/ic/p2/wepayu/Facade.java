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
import br.ufal.ic.p2.wepayu.models.RepositoryEmpregados;

public class Facade {

	RepositoryEmpregados listaEmpregados = new RepositoryEmpregados();

	public void zerarSistema() {

		listaEmpregados.zerarSistema();
	}

	public String getAtributoEmpregado(String id, String atributo) throws Exception {

		if (id == null || id.isEmpty())
			throw new Exception("Identificacao do empregado nao pode ser nula.");

		Empregado emp = listaEmpregados.getIdEmpregado(id);

		// Verifica os parametros
		if (emp == null) {

			throw new Exception("Empregado nao existe.");
		}

		// Retorna os atributos solicitados no parametro
		switch (atributo) {

		case "nome": return emp.getNome();

		case "endereco": return emp.getEndereco();

		case "tipo": return emp.getTipo();

		case "sindicalizado": return String.valueOf(emp.isSindicalizado());

		case "salario":

			if (emp instanceof EmpregadoHorista) {

				double salario = ((EmpregadoHorista) emp).getSalarioPorHora();

				return String.format("%.2f", salario).replace(".", ",");
			}

			else if (emp instanceof EmpregadoAssalariado) {

				double salario = ((EmpregadoAssalariado) emp).getSalarioMensal();

				return String.format("%.2f", salario).replace(".", ",");
			}

			else if (emp instanceof EmpregadoComissionado) {

				double salario = ((EmpregadoComissionado) emp).getSalarioMensal();

				return String.format("%.2f", salario).replace(".", ",");
			}

		case "comissao" :

			if (emp instanceof EmpregadoComissionado) {

				double comissao = ((EmpregadoComissionado) emp).getTaxaComissao();

				return String.format("%.2f", comissao).replace(".", ",");
			}

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

	public void encerrarSistema() {

	}
}






























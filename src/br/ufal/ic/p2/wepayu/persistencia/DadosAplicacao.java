package br.ufal.ic.p2.wepayu.persistencia;

import java.io.Serializable;
import java.util.ArrayList;

import br.ufal.ic.p2.wepayu.entidades.Empregado;
import br.ufal.ic.p2.wepayu.repositorio.RepositoryPagamentos;

/**
 * Encapsula os dados da aplicação que devem ser persistidos em XML entre execuções.
 */
public class DadosAplicacao implements Serializable {
	private static final long serialVersionUID = 1L;

	private ArrayList<Empregado> empregados = new ArrayList<>();
	private int idValido = 1;
	private RepositoryPagamentos pagamentos = new RepositoryPagamentos();
	private ArrayList<String> agendasDisponiveis = new ArrayList<>();

	public DadosAplicacao() {
	}

	public ArrayList<Empregado> getEmpregados() {
		return empregados;
	}

	public void setEmpregados(ArrayList<Empregado> empregados) {
		this.empregados = empregados != null ? empregados : new ArrayList<>();
	}

	public int getIdValido() {
		return idValido;
	}

	public void setIdValido(int idValido) {
		this.idValido = idValido;
	}

	public RepositoryPagamentos getPagamentos() {
		return pagamentos;
	}

	public void setPagamentos(RepositoryPagamentos pagamentos) {
		this.pagamentos = pagamentos != null ? pagamentos : new RepositoryPagamentos();
	}

	public ArrayList<String> getAgendasDisponiveis() {
		return agendasDisponiveis;
	}

	public void setAgendasDisponiveis(ArrayList<String> agendasDisponiveis) {
		this.agendasDisponiveis = agendasDisponiveis != null ? agendasDisponiveis : new ArrayList<>();
	}
}

package br.ufal.ic.p2.wepayu.repositorio;

import br.ufal.ic.p2.wepayu.entidades.Empregado;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RepositoryEmpregados implements Serializable {

	private ArrayList<Empregado> listaEmpregados = new ArrayList<>();
	private int idValido = 1;

	public RepositoryEmpregados() {
	}

	public List<Empregado> getListaEmpregados() {
		return Collections.unmodifiableList(listaEmpregados);
	}

	public ArrayList<Empregado> getListaEmpregadosInterna() {
		return listaEmpregados;
	}

	public void setListaEmpregadosInterna(ArrayList<Empregado> lista) {
		this.listaEmpregados = lista != null ? lista : new ArrayList<>();
	}

	public int getIdValido() {
		return idValido;
	}

	public void setIdValido(int idValido) {
		this.idValido = idValido;
	}

	public int getNumeroDeEmpregados() {
		return listaEmpregados.size();
	}

	public void zerarSistema() {
		listaEmpregados.clear();
		idValido = 1;
	}

	public RepositoryEmpregados copia() {
		RepositoryEmpregados clone = new RepositoryEmpregados();
		clone.idValido = idValido;
		for (Empregado empregado : listaEmpregados) {
			clone.adicionarEmpregado(empregado.copia());
		}
		return clone;
	}

	public String gerarIdValido() {
		String idGerado = String.valueOf(this.idValido);
		this.idValido++;
		return idGerado;
	}

	public void adicionarEmpregado(Empregado emp) {
		this.listaEmpregados.add(emp);
	}

	public Empregado getEmpregado(String id) {
		for (Empregado emp : listaEmpregados) {
			if (emp.getId() != null && emp.getId().equals(id)) {
				return emp;
			}
		}
		return null;
	}

	public Empregado getIdEmpregado(String id) {
		return getEmpregado(id);
	}

	public Empregado buscarEmpregadoPorNome(String nome, int indice) {
		int cont = 0;
		for (Empregado emp : listaEmpregados) {
			if (emp.getNome() != null && emp.getNome().equals(nome)) {
				cont++;
				if (cont == indice) {
					return emp;
				}
			}
		}
		return null;
	}

	public void removerEmpregado(Empregado emp) {
		listaEmpregados.remove(emp);
	}

	public Empregado getEmpregadoPorSindicato(String idSindicato) {
		for (Empregado emp : listaEmpregados) {
			if (emp.isSindicalizado() && emp.getIdSindicato() != null && emp.getIdSindicato().equals(idSindicato)) {
				return emp;
			}
		}
		return null;
	}
}

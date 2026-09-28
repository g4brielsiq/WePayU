package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RepositoryEmpregados implements Serializable {

	private ArrayList<Empregado> listaEmpregados;
	private int idValido = 1;

	public RepositoryEmpregados() {

		this.listaEmpregados = new ArrayList<Empregado>();
	}

	public List<Empregado> getListaEmpregados() {
		return Collections.unmodifiableList(listaEmpregados);
	}

	public int getNumeroDeEmpregados() {
		return listaEmpregados.size();
	}

	public int getIdValido() {
		return idValido;
	}

	public void setIdValido(int idValido) {
		this.idValido = idValido;
	}

	public void zerarSistema() {

		listaEmpregados.clear();
	}

	public void encerrarSistema() {
		// Apenas para não dar erro de "Unknown command" no final dos testes
	}

	public RepositoryEmpregados copia() {
		RepositoryEmpregados clone = new RepositoryEmpregados();
		clone.setIdValido(idValido);
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

	public Empregado getIdEmpregado(String id) {	

		for (int i = 0; i < listaEmpregados.size(); i++) {

			if (listaEmpregados.get(i).getId().equals(id)) {

				return listaEmpregados.get(i);
			}
		}

		return null;
	}

	public Empregado buscarEmpregadoPorNome(String nome, int indice) {

		int cont = 0;

		for (int i = 0; i < listaEmpregados.size(); i++) {

			if(listaEmpregados.get(i).getNome().equals(nome)) {

				cont++;

				if(cont == indice) {

					return listaEmpregados.get(i);
				}
			}
		}

		return null;
	}
	
	public void removerEmpregado(Empregado emp) {
		
	    listaEmpregados.remove(emp);
	}
	
	public Empregado getEmpregadoPorSindicato(String idSindicato) {
	    
	    for (int i = 0; i < listaEmpregados.size(); i++) {
	        
	        Empregado emp = listaEmpregados.get(i);
	        
	        // Verifica se o empregado está no sindicato e se o ID sindical é igual ao procurado
	        if (emp.isSindicalizado() && emp.getIdSindicato() != null && emp.getIdSindicato().equals(idSindicato)) {
	            
	            return emp;
	        }
	    }
	   
	    return null;
	}
}















package br.ufal.ic.p2.wepayu.entidades;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import br.ufal.ic.p2.wepayu.repositorio.RepositoryCartoesDePonto;

public class EmpregadoHorista extends Empregado implements Serializable {

	private double salarioPorHora;
	private RepositoryCartoesDePonto cartoesDePonto;

	public EmpregadoHorista(String id, String nome, String endereco, double salarioPorHora) {
	   
	    super(id, nome, endereco, "horista");
	    
	    this.salarioPorHora = salarioPorHora;
	    
	    this.cartoesDePonto = new RepositoryCartoesDePonto();
	}

	public double getSalarioPorHora() {
		return salarioPorHora;
	}

	public void setSalarioPorHora(double salarioPorHora) {
		this.salarioPorHora = salarioPorHora;
	}

	public List<CartaoDePonto> getCartoesDePonto() {
		return cartoesDePonto.getCartoes();
	}
	
	public void adicionarCartaoDePonto(CartaoDePonto cartao) {
		cartoesDePonto.adicionar(cartao);
	}

	public double totalHorasNormaisEntre(LocalDate inicio, LocalDate fim) {
		return cartoesDePonto.totalHorasNormais(inicio, fim);
	}

	public double totalHorasExtrasEntre(LocalDate inicio, LocalDate fim) {
		return cartoesDePonto.totalHorasExtras(inicio, fim);
	}

	@Override
	public EmpregadoHorista copia() {
		EmpregadoHorista copia = new EmpregadoHorista(getId(), getNome(), getEndereco(), salarioPorHora);
		copiarAtributosPara(copia);
		copia.cartoesDePonto = cartoesDePonto.copia();
		return copia;
	}
}

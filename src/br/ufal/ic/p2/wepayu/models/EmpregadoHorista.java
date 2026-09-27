package br.ufal.ic.p2.wepayu.models;

import java.util.ArrayList;

public class EmpregadoHorista extends Empregado{

	private double salarioPorHora;
	private ArrayList<CartaoDePonto> cartoesDePonto;

	public EmpregadoHorista(String id, String nome, String endereco, double salarioPorHora) {
	   
	    super(id, nome, endereco, "horista");
	    
	    this.salarioPorHora = salarioPorHora;
	    
	    this.cartoesDePonto = new ArrayList<CartaoDePonto>();
	}

	public double getSalarioPorHora() {
		return salarioPorHora;
	}

	public void setSalarioPorHora(double salarioPorHora) {
		this.salarioPorHora = salarioPorHora;
	}

	public ArrayList<CartaoDePonto> getCartoesDePonto() {
		return cartoesDePonto;
	}

	public void setCartoesDePonto(ArrayList<CartaoDePonto> cartoesDePonto) {
		this.cartoesDePonto = cartoesDePonto;
	}
	
	public void adicionarCartaoDePonto(CartaoDePonto cartao) {
		
	    this.cartoesDePonto.add(cartao);
	}
}
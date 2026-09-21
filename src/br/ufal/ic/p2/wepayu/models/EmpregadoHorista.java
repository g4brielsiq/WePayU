package br.ufal.ic.p2.wepayu.models;

import java.util.ArrayList;

public class EmpregadoHorista extends Empregado{

	private double salarioPorHora;
	private ArrayList<CartaoDePonto> cartaoDePonto;

	public EmpregadoHorista(String id, String nome, String endereco, double salarioPorHora) {
	   
	    super(id, nome, endereco, "horista");
	    
	    this.salarioPorHora = salarioPorHora;
	    
	    this.cartaoDePonto = new ArrayList<CartaoDePonto>();
	}

	public double getSalarioPorHora() {
		return salarioPorHora;
	}

	public void setSalarioPorHora(double salarioPorHora) {
		this.salarioPorHora = salarioPorHora;
	}

	public ArrayList<CartaoDePonto> getCartaoDePonto() {
		return cartaoDePonto;
	}

	public void setCartaoDePonto(ArrayList<CartaoDePonto> cartaoDePonto) {
		this.cartaoDePonto = cartaoDePonto;
	}
}
package br.ufal.ic.p2.wepayu.entidades;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmpregadoHorista extends Empregado implements Serializable {

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

	public List<CartaoDePonto> getCartoesDePonto() {
		return Collections.unmodifiableList(cartoesDePonto);
	}
	
	public void adicionarCartaoDePonto(CartaoDePonto cartao) {
		
	    this.cartoesDePonto.add(cartao);
	}

	@Override
	public EmpregadoHorista copia() {
		EmpregadoHorista copia = new EmpregadoHorista(getId(), getNome(), getEndereco(), salarioPorHora);
		copiarAtributosPara(copia);
		for (CartaoDePonto cartao : cartoesDePonto) {
			copia.adicionarCartaoDePonto(new CartaoDePonto(cartao.getData(), cartao.getHoras()));
		}
		return copia;
	}
}

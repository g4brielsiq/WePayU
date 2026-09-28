package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;

public class EmpregadoAssalariado extends Empregado implements Serializable {

	private double salarioMensal;

	public EmpregadoAssalariado(String id, String nome, String endereco, double salarioMensal) {
		
		super(id, nome, endereco, "assalariado");
		
		this.salarioMensal = salarioMensal;
	}

	public double getSalarioMensal() {
		return salarioMensal;
	}

	public void setSalarioMensal(double salarioMensal) {
		this.salarioMensal = salarioMensal;
	}

	@Override
	public EmpregadoAssalariado copia() {
		EmpregadoAssalariado copia = new EmpregadoAssalariado(getId(), getNome(), getEndereco(), salarioMensal);
		copiarAtributosPara(copia);
		return copia;
	}
}

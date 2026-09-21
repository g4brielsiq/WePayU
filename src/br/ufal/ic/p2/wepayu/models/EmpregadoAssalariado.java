package br.ufal.ic.p2.wepayu.models;

public class EmpregadoAssalariado extends Empregado{

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
}
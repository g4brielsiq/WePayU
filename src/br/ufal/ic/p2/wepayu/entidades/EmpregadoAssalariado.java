package br.ufal.ic.p2.wepayu.entidades;

import java.io.Serializable;
import java.time.LocalDate;

import br.ufal.ic.p2.wepayu.repositorio.RepositoryAgendas;
import br.ufal.ic.p2.wepayu.util.FormatoUtil;

public class EmpregadoAssalariado extends Empregado implements Serializable {
	private static final long serialVersionUID = 1L;

	private double salarioMensal;

	public EmpregadoAssalariado() {
		super(null, null, null, "assalariado");
	}

	public EmpregadoAssalariado(String id, String nome, String endereco, double salarioMensal) {
		this(id, nome, endereco, salarioMensal, "assalariado");
	}

	public EmpregadoAssalariado(String id, String nome, String endereco, double salarioMensal, String tipo) {
		super(id, nome, endereco, tipo);
		this.salarioMensal = salarioMensal;
	}

	public double getSalarioMensal() {
		return salarioMensal;
	}

	public void setSalarioMensal(double salarioMensal) {
		this.salarioMensal = salarioMensal;
	}

	@Override
	public double getSalario() {
		return salarioMensal;
	}

	@Override
	public void setSalario(double salario) {
		this.salarioMensal = salario;
	}

	public double calcularSalarioFixo() {
		String agenda = getAgendaPagamento();
		if (agenda.startsWith("mensal")) {
			return salarioMensal;
		}
		int k = RepositoryAgendas.getSemanas(agenda);
		return FormatoUtil.truncar((salarioMensal * 12.0 * k) / 52.0);
	}

	@Override
	public double calcularSalarioBruto(LocalDate dataFolha, LocalDate inicioPeriodo) {
		return calcularSalarioFixo();
	}

	@Override
	public EmpregadoAssalariado copia() {
		EmpregadoAssalariado copia = new EmpregadoAssalariado(getId(), getNome(), getEndereco(), salarioMensal);
		copiarAtributosPara(copia);
		return copia;
	}
}

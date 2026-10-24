package br.ufal.ic.p2.wepayu.entidades;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import br.ufal.ic.p2.wepayu.repositorio.RepositoryCartoesDePonto;

public class EmpregadoHorista extends Empregado implements Serializable {
	private static final long serialVersionUID = 1L;

	private double salarioPorHora;
	private RepositoryCartoesDePonto cartoesDePonto = new RepositoryCartoesDePonto();

	public EmpregadoHorista() {
		super(null, null, null, "horista");
	}

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

	public RepositoryCartoesDePonto getCartoesRepositorio() {
		return cartoesDePonto;
	}

	public void setCartoesRepositorio(RepositoryCartoesDePonto cartoesDePonto) {
		this.cartoesDePonto = cartoesDePonto != null ? cartoesDePonto : new RepositoryCartoesDePonto();
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
	public double getSalario() {
		return salarioPorHora;
	}

	@Override
	public void setSalario(double salario) {
		this.salarioPorHora = salario;
	}

	public double[] obterHorasTrabalhadas(LocalDate inicioPeriodo, LocalDate dataFolha) {
		double horasNormais = 0.0;
		double horasExtras = 0.0;
		for (CartaoDePonto cartao : getCartoesDePonto()) {
			if (!cartao.getData().isAfter(dataFolha) && cartao.getData().isAfter(inicioPeriodo)) {
				horasNormais += Math.min(8.0, cartao.getHoras());
				if (cartao.getHoras() > 8.0) {
					horasExtras += (cartao.getHoras() - 8.0);
				}
			}
		}
		return new double[]{horasNormais, horasExtras};
	}

	@Override
	public double calcularSalarioBruto(LocalDate dataFolha, LocalDate inicioPeriodo) {
		double[] horas = obterHorasTrabalhadas(inicioPeriodo, dataFolha);
		return (horas[0] * salarioPorHora) + (horas[1] * salarioPorHora * 1.5);
	}

	@Override
	public EmpregadoHorista copia() {
		EmpregadoHorista copia = new EmpregadoHorista(getId(), getNome(), getEndereco(), salarioPorHora);
		copiarAtributosPara(copia);
		copia.cartoesDePonto = cartoesDePonto.copia();
		return copia;
	}
}

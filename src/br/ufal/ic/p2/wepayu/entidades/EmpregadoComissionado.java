package br.ufal.ic.p2.wepayu.entidades;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import br.ufal.ic.p2.wepayu.repositorio.RepositoryVendas;

public class EmpregadoComissionado extends EmpregadoAssalariado implements Serializable {
	private static final long serialVersionUID = 1L;

	private double taxaComissao;
	private RepositoryVendas historicoVendas = new RepositoryVendas();

	public EmpregadoComissionado() {
		super(null, null, null, 0.0, "comissionado");
	}

	public EmpregadoComissionado(String id, String nome, String endereco, double salarioMensal, double taxaComissao) {
		super(id, nome, endereco, salarioMensal, "comissionado");
		this.taxaComissao = taxaComissao;
		this.historicoVendas = new RepositoryVendas();
	}

	public double getTaxaComissao() {
		return taxaComissao;
	}

	public void setTaxaComissao(double taxaComissao) {
		this.taxaComissao = taxaComissao;
	}

	public List<ResultadoVenda> getHistoricoVendas() {
		return historicoVendas.getVendas();
	}

	public RepositoryVendas getVendasRepositorio() {
		return historicoVendas;
	}

	public void setVendasRepositorio(RepositoryVendas historicoVendas) {
		this.historicoVendas = historicoVendas != null ? historicoVendas : new RepositoryVendas();
	}

	public void adicionarVenda(ResultadoVenda venda) {
		historicoVendas.adicionar(venda);
	}

	public double totalVendasEntre(LocalDate inicio, LocalDate fim) {
		return historicoVendas.totalEntre(inicio, fim);
	}

	@Override
	public EmpregadoComissionado copia() {
		EmpregadoComissionado copia = new EmpregadoComissionado(
				getId(), getNome(), getEndereco(), getSalarioMensal(), taxaComissao);
		copiarAtributosPara(copia);
		copia.historicoVendas = historicoVendas.copia();
		return copia;
	}
}

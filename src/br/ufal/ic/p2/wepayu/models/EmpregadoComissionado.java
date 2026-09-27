package br.ufal.ic.p2.wepayu.models;

import java.util.ArrayList;

public class EmpregadoComissionado extends EmpregadoAssalariado {

	private double taxaComissao;
	private ArrayList<ResultadoVenda> historicoVendas;

	public EmpregadoComissionado(String id, String nome, String endereco, double salarioMensal, double taxaComissao) {

		super(id, nome, endereco, salarioMensal);

		this.setTipo("comissionado");

		this.taxaComissao = taxaComissao;

		this.historicoVendas = new ArrayList<ResultadoVenda>();
	}

	public double getTaxaComissao() {
		return taxaComissao;
	}

	public void setTaxaComissao(double taxaComissao) {
		this.taxaComissao = taxaComissao;
	}

	public ArrayList<ResultadoVenda> getHistoricoVendas() {
		return historicoVendas;
	}

	public void setResultadoVenda(ArrayList<ResultadoVenda> historicoVendas) {
		this.historicoVendas = historicoVendas;
	}

	public void adicionarVenda(ResultadoVenda venda) {

		this.historicoVendas.add(venda);
	}
}
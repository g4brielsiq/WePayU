package br.ufal.ic.p2.wepayu.entidades;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmpregadoComissionado extends EmpregadoAssalariado implements Serializable {

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

	public List<ResultadoVenda> getHistoricoVendas() {
		return Collections.unmodifiableList(historicoVendas);
	}

	public void adicionarVenda(ResultadoVenda venda) {

		this.historicoVendas.add(venda);
	}

	@Override
	public EmpregadoComissionado copia() {
		EmpregadoComissionado copia = new EmpregadoComissionado(
				getId(), getNome(), getEndereco(), getSalarioMensal(), taxaComissao);
		copiarAtributosPara(copia);
		for (ResultadoVenda venda : historicoVendas) {
			copia.adicionarVenda(new ResultadoVenda(venda.getData(), venda.getValor()));
		}
		return copia;
	}
}

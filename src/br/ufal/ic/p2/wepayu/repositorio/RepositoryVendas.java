package br.ufal.ic.p2.wepayu.repositorio;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.ufal.ic.p2.wepayu.entidades.ResultadoVenda;

public class RepositoryVendas implements Serializable {
	private final ArrayList<ResultadoVenda> vendas = new ArrayList<>();

	public RepositoryVendas() {
	}

	public void adicionar(ResultadoVenda venda) {
		vendas.add(venda);
	}

	public List<ResultadoVenda> getVendas() {
		return Collections.unmodifiableList(vendas);
	}

	public ArrayList<ResultadoVenda> getVendasLista() {
		return vendas;
	}

	public void setVendasLista(ArrayList<ResultadoVenda> lista) {
		this.vendas.clear();
		if (lista != null) {
			this.vendas.addAll(lista);
		}
	}

	public double totalEntre(LocalDate inicio, LocalDate fim) {
		double total = 0;
		for (ResultadoVenda venda : vendas) {
			if (!venda.getData().isBefore(inicio) && venda.getData().isBefore(fim)) {
				total += venda.getValor();
			}
		}
		return total;
	}

	public RepositoryVendas copia() {
		RepositoryVendas copia = new RepositoryVendas();
		for (ResultadoVenda venda : vendas) {
			copia.adicionar(new ResultadoVenda(venda.getData(), venda.getValor()));
		}
		return copia;
	}
}

package br.ufal.ic.p2.wepayu.repositorio;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.ufal.ic.p2.wepayu.entidades.TaxaServico;

public class RepositoryTaxasServico implements Serializable {
	private final ArrayList<TaxaServico> taxas = new ArrayList<>();

	public RepositoryTaxasServico() {
	}

	public void adicionar(TaxaServico taxa) {
		taxas.add(taxa);
	}

	public List<TaxaServico> getTaxas() {
		return Collections.unmodifiableList(taxas);
	}

	public ArrayList<TaxaServico> getTaxasLista() {
		return taxas;
	}

	public void setTaxasLista(ArrayList<TaxaServico> lista) {
		this.taxas.clear();
		if (lista != null) {
			this.taxas.addAll(lista);
		}
	}

	public double totalEntre(LocalDate inicio, LocalDate fim) {
		double total = 0;
		for (TaxaServico taxa : taxas) {
			if (!taxa.getData().isBefore(inicio) && taxa.getData().isBefore(fim)) {
				total += taxa.getValor();
			}
		}
		return total;
	}

	public RepositoryTaxasServico copia() {
		RepositoryTaxasServico copia = new RepositoryTaxasServico();
		for (TaxaServico taxa : taxas) {
			copia.adicionar(new TaxaServico(taxa.getData(), taxa.getValor()));
		}
		return copia;
	}
}

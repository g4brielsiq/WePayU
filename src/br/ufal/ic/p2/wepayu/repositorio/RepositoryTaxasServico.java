package br.ufal.ic.p2.wepayu.repositorio;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.ufal.ic.p2.wepayu.entidades.TaxaServico;

/** Guarda as taxas de serviço lançadas para um empregado sindicalizado. */
public class RepositoryTaxasServico implements Serializable {
	private final ArrayList<TaxaServico> taxas = new ArrayList<>();

	public void adicionar(TaxaServico taxa) {
		taxas.add(taxa);
	}

	public List<TaxaServico> getTaxas() {
		return Collections.unmodifiableList(taxas);
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

package br.ufal.ic.p2.wepayu.repositorio;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.ufal.ic.p2.wepayu.entidades.CartaoDePonto;

public class RepositoryCartoesDePonto implements Serializable {
	private final ArrayList<CartaoDePonto> cartoes = new ArrayList<>();

	public RepositoryCartoesDePonto() {
	}

	public void adicionar(CartaoDePonto cartao) {
		cartoes.add(cartao);
	}

	public List<CartaoDePonto> getCartoes() {
		return Collections.unmodifiableList(cartoes);
	}

	public ArrayList<CartaoDePonto> getCartoesLista() {
		return cartoes;
	}

	public void setCartoesLista(ArrayList<CartaoDePonto> lista) {
		this.cartoes.clear();
		if (lista != null) {
			this.cartoes.addAll(lista);
		}
	}

	public double totalHorasNormais(LocalDate inicio, LocalDate fim) {
		double total = 0;
		for (CartaoDePonto cartao : cartoes) {
			if (estaNoPeriodo(cartao.getData(), inicio, fim)) {
				total += Math.min(8, cartao.getHoras());
			}
		}
		return total;
	}

	public double totalHorasExtras(LocalDate inicio, LocalDate fim) {
		double total = 0;
		for (CartaoDePonto cartao : cartoes) {
			if (estaNoPeriodo(cartao.getData(), inicio, fim) && cartao.getHoras() > 8) {
				total += cartao.getHoras() - 8;
			}
		}
		return total;
	}

	public RepositoryCartoesDePonto copia() {
		RepositoryCartoesDePonto copia = new RepositoryCartoesDePonto();
		for (CartaoDePonto cartao : cartoes) {
			copia.adicionar(new CartaoDePonto(cartao.getData(), cartao.getHoras()));
		}
		return copia;
	}

	private boolean estaNoPeriodo(LocalDate data, LocalDate inicio, LocalDate fim) {
		return !data.isBefore(inicio) && data.isBefore(fim);
	}
}

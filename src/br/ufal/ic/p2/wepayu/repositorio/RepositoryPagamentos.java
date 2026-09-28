package br.ufal.ic.p2.wepayu.repositorio;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

/** Guarda a última data de pagamento e de processamento de cada empregado. */
public class RepositoryPagamentos implements Serializable {
	private final ArrayList<RegistroPagamento> registros = new ArrayList<>();

	public LocalDate getUltimoPagamento(String idEmpregado) {
		RegistroPagamento registro = buscar(idEmpregado);
		return registro == null ? null : registro.getUltimoPagamento();
	}

	public LocalDate getUltimoProcessamento(String idEmpregado) {
		RegistroPagamento registro = buscar(idEmpregado);
		return registro == null ? null : registro.getUltimoProcessamento();
	}

	public void registrarProcessamento(String idEmpregado, LocalDate data, boolean houvePagamento) {
		RegistroPagamento registro = buscar(idEmpregado);
		if (registro == null) {
			registro = new RegistroPagamento(idEmpregado);
			registros.add(registro);
		}
		registro.setUltimoProcessamento(data);
		if (houvePagamento) {
			registro.setUltimoPagamento(data);
		}
	}

	public void zerar() {
		registros.clear();
	}

	public RepositoryPagamentos copia() {
		RepositoryPagamentos copia = new RepositoryPagamentos();
		for (RegistroPagamento registro : registros) {
			copia.registros.add(registro.copia());
		}
		return copia;
	}

	private RegistroPagamento buscar(String idEmpregado) {
		for (RegistroPagamento registro : registros) {
			if (registro.getIdEmpregado().equals(idEmpregado)) {
				return registro;
			}
		}
		return null;
	}

	private static class RegistroPagamento implements Serializable {
		private final String idEmpregado;
		private LocalDate ultimoPagamento;
		private LocalDate ultimoProcessamento;

		RegistroPagamento(String idEmpregado) {
			this.idEmpregado = idEmpregado;
		}

		String getIdEmpregado() {
			return idEmpregado;
		}

		LocalDate getUltimoPagamento() {
			return ultimoPagamento;
		}

		LocalDate getUltimoProcessamento() {
			return ultimoProcessamento;
		}

		void setUltimoPagamento(LocalDate data) {
			ultimoPagamento = data;
		}

		void setUltimoProcessamento(LocalDate data) {
			ultimoProcessamento = data;
		}

		RegistroPagamento copia() {
			RegistroPagamento copia = new RegistroPagamento(idEmpregado);
			copia.ultimoPagamento = ultimoPagamento;
			copia.ultimoProcessamento = ultimoProcessamento;
			return copia;
		}
	}
}

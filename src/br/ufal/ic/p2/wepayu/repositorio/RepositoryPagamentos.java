package br.ufal.ic.p2.wepayu.repositorio;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

public class RepositoryPagamentos implements Serializable {
	private ArrayList<RegistroPagamento> registros = new ArrayList<>();

	public RepositoryPagamentos() {
	}

	public ArrayList<RegistroPagamento> getRegistros() {
		return registros;
	}

	public void setRegistros(ArrayList<RegistroPagamento> registros) {
		this.registros = registros != null ? registros : new ArrayList<>();
	}

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
			if (registro.getIdEmpregado() != null && registro.getIdEmpregado().equals(idEmpregado)) {
				return registro;
			}
		}
		return null;
	}

	public static class RegistroPagamento implements Serializable {
		private String idEmpregado;
		private LocalDate ultimoPagamento;
		private LocalDate ultimoProcessamento;

		public RegistroPagamento() {
		}

		public RegistroPagamento(String idEmpregado) {
			this.idEmpregado = idEmpregado;
		}

		public String getIdEmpregado() {
			return idEmpregado;
		}

		public void setIdEmpregado(String idEmpregado) {
			this.idEmpregado = idEmpregado;
		}

		public LocalDate getUltimoPagamento() {
			return ultimoPagamento;
		}

		public void setUltimoPagamento(LocalDate data) {
			ultimoPagamento = data;
		}

		public LocalDate getUltimoProcessamento() {
			return ultimoProcessamento;
		}

		public void setUltimoProcessamento(LocalDate data) {
			ultimoProcessamento = data;
		}

		public RegistroPagamento copia() {
			RegistroPagamento copia = new RegistroPagamento(idEmpregado);
			copia.ultimoPagamento = ultimoPagamento;
			copia.ultimoProcessamento = ultimoProcessamento;
			return copia;
		}
	}
}

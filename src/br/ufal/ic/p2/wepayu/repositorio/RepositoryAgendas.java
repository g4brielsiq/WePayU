package br.ufal.ic.p2.wepayu.repositorio;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import br.ufal.ic.p2.wepayu.excecoes.ValorInvalidoException;

public class RepositoryAgendas implements Serializable {
	private static final long serialVersionUID = 1L;

	private List<String> agendas = new ArrayList<>();

	public RepositoryAgendas() {
		zerar();
	}

	public RepositoryAgendas(RepositoryAgendas outro) {
		if (outro != null) {
			this.agendas = new ArrayList<>(outro.agendas);
		} else {
			zerar();
		}
	}

	public void zerar() {
		agendas.clear();
		agendas.add("semanal 5");
		agendas.add("mensal $");
		agendas.add("semanal 2 5");
	}

	public RepositoryAgendas copia() {
		return new RepositoryAgendas(this);
	}

	public boolean isDisponivel(String agenda) {
		if (agenda == null) return false;
		return agendas.contains(agenda.trim());
	}

	public void criarAgenda(String descricao) throws ValorInvalidoException {
		validarDescricao(descricao);
		String agendaNormalizada = normalizar(descricao);
		if (agendas.contains(agendaNormalizada)) {
			throw new ValorInvalidoException("Agenda de pagamentos ja existe");
		}
		agendas.add(agendaNormalizada);
	}

	public static void validarDescricao(String descricao) throws ValorInvalidoException {
		if (descricao == null || descricao.trim().isEmpty()) {
			throw new ValorInvalidoException("Descricao de agenda invalida");
		}
		String[] partes = descricao.trim().split("\\s+");
		if (partes[0].equals("semanal")) {
			if (partes.length == 2) {
				try {
					int dia = Integer.parseInt(partes[1]);
					if (dia < 1 || dia > 7) {
						throw new ValorInvalidoException("Descricao de agenda invalida");
					}
				} catch (NumberFormatException e) {
					throw new ValorInvalidoException("Descricao de agenda invalida");
				}
			} else if (partes.length == 3) {
				try {
					int k = Integer.parseInt(partes[1]);
					int dia = Integer.parseInt(partes[2]);
					if (k < 1 || k > 52 || dia < 1 || dia > 7) {
						throw new ValorInvalidoException("Descricao de agenda invalida");
					}
				} catch (NumberFormatException e) {
					throw new ValorInvalidoException("Descricao de agenda invalida");
				}
			} else {
				throw new ValorInvalidoException("Descricao de agenda invalida");
			}
		} else if (partes[0].equals("mensal")) {
			if (partes.length == 2) {
				if (!partes[1].equals("$")) {
					try {
						int dia = Integer.parseInt(partes[1]);
						if (dia < 1 || dia > 28) {
							throw new ValorInvalidoException("Descricao de agenda invalida");
						}
					} catch (NumberFormatException e) {
						throw new ValorInvalidoException("Descricao de agenda invalida");
					}
				}
			} else {
				throw new ValorInvalidoException("Descricao de agenda invalida");
			}
		} else {
			throw new ValorInvalidoException("Descricao de agenda invalida");
		}
	}

	private static String normalizar(String descricao) {
		String[] partes = descricao.trim().split("\\s+");
		return String.join(" ", partes);
	}

	public List<String> getAgendas() {
		return new ArrayList<>(agendas);
	}

	public void setAgendas(List<String> novasAgendas) {
		if (novasAgendas != null && !novasAgendas.isEmpty()) {
			this.agendas = new ArrayList<>(novasAgendas);
		} else {
			zerar();
		}
	}

	public static boolean isDiaDePagamento(String agenda, LocalDate data) {
		if (agenda == null || agenda.trim().isEmpty() || data == null) {
			return false;
		}
		String[] partes = agenda.trim().split("\\s+");
		if (partes[0].equals("mensal")) {
			if (partes.length == 2) {
				if (partes[1].equals("$")) {
					LocalDate ultimoDia = data.with(TemporalAdjusters.lastDayOfMonth());
					while (ultimoDia.getDayOfWeek() == DayOfWeek.SATURDAY || ultimoDia.getDayOfWeek() == DayOfWeek.SUNDAY) {
						ultimoDia = ultimoDia.minusDays(1);
					}
					return data.equals(ultimoDia);
				} else {
					try {
						int dia = Integer.parseInt(partes[1]);
						return data.getDayOfMonth() == dia;
					} catch (NumberFormatException e) {
						return false;
					}
				}
			}
		} else if (partes[0].equals("semanal")) {
			if (partes.length == 2) {
				try {
					int dia = Integer.parseInt(partes[1]);
					return data.getDayOfWeek().getValue() == dia;
				} catch (NumberFormatException e) {
					return false;
				}
			} else if (partes.length == 3) {
				try {
					int k = Integer.parseInt(partes[1]);
					int dia = Integer.parseInt(partes[2]);
					if (data.getDayOfWeek().getValue() != dia) {
						return false;
					}
					int semana = data.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
					return semana % k == 0;
				} catch (NumberFormatException e) {
					return false;
				}
			}
		}
		return false;
	}

	public static int getSemanas(String agenda) {
		if (agenda == null) return 1;
		String[] partes = agenda.trim().split("\\s+");
		if (partes[0].equals("semanal") && partes.length == 3) {
			try {
				return Integer.parseInt(partes[1]);
			} catch (NumberFormatException e) {
				return 1;
			}
		}
		return 1;
	}
}

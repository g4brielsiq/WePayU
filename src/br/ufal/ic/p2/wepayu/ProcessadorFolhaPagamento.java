package br.ufal.ic.p2.wepayu;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufal.ic.p2.wepayu.models.CartaoDePonto;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.EmpregadoAssalariado;
import br.ufal.ic.p2.wepayu.models.EmpregadoComissionado;
import br.ufal.ic.p2.wepayu.models.EmpregadoHorista;
import br.ufal.ic.p2.wepayu.models.RepositoryEmpregados;
import br.ufal.ic.p2.wepayu.models.ResultadoVenda;
import br.ufal.ic.p2.wepayu.models.TaxaServico;

public class ProcessadorFolhaPagamento {

	private Map<String, LocalDate> ultimoPagamento = new HashMap<>();
	private Map<String, LocalDate> ultimoProcessamentoFolha = new HashMap<>();

	public ProcessadorFolhaPagamento() {
	}

	public ProcessadorFolhaPagamento(ProcessadorFolhaPagamento outro) {
		this.ultimoPagamento = new HashMap<>(outro.ultimoPagamento);
		this.ultimoProcessamentoFolha = new HashMap<>(outro.ultimoProcessamentoFolha);
	}

	public void zerar() {
		ultimoPagamento.clear();
		ultimoProcessamentoFolha.clear();
	}

	public String totalFolha(RepositoryEmpregados empregados, String data) throws Exception {
		LocalDate dataFolha = converterData(data, "Data invalida.");
		double totalBruto = 0.0;
		for (Empregado emp : empregados.getListaEmpregados()) {
			if (isDiaDePagamento(emp, dataFolha)) {
				totalBruto += calcularSalarioBruto(emp, dataFolha);
			}
		}
		return formatarMonetario(arredondar(totalBruto));
	}

	public void rodaFolha(RepositoryEmpregados empregados, String data, String saida) throws Exception {
		LocalDate dataFolha = converterData(data, "Data invalida.");

		List<EmpregadoHorista> horistas = new ArrayList<>();
		List<EmpregadoAssalariado> assalariados = new ArrayList<>();
		List<EmpregadoComissionado> comissionados = new ArrayList<>();

		for (Empregado emp : empregados.getListaEmpregados()) {
			if (isDiaDePagamento(emp, dataFolha)) {
				if (emp instanceof EmpregadoComissionado) comissionados.add((EmpregadoComissionado) emp);
				else if (emp instanceof EmpregadoHorista) horistas.add((EmpregadoHorista) emp);
				else if (emp instanceof EmpregadoAssalariado) assalariados.add((EmpregadoAssalariado) emp);
			}
		}

		horistas.sort(Comparator.comparing(Empregado::getNome));
		assalariados.sort(Comparator.comparing(Empregado::getNome));
		comissionados.sort(Comparator.comparing(Empregado::getNome));

		boolean haPagamentos = !horistas.isEmpty() || !assalariados.isEmpty() || !comissionados.isEmpty();

		try (PrintWriter writer = new PrintWriter(new FileWriter(saida))) {
			writer.println("FOLHA DE PAGAMENTO DO DIA " + dataFolha.toString());

			if (!haPagamentos) {
				writer.println("====================================");
				writer.println("Nao ha funcionarios a serem pagos hoje.");
				writer.println("====================================");
				writer.println();
				writer.print("TOTAL FOLHA: 0,00");
			} else {
				writer.println("====================================");
				writer.println();

				// --- BLOCO HORISTAS ---
				writer.println("===============================================================================================================================");
				writer.println("===================== HORISTAS ================================================================================================");
				writer.println("===============================================================================================================================");
				writer.println("Nome                                 Horas Extra Salario Bruto Descontos Salario Liquido Metodo");
				writer.println("==================================== ===== ===== ============= ========= =============== ======================================");

				int totalHoras = 0;
				int totalExtras = 0;
				double totalBrutoHoristas = 0.0;
				double totalDescHoristas = 0.0;
				double totalLiqHoristas = 0.0;

				for (EmpregadoHorista h : horistas) {
					double[] hs = obterHoras(h, dataFolha);
					int hn = (int) hs[0];
					int he = (int) hs[1];
					double bruto = arredondar(calcularSalarioBruto(h, dataFolha));
					double desc = arredondar(calcularDescontos(h, dataFolha));
					double liq = arredondar(bruto - desc);

					totalHoras += hn;
					totalExtras += he;
					totalBrutoHoristas += bruto;
					totalDescHoristas += desc;
					totalLiqHoristas += liq;

					writer.printf("%-36s %5d %5d %13s %9s %15s %s\n",
							h.getNome(), hn, he,
							formatarMonetario(bruto),
							formatarMonetario(desc),
							formatarMonetario(liq),
							formatarMetodo(h));
				}
				writer.println();
				writer.printf("%-36s %5d %5d %13s %9s %15s\n",
						"TOTAL HORISTAS", totalHoras, totalExtras,
						formatarMonetario(totalBrutoHoristas),
						formatarMonetario(totalDescHoristas),
						formatarMonetario(totalLiqHoristas));
				writer.println();

				// --- BLOCO ASSALARIADOS ---
				writer.println("===============================================================================================================================");
				writer.println("===================== ASSALARIADOS ============================================================================================");
				writer.println("===============================================================================================================================");
				writer.println("Nome                                             Salario Bruto Descontos Salario Liquido Metodo");
				writer.println("================================================ ============= ========= =============== ======================================");

				double totalBrutoAss = 0.0;
				double totalDescAss = 0.0;
				double totalLiqAss = 0.0;

				for (EmpregadoAssalariado a : assalariados) {
					double bruto = arredondar(calcularSalarioBruto(a, dataFolha));
					double desc = arredondar(calcularDescontos(a, dataFolha));
					double liq = arredondar(bruto - desc);

					totalBrutoAss += bruto;
					totalDescAss += desc;
					totalLiqAss += liq;

					writer.printf("%-48s %13s %9s %15s %s\n",
							a.getNome(),
							formatarMonetario(bruto),
							formatarMonetario(desc),
							formatarMonetario(liq),
							formatarMetodo(a));
				}
				writer.println();
				writer.printf("%-48s %13s %9s %15s\n",
						"TOTAL ASSALARIADOS",
						formatarMonetario(totalBrutoAss),
						formatarMonetario(totalDescAss),
						formatarMonetario(totalLiqAss));
				writer.println();

				// --- BLOCO COMISSIONADOS ---
				writer.println("===============================================================================================================================");
				writer.println("===================== COMISSIONADOS ===========================================================================================");
				writer.println("===============================================================================================================================");
				writer.println("Nome                  Fixo     Vendas   Comissao Salario Bruto Descontos Salario Liquido Metodo");
				writer.println("===================== ======== ======== ======== ============= ========= =============== ======================================");

				double totalFixoCom = 0.0;
				double totalVendasCom = 0.0;
				double totalComissaoCom = 0.0;
				double totalBrutoCom = 0.0;
				double totalDescCom = 0.0;
				double totalLiqCom = 0.0;

				for (EmpregadoComissionado c : comissionados) {
					double fixo = truncar((c.getSalarioMensal() * 12.0) / 26.0);
					double vendas = 0.0;
					LocalDate ultimoProc = ultimoProcessamentoFolha.get(c.getId());
					for (ResultadoVenda v : c.getHistoricoVendas()) {
						if (!v.getData().isAfter(dataFolha) && (ultimoProc == null || v.getData().isAfter(ultimoProc))) {
							vendas += v.getValor();
						}
					}
					vendas = arredondar(vendas);
					double comissaoValor = truncar(vendas * c.getTaxaComissao());
					double bruto = arredondar(fixo + comissaoValor);
					double desc = arredondar(calcularDescontos(c, dataFolha));
					double liq = arredondar(bruto - desc);

					totalFixoCom += fixo;
					totalVendasCom += vendas;
					totalComissaoCom += comissaoValor;
					totalBrutoCom += bruto;
					totalDescCom += desc;
					totalLiqCom += liq;

					writer.printf("%-21s %8s %8s %8s %13s %9s %15s %s\n",
							c.getNome(),
							formatarMonetario(fixo),
							formatarMonetario(vendas),
							formatarMonetario(comissaoValor),
							formatarMonetario(bruto),
							formatarMonetario(desc),
							formatarMonetario(liq),
							formatarMetodo(c));
				}
				writer.println();
				writer.printf("%-21s %8s %8s %8s %13s %9s %15s\n",
						"TOTAL COMISSIONADOS",
						formatarMonetario(totalFixoCom),
						formatarMonetario(totalVendasCom),
						formatarMonetario(totalComissaoCom),
						formatarMonetario(totalBrutoCom),
						formatarMonetario(totalDescCom),
						formatarMonetario(totalLiqCom));
				writer.println();

				double totalGeralBruto = arredondar(totalBrutoHoristas + totalBrutoAss + totalBrutoCom);
				writer.print("TOTAL FOLHA: " + formatarMonetario(totalGeralBruto));
			}
		} catch (Exception e) {
			throw new Exception("Erro ao gerar folha.");
		}

		for (EmpregadoHorista h : horistas) {
			double bruto = calcularSalarioBruto(h, dataFolha);
			if (bruto > 0) {
				ultimoPagamento.put(h.getId(), dataFolha);
			}
			ultimoProcessamentoFolha.put(h.getId(), dataFolha);
		}
		for (EmpregadoComissionado c : comissionados) {
			ultimoPagamento.put(c.getId(), dataFolha);
			ultimoProcessamentoFolha.put(c.getId(), dataFolha);
		}
		for (EmpregadoAssalariado a : assalariados) {
			ultimoPagamento.put(a.getId(), dataFolha);
			ultimoProcessamentoFolha.put(a.getId(), dataFolha);
		}
	}

	private String formatarMetodo(Empregado emp) {
		String metodo = emp.getMetodoPagamento();
		if (metodo.equals("emMaos")) return "Em maos";
		if (metodo.equals("correios")) return "Correios, " + emp.getEndereco();
		if (metodo.equals("banco")) {
			return String.format("%s, Ag. %s CC %s", emp.getBanco(), emp.getAgencia(), emp.getContaCorrente());
		}
		return metodo;
	}

	private LocalDate converterData(String data, String mensagemErro) throws Exception {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT);
			return LocalDate.parse(data, formatter);
		} catch (DateTimeParseException e) {
			throw new Exception(mensagemErro);
		}
	}

	private double arredondar(double valor) {
		return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	private double truncar(double valor) {
		return BigDecimal.valueOf(valor).setScale(2, RoundingMode.DOWN).doubleValue();
	}

	private String formatarMonetario(double valor) {
		return String.format("%.2f", valor).replace(".", ",");
	}

	private String formatarHoras(double valor) {
		if (valor % 1 == 0) return String.valueOf((long) valor);
		String formatado = String.valueOf(valor).replace(".", ",");
		if (formatado.endsWith(",0")) return formatado.substring(0, formatado.length() - 2);
		return formatado;
	}

	private boolean isDiaDePagamento(Empregado emp, LocalDate data) {
		String tipo = emp.getTipo();
		if (tipo.equals("assalariado")) {
			LocalDate ultimoDia = data.with(TemporalAdjusters.lastDayOfMonth());
			while (ultimoDia.getDayOfWeek() == DayOfWeek.SATURDAY || ultimoDia.getDayOfWeek() == DayOfWeek.SUNDAY) {
				ultimoDia = ultimoDia.minusDays(1);
			}
			return data.equals(ultimoDia);
		} else if (tipo.equals("horista")) {
			return data.getDayOfWeek() == DayOfWeek.FRIDAY;
		} else if (tipo.equals("comissionado")) {
			if (data.getDayOfWeek() != DayOfWeek.FRIDAY) return false;
			long semanas = ChronoUnit.WEEKS.between(LocalDate.of(2005, 1, 14), data);
			return Math.abs(semanas) % 2 == 0;
		}
		return false;
	}

	private double[] obterHoras(Empregado emp, LocalDate dataFolha) {
		double hNormais = 0.0;
		double hExtras = 0.0;
		if (emp instanceof EmpregadoHorista) {
			LocalDate ultimoProc = ultimoProcessamentoFolha.get(emp.getId());
			for (CartaoDePonto c : ((EmpregadoHorista) emp).getCartoesDePonto()) {
				if (!c.getData().isAfter(dataFolha) && (ultimoProc == null || c.getData().isAfter(ultimoProc))) {
					hNormais += Math.min(8.0, c.getHoras());
					if (c.getHoras() > 8.0) hExtras += (c.getHoras() - 8.0);
				}
			}
		}
		return new double[]{hNormais, hExtras};
	}

	private double calcularSalarioBruto(Empregado emp, LocalDate dataFolha) {
		double bruto = 0.0;
		if (emp instanceof EmpregadoHorista) {
			EmpregadoHorista h = (EmpregadoHorista) emp;
			double[] horas = obterHoras(h, dataFolha);
			bruto = (horas[0] * h.getSalarioPorHora()) + (horas[1] * h.getSalarioPorHora() * 1.5);
		} else if (emp instanceof EmpregadoComissionado) {
			EmpregadoComissionado c = (EmpregadoComissionado) emp;
			double fixo = truncar((c.getSalarioMensal() * 12.0) / 26.0);
			double vendas = 0.0;
			LocalDate ultimoProc = ultimoProcessamentoFolha.get(c.getId());
			for (ResultadoVenda v : c.getHistoricoVendas()) {
				if (!v.getData().isAfter(dataFolha) && (ultimoProc == null || v.getData().isAfter(ultimoProc))) {
					vendas += v.getValor();
				}
			}
			double comissaoValor = truncar(arredondar(vendas) * c.getTaxaComissao());
			bruto = fixo + comissaoValor;
		} else if (emp instanceof EmpregadoAssalariado) {
			bruto = ((EmpregadoAssalariado) emp).getSalarioMensal();
		}
		return bruto;
	}

	private double calcularDescontos(Empregado emp, LocalDate dataFolha) {
		double descontos = 0.0;
		if (emp.isSindicalizado()) {
			double bruto = calcularSalarioBruto(emp, dataFolha);
			if (bruto > 0) {
				long diasPeriodo;
				if (emp instanceof EmpregadoHorista) {
					LocalDate ultimo = ultimoPagamento.get(emp.getId());
					if (ultimo != null) {
						diasPeriodo = ChronoUnit.DAYS.between(ultimo, dataFolha);
					} else {
						diasPeriodo = 7;
					}
				} else if (emp instanceof EmpregadoComissionado) {
					diasPeriodo = 14;
				} else {
					diasPeriodo = dataFolha.lengthOfMonth();
				}
				descontos += (emp.getTaxaSindical() * diasPeriodo);
			}
			LocalDate ultimoProc = ultimoProcessamentoFolha.get(emp.getId());
			for (TaxaServico t : emp.getTaxasServico()) {
				if (!t.getData().isAfter(dataFolha) && (ultimoProc == null || t.getData().isAfter(ultimoProc))) {
					descontos += t.getValor();
				}
			}
		}
		return descontos;
	}
}

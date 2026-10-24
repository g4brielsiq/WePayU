package br.ufal.ic.p2.wepayu.servicos;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import br.ufal.ic.p2.wepayu.entidades.Empregado;
import br.ufal.ic.p2.wepayu.entidades.EmpregadoAssalariado;
import br.ufal.ic.p2.wepayu.entidades.EmpregadoComissionado;
import br.ufal.ic.p2.wepayu.entidades.EmpregadoHorista;
import br.ufal.ic.p2.wepayu.excecoes.FolhaException;
import br.ufal.ic.p2.wepayu.repositorio.RepositoryAgendas;
import br.ufal.ic.p2.wepayu.repositorio.RepositoryEmpregados;
import br.ufal.ic.p2.wepayu.repositorio.RepositoryPagamentos;
import br.ufal.ic.p2.wepayu.util.FormatoUtil;

public class ProcessadorFolhaPagamento {

	private RepositoryPagamentos historicoPagamentos = new RepositoryPagamentos();

	public ProcessadorFolhaPagamento() {
	}

	public ProcessadorFolhaPagamento(ProcessadorFolhaPagamento outro) {
		this.historicoPagamentos = outro.historicoPagamentos.copia();
	}

	public void zerar() {
		historicoPagamentos.zerar();
	}

	public RepositoryPagamentos getHistoricoPagamentos() {
		return historicoPagamentos;
	}

	public void setHistoricoPagamentos(RepositoryPagamentos historicoPagamentos) {
		this.historicoPagamentos = historicoPagamentos != null ? historicoPagamentos : new RepositoryPagamentos();
	}

	public String totalFolha(RepositoryEmpregados empregados, String data) throws Exception {
		LocalDate dataFolha = FormatoUtil.converterData(data, "Data invalida.");
		double totalBruto = 0.0;
		for (Empregado empregado : empregados.getListaEmpregados()) {
			if (empregado.isDiaDePagamento(dataFolha)) {
				LocalDate inicioPeriodo = obterDataInicioPeriodo(empregado, dataFolha);
				totalBruto += empregado.calcularSalarioBruto(dataFolha, inicioPeriodo);
			}
		}
		return FormatoUtil.formatarMonetario(FormatoUtil.arredondar(totalBruto));
	}

	public void rodaFolha(RepositoryEmpregados empregados, String data, String saida) throws Exception {
		LocalDate dataFolha = FormatoUtil.converterData(data, "Data invalida.");

		List<EmpregadoHorista> horistas = new ArrayList<>();
		List<EmpregadoAssalariado> assalariados = new ArrayList<>();
		List<EmpregadoComissionado> comissionados = new ArrayList<>();

		for (Empregado empregado : empregados.getListaEmpregados()) {
			if (empregado.isDiaDePagamento(dataFolha)) {
				if (empregado instanceof EmpregadoComissionado) {
					comissionados.add((EmpregadoComissionado) empregado);
				} else if (empregado instanceof EmpregadoHorista) {
					horistas.add((EmpregadoHorista) empregado);
				} else if (empregado instanceof EmpregadoAssalariado) {
					assalariados.add((EmpregadoAssalariado) empregado);
				}
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

				for (EmpregadoHorista horista : horistas) {
					LocalDate inicioPeriodo = obterDataInicioPeriodo(horista, dataFolha);
					double[] horasApontadas = horista.obterHorasTrabalhadas(inicioPeriodo, dataFolha);
					int horasNormais = (int) horasApontadas[0];
					int horasExtras = (int) horasApontadas[1];
					double bruto = FormatoUtil.arredondar(horista.calcularSalarioBruto(dataFolha, inicioPeriodo));
					double desc = FormatoUtil.arredondar(calcularDescontos(horista, dataFolha));
					double liq = FormatoUtil.arredondar(bruto - desc);

					totalHoras += horasNormais;
					totalExtras += horasExtras;
					totalBrutoHoristas += bruto;
					totalDescHoristas += desc;
					totalLiqHoristas += liq;

					writer.printf("%-36s %5d %5d %13s %9s %15s %s\n",
							horista.getNome(), horasNormais, horasExtras,
							FormatoUtil.formatarMonetario(bruto),
							FormatoUtil.formatarMonetario(desc),
							FormatoUtil.formatarMonetario(liq),
							horista.getDescricaoMetodoPagamento());
				}
				writer.println();
				writer.printf("%-36s %5d %5d %13s %9s %15s\n",
						"TOTAL HORISTAS", totalHoras, totalExtras,
						FormatoUtil.formatarMonetario(totalBrutoHoristas),
						FormatoUtil.formatarMonetario(totalDescHoristas),
						FormatoUtil.formatarMonetario(totalLiqHoristas));
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

				for (EmpregadoAssalariado assalariado : assalariados) {
					double bruto = FormatoUtil.arredondar(assalariado.calcularSalarioBruto(dataFolha, null));
					double desc = FormatoUtil.arredondar(calcularDescontos(assalariado, dataFolha));
					double liq = FormatoUtil.arredondar(bruto - desc);

					totalBrutoAss += bruto;
					totalDescAss += desc;
					totalLiqAss += liq;

					writer.printf("%-48s %13s %9s %15s %s\n",
							assalariado.getNome(),
							FormatoUtil.formatarMonetario(bruto),
							FormatoUtil.formatarMonetario(desc),
							FormatoUtil.formatarMonetario(liq),
							assalariado.getDescricaoMetodoPagamento());
				}
				writer.println();
				writer.printf("%-48s %13s %9s %15s\n",
						"TOTAL ASSALARIADOS",
						FormatoUtil.formatarMonetario(totalBrutoAss),
						FormatoUtil.formatarMonetario(totalDescAss),
						FormatoUtil.formatarMonetario(totalLiqAss));
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

				for (EmpregadoComissionado comissionado : comissionados) {
					LocalDate inicioPeriodo = obterDataInicioPeriodo(comissionado, dataFolha);
					double fixo = comissionado.calcularSalarioFixo();
					double vendas = comissionado.obterTotalVendas(inicioPeriodo, dataFolha);
					double comissaoValor = comissionado.obterComissao(inicioPeriodo, dataFolha);
					double bruto = FormatoUtil.arredondar(fixo + comissaoValor);
					double desc = FormatoUtil.arredondar(calcularDescontos(comissionado, dataFolha));
					double liq = FormatoUtil.arredondar(bruto - desc);

					totalFixoCom += fixo;
					totalVendasCom += vendas;
					totalComissaoCom += comissaoValor;
					totalBrutoCom += bruto;
					totalDescCom += desc;
					totalLiqCom += liq;

					writer.printf("%-21s %8s %8s %8s %13s %9s %15s %s\n",
							comissionado.getNome(),
							FormatoUtil.formatarMonetario(fixo),
							FormatoUtil.formatarMonetario(vendas),
							FormatoUtil.formatarMonetario(comissaoValor),
							FormatoUtil.formatarMonetario(bruto),
							FormatoUtil.formatarMonetario(desc),
							FormatoUtil.formatarMonetario(liq),
							comissionado.getDescricaoMetodoPagamento());
				}
				writer.println();
				writer.printf("%-21s %8s %8s %8s %13s %9s %15s\n",
						"TOTAL COMISSIONADOS",
						FormatoUtil.formatarMonetario(totalFixoCom),
						FormatoUtil.formatarMonetario(totalVendasCom),
						FormatoUtil.formatarMonetario(totalComissaoCom),
						FormatoUtil.formatarMonetario(totalBrutoCom),
						FormatoUtil.formatarMonetario(totalDescCom),
						FormatoUtil.formatarMonetario(totalLiqCom));
				writer.println();

				double totalGeralBruto = FormatoUtil.arredondar(totalBrutoHoristas + totalBrutoAss + totalBrutoCom);
				writer.print("TOTAL FOLHA: " + FormatoUtil.formatarMonetario(totalGeralBruto));
			}
		} catch (Exception e) {
			throw new FolhaException("Erro ao gerar folha.", e);
		}

		for (EmpregadoHorista horista : horistas) {
			double bruto = horista.calcularSalarioBruto(dataFolha, obterDataInicioPeriodo(horista, dataFolha));
			historicoPagamentos.registrarProcessamento(horista.getId(), dataFolha, bruto > 0);
		}
		for (EmpregadoComissionado comissionado : comissionados) {
			historicoPagamentos.registrarProcessamento(comissionado.getId(), dataFolha, true);
		}
		for (EmpregadoAssalariado assalariado : assalariados) {
			historicoPagamentos.registrarProcessamento(assalariado.getId(), dataFolha, true);
		}
	}

	private LocalDate obterDataInicioPeriodo(Empregado empregado, LocalDate dataFolha) {
		LocalDate ultimoProc = historicoPagamentos.getUltimoProcessamento(empregado.getId());
		LocalDate inicioAgenda;
		String agenda = empregado.getAgendaPagamento();
		if (agenda.startsWith("semanal")) {
			int k = RepositoryAgendas.getSemanas(agenda);
			inicioAgenda = dataFolha.minusWeeks(k);
		} else if (agenda.startsWith("mensal")) {
			String[] partes = agenda.split("\\s+");
			if (partes.length == 2 && partes[1].equals("$")) {
				LocalDate mesAnterior = dataFolha.minusMonths(1);
				inicioAgenda = mesAnterior.with(TemporalAdjusters.lastDayOfMonth());
				while (inicioAgenda.getDayOfWeek() == DayOfWeek.SATURDAY || inicioAgenda.getDayOfWeek() == DayOfWeek.SUNDAY) {
					inicioAgenda = inicioAgenda.minusDays(1);
				}
			} else if (partes.length == 2) {
				int dia = Integer.parseInt(partes[1]);
				inicioAgenda = dataFolha.minusMonths(1).withDayOfMonth(dia);
			} else {
				inicioAgenda = dataFolha.minusMonths(1);
			}
		} else {
			inicioAgenda = dataFolha.minusWeeks(1);
		}

		if (ultimoProc != null && !ultimoProc.equals(dataFolha) && ultimoProc.isAfter(inicioAgenda)) {
			return ultimoProc;
		}
		return inicioAgenda;
	}

	private double calcularDescontos(Empregado empregado, LocalDate dataFolha) {
		double descontos = 0.0;
		if (empregado.isSindicalizado()) {
			LocalDate inicioPeriodo = obterDataInicioPeriodo(empregado, dataFolha);
			double bruto = empregado.calcularSalarioBruto(dataFolha, inicioPeriodo);
			if (bruto > 0) {
				long diasPeriodo;
				String agenda = empregado.getAgendaPagamento();
				if (agenda.startsWith("mensal")) {
					diasPeriodo = dataFolha.lengthOfMonth();
				} else {
					int k = RepositoryAgendas.getSemanas(agenda);
					if (empregado instanceof EmpregadoHorista) {
						LocalDate ultimo = historicoPagamentos.getUltimoPagamento(empregado.getId());
						if (ultimo != null && !ultimo.equals(dataFolha)) {
							diasPeriodo = ChronoUnit.DAYS.between(ultimo, dataFolha);
						} else {
							diasPeriodo = 7L * k;
						}
					} else {
						diasPeriodo = 7L * k;
					}
				}
				descontos += (empregado.getTaxaSindical() * diasPeriodo);
			}
			descontos += empregado.totalTaxasServicoNoPeriodo(inicioPeriodo, dataFolha);
		}
		return descontos;
	}
}

package br.ufal.ic.p2.wepayu.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

import br.ufal.ic.p2.wepayu.excecoes.ValorInvalidoException;

public final class FormatoUtil {

	private static final DateTimeFormatter FORMATADOR_DATA = 
			DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);

	private FormatoUtil() {
		// Construtor privado para classe utilitária
	}

	public static LocalDate converterData(String data, String mensagemErro) throws ValorInvalidoException {
		if (data == null || data.trim().isEmpty()) {
			throw new ValorInvalidoException(mensagemErro);
		}
		try {
			return LocalDate.parse(data.trim(), FORMATADOR_DATA);
		} catch (DateTimeParseException e) {
			throw new ValorInvalidoException(mensagemErro);
		}
	}

	public static double arredondar(double valor) {
		return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	public static double truncar(double valor) {
		return BigDecimal.valueOf(valor).setScale(2, RoundingMode.DOWN).doubleValue();
	}

	public static String formatarMonetario(double valor) {
		return String.format(Locale.ROOT, "%.2f", valor).replace(".", ",");
	}

	public static String formatarHoras(double valor) {
		if (valor % 1 == 0) {
			return String.valueOf((long) valor);
		}
		String formatado = String.valueOf(valor).replace(".", ",");
		if (formatado.endsWith(",0")) {
			return formatado.substring(0, formatado.length() - 2);
		}
		return formatado;
	}
}

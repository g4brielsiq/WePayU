package br.ufal.ic.p2.wepayu.excecoes;

public class WePayUException extends Exception {
	private static final long serialVersionUID = 1L;

	public WePayUException(String message) {
		super(message);
	}

	public WePayUException(String message, Throwable cause) {
		super(message, cause);
	}
}

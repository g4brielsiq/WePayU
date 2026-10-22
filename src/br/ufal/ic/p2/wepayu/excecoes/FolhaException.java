package br.ufal.ic.p2.wepayu.excecoes;

public class FolhaException extends WePayUException {
	private static final long serialVersionUID = 1L;

	public FolhaException(String message) {
		super(message);
	}

	public FolhaException(String message, Throwable cause) {
		super(message, cause);
	}
}

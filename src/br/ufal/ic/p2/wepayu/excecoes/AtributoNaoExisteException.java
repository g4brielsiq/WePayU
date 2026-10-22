package br.ufal.ic.p2.wepayu.excecoes;

public class AtributoNaoExisteException extends WePayUException {
	private static final long serialVersionUID = 1L;

	public AtributoNaoExisteException() {
		super("Atributo nao existe.");
	}

	public AtributoNaoExisteException(String message) {
		super(message);
	}
}

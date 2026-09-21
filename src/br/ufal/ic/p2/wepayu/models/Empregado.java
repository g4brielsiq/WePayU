package br.ufal.ic.p2.wepayu.models;

public abstract class Empregado {

	private String id;
	
	private String nome;
	private String endereco;
	private String tipo;
	private String metodoPagamento = "em maos";

	// atributos referente à características "sindicais" de alguns empregados
	private boolean sindicalizado = false;
	private String idSindicato;
	private double taxaSindical;

	// id deve-se setar automaticamente
	public Empregado(String id, String nome, String endereco, String tipo) {
		
		this.id = id;
		this.nome = nome;
		this.endereco = endereco;
		this.tipo = tipo;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getMetodoPagamento() {
		return metodoPagamento;
	}

	public void setMetodoPagamento(String metodoPagamento) {
		this.metodoPagamento = metodoPagamento;
	}

	public boolean isSindicalizado() {
		return sindicalizado;
	}

	public void setSindicalizado(boolean sindicalizado) {
		this.sindicalizado = sindicalizado;
	}

	public String getIdSindicato() {
		return idSindicato;
	}

	public void setIdSindicato(String idSindicato) {
		this.idSindicato = idSindicato;
	}

	public double getTaxaSindical() {
		return taxaSindical;
	}

	public void setTaxaSindical(double taxaSindical) {
		this.taxaSindical = taxaSindical;
	}
}
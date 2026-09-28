package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class Empregado implements Serializable {
	

	private String id;
	
	private String nome;
	private String endereco;
	private String tipo;
	private String metodoPagamento = "emMaos";

	// atributos referente à características "sindicais" de alguns empregados
	private boolean sindicalizado = false;
	private String idSindicato;
	private double taxaSindical;

	private ArrayList<TaxaServico> taxasServico = new ArrayList<TaxaServico>();
	private String banco, agencia, contaCorrente;

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

	public ArrayList<TaxaServico> getTaxasServico() {
		return taxasServico;
	}

	public void setTaxasServico(ArrayList<TaxaServico> taxasServico) {
		this.taxasServico = taxasServico;
	}

	public String getBanco() {
		return banco;
	}

	public void setBanco(String banco) {
		this.banco = banco;
	}

	public String getAgencia() {
		return agencia;
	}

	public void setAgencia(String agencia) {
		this.agencia = agencia;
	}

	public String getContaCorrente() {
		return contaCorrente;
	}

	public void setContaCorrente(String contaCorrente) {
		this.contaCorrente = contaCorrente;
	}
	
	public void adicionarTaxaServico(TaxaServico taxa) { 
		this.taxasServico.add(taxa); 
	}

	public abstract Empregado copia();

	protected void copiarAtributosPara(Empregado destino) {
		destino.setMetodoPagamento(metodoPagamento);
		destino.setSindicalizado(sindicalizado);
		destino.setIdSindicato(idSindicato);
		destino.setTaxaSindical(taxaSindical);
		destino.setBanco(banco);
		destino.setAgencia(agencia);
		destino.setContaCorrente(contaCorrente);

		for (TaxaServico taxa : taxasServico) {
			destino.adicionarTaxaServico(new TaxaServico(taxa.getData(), taxa.getValor()));
		}
	}
}

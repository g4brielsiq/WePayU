package br.ufal.ic.p2.wepayu.persistencia;

import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;

/**
 * Gerencia a gravação e recuperação dos dados do sistema em arquivos XML
 * utilizando java.beans.XMLEncoder e java.beans.XMLDecoder conforme especificado na disciplina.
 */
public class PersistenciaXML {
	private static final String ARQUIVO_PADRAO = "wepayu_dados.xml";
	private final File arquivo;

	public PersistenciaXML() {
		this(new File(ARQUIVO_PADRAO));
	}

	public PersistenciaXML(File arquivo) {
		this.arquivo = arquivo;
	}

	public void salvar(DadosAplicacao dados) {
		try (XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(arquivo)))) {
			encoder.setExceptionListener(e -> {
				System.err.println("[PersistenciaXML] Erro ao gravar dados no XML: " + e.getMessage());
			});
			configurarEncoder(encoder);
			encoder.writeObject(dados);
		} catch (Exception e) {
			System.err.println("[PersistenciaXML] Nao foi possivel abrir arquivo para gravacao: " + e.getMessage());
		}
	}

	public DadosAplicacao carregar() {
		if (!arquivo.exists() || arquivo.length() == 0) {
			return new DadosAplicacao();
		}
		try (XMLDecoder decoder = new XMLDecoder(
				new BufferedInputStream(new FileInputStream(arquivo)),
				null,
				e -> System.err.println("[PersistenciaXML] Erro ao ler objeto do XML: " + e.getMessage()),
				PersistenciaXML.class.getClassLoader())) {
			Object obj = decoder.readObject();
			if (obj instanceof DadosAplicacao) {
				return (DadosAplicacao) obj;
			}
		} catch (Exception e) {
			System.err.println("[PersistenciaXML] Falha ao ler arquivo de persistencia: " + e.getMessage());
		}
		return new DadosAplicacao();
	}

	public void limpar() {
		if (arquivo.exists()) {
			arquivo.delete();
		}
	}

	public boolean existeArquivo() {
		return arquivo.exists() && arquivo.length() > 0;
	}

	private void configurarEncoder(XMLEncoder encoder) {
		// java.time.LocalDate não possui construtor padrão (no-arg).
		// O PersistenceDelegate instrui o XMLEncoder a instanciar LocalDate através do método estático LocalDate.of(ano, mes, dia).
		encoder.setPersistenceDelegate(LocalDate.class, new PersistenceDelegate() {
			@Override
			protected Expression instantiate(Object oldInstance, Encoder out) {
				LocalDate data = (LocalDate) oldInstance;
				return new Expression(oldInstance, LocalDate.class, "of",
						new Object[]{data.getYear(), data.getMonthValue(), data.getDayOfMonth()});
			}
		});
	}
}

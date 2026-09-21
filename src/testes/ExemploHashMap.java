package testes;

import java.util.HashMap;
import java.util.Map;

public class ExemploHashMap {
	
    public static void main(String[] args) {
    	
        // Criando um mapa onde a Chave é String (o ID) e o Valor é String (o Nome do Empregado)
        Map<String, String> mapaEmpregados = new HashMap<>();

        // Adicionando elementos (put)
        mapaEmpregados.put("id1", "Lucas Gabriel");
        mapaEmpregados.put("id2", "João da Silva");

        // Recuperando um elemento instantaneamente pela chave (get)
        String funcionario = mapaEmpregados.get("id1");
        System.out.println(funcionario); // Imprime: Lucas Gabriel
        
        // System.out.println(mapaEmpregados.getClass());

        // Verificando se uma chave existe (containsKey) - ótimo para as validações do projeto!
        if (mapaEmpregados.containsKey("id3")) {
            System.out.println("Empregado existe!");
        } else {
            System.out.println("Lançar EmpregadoNaoExisteException");
        }
    }
}
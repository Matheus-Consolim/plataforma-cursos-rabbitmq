package br.edu.plataforma.comunicacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"br.edu.plataforma.contrato", "br.edu.plataforma.infra",
        "br.edu.plataforma.comunicacao"})
public class ComunicacaoApplication {
    public static void main(String[] args) {
        SpringApplication.run(ComunicacaoApplication.class, args);
    }
}

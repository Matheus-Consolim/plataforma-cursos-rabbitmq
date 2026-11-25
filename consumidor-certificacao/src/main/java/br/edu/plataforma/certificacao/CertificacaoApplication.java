package br.edu.plataforma.certificacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"br.edu.plataforma.contrato", "br.edu.plataforma.infra",
        "br.edu.plataforma.certificacao"})
public class CertificacaoApplication {
    public static void main(String[] args) {
        SpringApplication.run(CertificacaoApplication.class, args);
    }
}

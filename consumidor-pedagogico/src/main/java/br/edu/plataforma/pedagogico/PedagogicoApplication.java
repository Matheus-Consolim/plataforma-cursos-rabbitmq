package br.edu.plataforma.pedagogico;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"br.edu.plataforma.contrato", "br.edu.plataforma.infra",
        "br.edu.plataforma.pedagogico"})
public class PedagogicoApplication {
    public static void main(String[] args) {
        SpringApplication.run(PedagogicoApplication.class, args);
    }
}

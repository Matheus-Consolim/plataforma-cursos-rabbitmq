package br.edu.plataforma.indicadores;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"br.edu.plataforma.contrato", "br.edu.plataforma.infra",
        "br.edu.plataforma.indicadores"})
public class IndicadoresApplication {
    public static void main(String[] args) {
        SpringApplication.run(IndicadoresApplication.class, args);
    }
}

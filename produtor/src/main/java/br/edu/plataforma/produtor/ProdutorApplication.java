package br.edu.plataforma.produtor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"br.edu.plataforma.contrato", "br.edu.plataforma.produtor"})
public class ProdutorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProdutorApplication.class, args);
    }
}

package br.edu.plataforma.certificacao;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class CertificadosController {

    private final CertificadoRepositorio repositorio;

    public CertificadosController(CertificadoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    /** GET /certificados  ou  GET /certificados?idEvento=... */
    @GetMapping("/certificados")
    public List<Map<String, Object>> listar(@RequestParam(required = false) String idEvento) {
        return repositorio.listar(idEvento);
    }
}

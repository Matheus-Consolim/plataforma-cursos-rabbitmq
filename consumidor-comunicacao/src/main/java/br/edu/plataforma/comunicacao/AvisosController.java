package br.edu.plataforma.comunicacao;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class AvisosController {

    private final AvisoRepositorio avisos;

    public AvisosController(AvisoRepositorio avisos) {
        this.avisos = avisos;
    }

    /** GET /avisos  ou  GET /avisos?tipo=CONCLUSAO|ABANDONO|REVISAO_AVALIACAO */
    @GetMapping("/avisos")
    public List<Map<String, Object>> listar(@RequestParam(required = false) String tipo) {
        return avisos.listar(tipo);
    }
}

package br.edu.plataforma.auditoria;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
public class AuditoriaController {

    private final AuditoriaRepositorio repositorio;

    public AuditoriaController(AuditoriaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * GET /auditoria?idEvento=...&tipo=curso.concluido&de=2026-11-25T00:00:00Z&ate=2026-11-26T00:00:00Z
     * (todos os filtros são opcionais)
     */
    @GetMapping("/auditoria")
    public List<Map<String, Object>> consultar(@RequestParam(required = false) String idEvento,
                                               @RequestParam(required = false) String tipo,
                                               @RequestParam(required = false) Instant de,
                                               @RequestParam(required = false) Instant ate) {
        return repositorio.consultar(idEvento, tipo, de, ate);
    }
}

package br.edu.plataforma.indicadores;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class IndicadoresController {

    private final IndicadoresRepositorio repositorio;

    public IndicadoresController(IndicadoresRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    /** Total de conclusões por curso. */
    @GetMapping("/indicadores")
    public List<Map<String, Object>> totais() {
        return repositorio.totais();
    }

    /** Quantos eventos cada instância processou (detalhe por evento em GET /processados). */
    @GetMapping("/indicadores/instancias")
    public List<Map<String, Object>> porInstancia() {
        return repositorio.porInstancia();
    }
}

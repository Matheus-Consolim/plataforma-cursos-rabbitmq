package br.edu.plataforma.pedagogico;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class AcompanhamentoController {

    private final AcompanhamentoRepositorio repositorio;

    public AcompanhamentoController(AcompanhamentoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    /** GET /avaliacoes  ou  GET /avaliacoes?avaliacaoId=AV-1234 */
    @GetMapping("/avaliacoes")
    public List<Map<String, Object>> listar(@RequestParam(required = false) String avaliacaoId) {
        return repositorio.listar(avaliacaoId);
    }
}

package br.edu.plataforma.produtor;

import br.edu.plataforma.contrato.EventoDominio;
import br.edu.plataforma.contrato.TiposEvento;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API da aplicação produtora. Cada endpoint registra um fato do domínio e publica o evento.
 * O corpo é opcional: campos não informados recebem dados fictícios.
 */
@RestController
@RequestMapping("/eventos")
public class EventosController {

    private final PublicadorEventos publicador;

    public EventosController(PublicadorEventos publicador) {
        this.publicador = publicador;
    }

    @PostMapping("/curso-concluido")
    public EventoDominio cursoConcluido(@RequestBody(required = false) Map<String, Object> corpo) {
        return publicador.publicar(EventoDominio.novo(TiposEvento.CURSO_CONCLUIDO, dadosConclusao(corpo)));
    }

    /** Publica N conclusões distintas (usado na demonstração de trabalho concorrente). */
    @PostMapping("/curso-concluido/lote")
    public List<EventoDominio> lote(@RequestParam(defaultValue = "10") int quantidade) {
        List<EventoDominio> r = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            r.add(publicador.publicar(EventoDominio.novo(TiposEvento.CURSO_CONCLUIDO, dadosConclusao(null))));
        }
        return r;
    }

    @PostMapping("/curso-abandonado")
    public EventoDominio cursoAbandonado(@RequestBody(required = false) Map<String, Object> corpo) {
        Map<String, Object> d = base(corpo);
        DadosFicticios.Curso c = DadosFicticios.curso();
        d.putIfAbsent("cursoId", c.id());
        d.putIfAbsent("cursoNome", c.nome());
        d.putIfAbsent("percentualConcluido", DadosFicticios.percentual());
        d.putIfAbsent("motivo", "Inatividade superior a 30 dias");
        return publicador.publicar(EventoDominio.novo(TiposEvento.CURSO_ABANDONADO, d));
    }

    @PostMapping("/avaliacao-enviada")
    public EventoDominio avaliacaoEnviada(@RequestBody(required = false) Map<String, Object> corpo) {
        Map<String, Object> d = dadosAvaliacao(corpo);
        return publicador.publicar(EventoDominio.novo(TiposEvento.AVALIACAO_ENVIADA, d));
    }

    @PostMapping("/avaliacao-revisada")
    public EventoDominio avaliacaoRevisada(@RequestBody(required = false) Map<String, Object> corpo) {
        Map<String, Object> d = dadosAvaliacao(corpo);
        d.putIfAbsent("nota", DadosFicticios.nota());
        d.putIfAbsent("revisor", "Prof. Ricardo Alves");
        return publicador.publicar(EventoDominio.novo(TiposEvento.AVALIACAO_REVISADA, d));
    }

    /**
     * Publica um evento de tipo livre, para demonstrar regras de roteamento:
     * ex.: "avaliacao.contestada" (novo fato de família acompanhada) ou
     * "matricula.realizada" (não atende regra específica de nenhum consumidor).
     */
    @PostMapping("/personalizado")
    public EventoDominio personalizado(@RequestBody Map<String, Object> corpo) {
        Object tipo = corpo.get("tipo");
        if (tipo == null || tipo.toString().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "informe 'tipo'");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> dados = corpo.get("dados") instanceof Map<?, ?> m
                ? new LinkedHashMap<>((Map<String, Object>) m) : new LinkedHashMap<>();
        return publicador.publicar(EventoDominio.novo(tipo.toString(), dados));
    }

    /** Republica exatamente o mesmo evento (mesmo idEvento) - teste de duplicidade. */
    @PostMapping("/{idEvento}/republicar")
    public EventoDominio republicar(@PathVariable String idEvento) {
        EventoDominio e = publicador.buscar(idEvento);
        if (e == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "evento nao publicado por esta instancia; use POST /eventos/bruto com o JSON completo");
        }
        return publicador.publicar(e);
    }

    /** Publica um evento já pronto (JSON completo do contrato), preservando o idEvento informado. */
    @PostMapping("/bruto")
    public EventoDominio bruto(@RequestBody EventoDominio evento) {
        if (evento.idEvento() == null || evento.tipo() == null || evento.ocorridoEm() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "idEvento, tipo e ocorridoEm sao obrigatorios");
        }
        return publicador.publicar(evento);
    }

    @GetMapping
    public List<EventoDominio> listar() {
        return publicador.listar();
    }

    // ----------------------------------------------------------------------------------

    private Map<String, Object> base(Map<String, Object> corpo) {
        Map<String, Object> d = corpo == null ? new LinkedHashMap<>() : new LinkedHashMap<>(corpo);
        DadosFicticios.Aluno a = DadosFicticios.aluno();
        if (!d.containsKey("alunoId")) {
            d.put("alunoId", a.id());
            d.putIfAbsent("alunoNome", a.nome());
        }
        d.putIfAbsent("alunoNome", "Aluno " + d.get("alunoId"));
        return d;
    }

    private Map<String, Object> dadosConclusao(Map<String, Object> corpo) {
        Map<String, Object> d = base(corpo);
        DadosFicticios.Curso c = DadosFicticios.curso();
        if (!d.containsKey("cursoId")) {
            d.put("cursoId", c.id());
            d.putIfAbsent("cursoNome", c.nome());
            d.putIfAbsent("cargaHoraria", c.cargaHoraria());
        }
        d.putIfAbsent("cursoNome", d.get("cursoId"));
        d.putIfAbsent("cargaHoraria", 40);
        return d;
    }

    private Map<String, Object> dadosAvaliacao(Map<String, Object> corpo) {
        Map<String, Object> d = base(corpo);
        DadosFicticios.Curso c = DadosFicticios.curso();
        d.putIfAbsent("avaliacaoId", DadosFicticios.avaliacaoId());
        d.putIfAbsent("cursoId", c.id());
        d.putIfAbsent("cursoNome", c.nome());
        return d;
    }
}

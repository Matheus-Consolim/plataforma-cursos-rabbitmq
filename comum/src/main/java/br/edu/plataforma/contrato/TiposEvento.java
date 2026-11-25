package br.edu.plataforma.contrato;

/**
 * Tipos de evento. O tipo é usado diretamente como routing key no formato
 * {@code <familia>.<fato>}, o que permite ao exchange topic selecionar por
 * evento ({@code curso.concluido}), por família ({@code avaliacao.#}) ou tudo ({@code #}).
 */
public final class TiposEvento {

    /** Processo principal (família "curso"). */
    public static final String CURSO_CONCLUIDO = "curso.concluido";
    public static final String CURSO_ABANDONADO = "curso.abandonado";

    /** Processo especializado (família "avaliacao"). */
    public static final String AVALIACAO_ENVIADA = "avaliacao.enviada";
    public static final String AVALIACAO_REVISADA = "avaliacao.revisada";

    private TiposEvento() {
    }
}

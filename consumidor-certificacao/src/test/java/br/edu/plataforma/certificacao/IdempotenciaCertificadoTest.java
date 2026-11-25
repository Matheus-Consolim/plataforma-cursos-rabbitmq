package br.edu.plataforma.certificacao;

import br.edu.plataforma.contrato.EventoDominio;
import br.edu.plataforma.contrato.TiposEvento;
import br.edu.plataforma.infra.ProcessadorIdempotente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Mesmo evento (mesmo idEvento) entregue duas vezes => um único certificado. */
class IdempotenciaCertificadoTest {

    private JdbcTemplate jdbc;
    private ProcessadorIdempotente processador;
    private CertificadoRepositorio certificados;

    @BeforeEach
    void preparar() {
        DriverManagerDataSource ds = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        new ResourceDatabasePopulator(new ClassPathResource("schema.sql")).execute(ds);
        jdbc = new JdbcTemplate(ds);
        processador = new ProcessadorIdempotente(jdbc,
                new TransactionTemplate(new DataSourceTransactionManager(ds)), "certificacao-teste");
        certificados = new CertificadoRepositorio(jdbc);
    }

    @Test
    void republicacaoComMesmoIdNaoGeraSegundoCertificado() {
        EventoDominio evento = EventoDominio.novo(TiposEvento.CURSO_CONCLUIDO, Map.of(
                "alunoId", "A-1", "alunoNome", "Ana", "cursoId", "C-JAVA",
                "cursoNome", "Java", "cargaHoraria", 40));

        assertThat(processador.processar(evento, certificados::emitir))
                .isEqualTo(ProcessadorIdempotente.Resultado.PROCESSADO);
        assertThat(processador.processar(evento, certificados::emitir))
                .isEqualTo(ProcessadorIdempotente.Resultado.DUPLICADO);

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM certificados", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT duplicatas_ignoradas FROM eventos_processados WHERE id_evento = ?",
                Integer.class, evento.idEvento())).isEqualTo(1);
    }

    @Test
    void falhaNoEfeitoDesfazORegistroDeProcessado() {
        EventoDominio evento = EventoDominio.novo(TiposEvento.CURSO_CONCLUIDO, Map.of("alunoId", "A-1"));
        try {
            processador.processar(evento, e -> { throw new IllegalStateException("falha"); });
        } catch (IllegalStateException esperado) {
            // a mensagem receberia nack; o idEvento NAO pode ficar marcado como processado
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM eventos_processados", Integer.class)).isZero();
    }
}

package br.edu.plataforma.produtor;

import java.util.List;
import java.util.Random;

/** Gerador simples de dados fictícios para disparar os eventos sem precisar montar o JSON. */
final class DadosFicticios {

    record Aluno(String id, String nome) {}
    record Curso(String id, String nome, int cargaHoraria) {}

    private static final Random R = new Random();

    static final List<Aluno> ALUNOS = List.of(
            new Aluno("A-1001", "Ana Souza"),
            new Aluno("A-1002", "Bruno Lima"),
            new Aluno("A-1003", "Carla Mendes"),
            new Aluno("A-1004", "Diego Rocha"),
            new Aluno("A-1005", "Elisa Martins"),
            new Aluno("A-1006", "Fabio Nunes"));

    static final List<Curso> CURSOS = List.of(
            new Curso("C-JAVA", "Java com Spring Boot", 40),
            new Curso("C-RMQ", "Mensageria com RabbitMQ", 20),
            new Curso("C-CLOUD", "Fundamentos de Cloud Computing", 60),
            new Curso("C-SD", "Sistemas Distribuidos", 80));

    static Aluno aluno() {
        return ALUNOS.get(R.nextInt(ALUNOS.size()));
    }

    static Curso curso() {
        return CURSOS.get(R.nextInt(CURSOS.size()));
    }

    static double nota() {
        return Math.round((5 + R.nextDouble() * 5) * 10) / 10.0;
    }

    static int percentual() {
        return 5 + R.nextInt(80);
    }

    static String avaliacaoId() {
        return "AV-" + (1000 + R.nextInt(9000));
    }

    private DadosFicticios() {
    }
}

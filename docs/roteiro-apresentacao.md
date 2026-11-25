# Roteiro da apresentação (até 20 min)

Sugestão de divisão. Todos devem conseguir explicar o fluxo inteiro.

## Antes de começar (fora do tempo)

```powershell
. .\scripts\ambiente.ps1
mvn -q -DskipTests package
Remove-Item -Recurse -Force data, logs -ErrorAction SilentlyContinue   # estado limpo
.\scripts\iniciar-rabbitmq.ps1        # ou: docker compose up -d
.\scripts\iniciar-todos.ps1 -Janela   # uma janela de log por aplicação
```

Abra o painel <http://localhost:15672> (guest/guest) na aba **Queues and Streams** e deixe as
janelas de log visíveis.

---

## 1. Arquitetura e contrato (3 min), Matheus

* Domínio: plataforma de cursos. Processo principal (`curso.*`) e processo especializado (`avaliacao.*`).
* Diagrama do README (§1): produtor → exchange **topic** `plataforma.eventos` → 5 filas → consumidores.
* Mostrar `docs/exemplo-evento.json`: `idEvento` (idempotência), `tipo` (= routing key), `ocorridoEm`, `dados`.
* Caminho esperado: API → publish com confirm → topic casa padrões → cópia em cada fila → consumidor grava efeito + idEvento numa transação → ack.

## 2. Roteamento (5 min), Carlos

* Mostrar a matriz (§4) e, no painel, *Exchanges → plataforma.eventos → Bindings*.
* Rodar `.\scripts\demo\1-roteamento.ps1`. Ele pausa antes de cada evento e mostra a previsão.
  Explicar cada previsão e só então apertar ENTER.
* Comparar com o painel e com as linhas `OK` e os efeitos listados no fim.
* Justificativa do topic contra direct e fanout (§5): evento, família e abrangente.

## 3. Trabalho concorrente (3 min), Lucas

* Mostrar no painel: fila `plataforma.indicadores` com **2 consumidores**.
* Rodar `.\scripts\demo\2-concorrencia.ps1 -Quantidade 10`.
* Mostrar a tabela "qual instância processou cada evento", que cada evento foi processado uma vez
  e os efeitos nas outras responsabilidades. Não se exige divisão igual.
* Explicar: mesma fila = competing consumers; `prefetch=1`; banco compartilhado + MERGE atômico.

## 4. Interrupção e recuperação (4 min), João

* Rodar `.\scripts\demo\3-interrupcao.ps1`:
  1. para a Comunicação (a janela fecha; no painel, *consumers = 0*);
  2. publica `curso.abandonado`: a Auditoria processa e `plataforma.comunicacao` fica com **1 pronta**;
  3. ENTER: religa a Comunicação, a pendência é consumida e o aviso aparece.
* Explicar: fila durável + mensagem persistente; cada consumidor tem sua fila, então um não bloqueia o outro.

## 5. Duplicidade e defesa (5 min), Yuri

* Rodar `.\scripts\demo\4-duplicidade.ps1`: 1 certificado antes e depois; `duplicatas_ignoradas=1`.
  Mostrar no log da Certificação `[ACK] ... resultado=DUPLICADO (efeito NAO repetido)`.
* Explicar o ack (README §7): ack só após o commit; duplicado recebe ack sem efeito; falha gera
  nack com requeue e depois DLQ (`.\scripts\demo\6-falha-e-dlq.ps1` se der tempo).
* Perguntas prováveis: `.\scripts\demo\5-novo-evento-e-sem-regra.ps1`.

---

## Perguntas prováveis e respostas curtas

**E se surgir `avaliacao.contestada`?**
Casa com `avaliacao.#` e `#`. Pedagógico e Auditoria recebem sem mudar nada. Os demais não, pois têm bindings exatos.

**E um evento que não atende regra de ninguém (`matricula.realizada`)?**
Só a Auditoria (`#`). Se nem ela existisse, o broker descartaria a mensagem. Como publicamos com `mandatory=true`, o produtor registraria `[NAO ROTEADA]`.

**E se a regra mudar? (ex.: Comunicação também deve avisar abandono de avaliação)**
Adiciona-se um binding na fila `plataforma.comunicacao` (painel, `definitions.json` e `Topologia.java`) e o caso no `switch` do `AvisoRepositorio`. O produtor não muda.

**Por que não fanout?**
Todo mundo receberia tudo e filtraria no código, misturando regra de roteamento com regra de negócio e desperdiçando processamento.

**Por que não direct?**
Funciona para evento exato, mas família e "tudo" exigiriam listar cada tipo. Um tipo novo ficaria de fora em silêncio.

**Quando a mensagem é confirmada?**
Depois do commit da transação que grava o efeito e o `idEvento`. Se o processo cair antes, o broker reentrega, e a idempotência evita efeito duplo.

**O que garante uma execução por evento nos Indicadores?**
Em condições normais, o broker entrega cada mensagem a uma só instância (mesma fila). Em caso de reentrega (queda antes do ack), a tabela `eventos_processados`, compartilhada pelas duas instâncias, impede a contagem dupla.

**Por que dois bancos/tabelas de idempotência e não um global?**
Cada consumidor é dono do seu estado. O mesmo evento *deve* ser processado uma vez por **cada** responsabilidade, não uma vez no sistema todo.

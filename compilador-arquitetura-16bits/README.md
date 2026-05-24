# Compilador para Arquitetura 16 bits

Compilador didatico com interface JavaFX que transforma um codigo de alto nivel (com sintaxe simplificada) em Assembly para uma arquitetura de 16 bits. O projeto inclui visualizacao de registradores e memoria, suporte a estruturas condicionais e de repeticao, e geracao/salvamento de Assembly.

## Requisitos

- Java 21
- Maven 3.8+

## Como executar

```bash
mvn clean javafx:run
```

## Como usar

1. Digite o codigo na area de texto.
2. Clique em **Compilar** ou use o menu **Codigo > Compilar**.
3. Para salvar o Assembly, use **Codigo > Compilar e Salvar Assembly**.
4. Para ver a sintaxe, acesse **Sobre > Sintaxe**.

## Sintaxe suportada

### Declaracao de inteiro (1-15)

```text
inteiro nomeDaVariavel;
inteiro nomeDaVariavel = valor;
```

### Condicional

```text
se (var/const == var/const) {
    // corpo do se
} senao {
    // corpo do senao
}

se (var/const != var/const) {
    // corpo do se
} senao {
    // corpo do senao
}
```

### Enquanto

```text
enquanto (var/const == var/const) {
    // corpo do enquanto
}

enquanto (var/const != var/const) {
    // corpo do enquanto
}
```

### Para

Nao implementado ainda.

### Comentarios

```text
// comentario de linha
```

### Operacoes

- `+` (soma)
- `-` (subtracao)
- `*` (multiplicacao)

Observacoes:
- Soma e subtracao aceitam multiplas operacoes na mesma linha.
- Multiplicacao atualmente aceita apenas operacoes entre variaveis.

## Limitacoes da arquitetura

- Valores devem estar entre 1 e 15 (4 bits).
- 8 registradores (R0-R7).
- Memoria com 16 posicoes (0-15) para spill de variaveis.

## Estrutura do projeto (resumo)

- `src/main/java/com/unipampa/compiladorarquitetura16bits`: codigo-fonte (parser, controladores e utilitarios).
- `src/main/resources/com/unipampa/compiladorarquitetura16bits`: FXML e estilos.
- `pom.xml`: configuracao Maven/JavaFX.

## Exemplo rapido

```text
inteiro a = 1;
inteiro b = 2;
inteiro soma;

soma = a + b;

se (soma == 3) {
    soma = soma + 1;
}
```

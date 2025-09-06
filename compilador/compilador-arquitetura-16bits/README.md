# Compilador para Arquitetura de 16 bits

## Descrição
Este compilador traduz código assembly para uma arquitetura customizada de 16 bits com 8 registradores (R0-R7).

## Conjunto de Instruções Suportadas

### Grupo 1 - Aritmética e Manipulação de Dados
- **LDA Rdest, valor_imediato** - Carrega um valor imediato no registrador destino
- **SUM Rdest, Rf1, Rf2** - Soma Rf1 com Rf2 e armazena em Rdest
- **SUB Rdest, Rf1, Rf2** - Subtrai Rf2 de Rf1 e armazena em Rdest
- **MUL Rdest, Rf1, Rf2** - Multiplica Rf1 por Rf2 e armazena em Rdest

### Grupo 2 - Controle de Fluxo
- **JMP endereço** - Desvia incondicionalmente para o endereço indicado
- **BEQ Rf1, Rf2, endereço** - Desvia se Rf1 for igual a Rf2
- **BNE Rf1, Rf2, endereço** - Desvia se Rf1 for diferente de Rf2

## Formato das Instruções (16 bits)

### Instruções Aritméticas
- **LDA**: OPCODE(4) + REG_DEST(3) + VALOR_IMEDIATO(9)
- **SUM/SUB/MUL**: OPCODE(4) + REG_DEST(3) + REG1(3) + REG2(3) + UNUSED(3)

### Instruções de Controle
- **JMP**: OPCODE(4) + ENDERECO(12)
- **BEQ/BNE**: OPCODE(4) + REG1(3) + REG2(3) + ENDERECO(6)

## Códigos de Operação (OPCODE)
- LDA: 0001
- SUM: 0010
- SUB: 0011
- MUL: 0100
- JMP: 1001
- BEQ: 1010
- BNE: 1011

## Exemplos de Uso

### Exemplo 1: Operações Básicas
```assembly
LDA R0, 10      ; Carrega 10 em R0
LDA R1, 5       ; Carrega 5 em R1
SUM R2, R0, R1  ; R2 = R0 + R1 (15)
SUB R3, R0, R1  ; R3 = R0 - R1 (5)
MUL R4, R0, R1  ; R4 = R0 * R1 (50)
```

### Exemplo 2: Uso de Labels e Saltos
```assembly
LDA R0, 10
LDA R1, 10
BEQ R0, R1, igual
JMP fim

igual:
LDA R2, 1

fim:
LDA R3, 0
```

### Exemplo 3: Loop Simples
```assembly
LDA R0, 5       ; Contador
LDA R1, 0       ; Acumulador

loop:
SUM R1, R1, R0  ; Soma o contador ao acumulador
SUB R0, R0, 1   ; Decrementa contador
BNE R0, R0, loop ; Se R0 != 0, volta ao loop
```

## Limitações
- Valores imediatos: 0-511 (9 bits)
- Endereços: 0-4095 (12 bits para JMP, 6 bits para BEQ/BNE)
- Registradores: R0-R7
- Comentários começam com ';'

## Como Usar a Interface
1. Digite o código assembly na área de texto superior
2. Clique em "Compilar"
3. O código binário aparecerá na área de saída
4. Labels e seus endereços serão listados ao final

## Tratamento de Erros
O compilador detecta e reporta:
- Instruções não reconhecidas
- Sintaxe incorreta
- Registradores inválidos
- Valores fora do range
- Labels duplicados
- Endereços inválidos

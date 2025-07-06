
---

# 🧠 Projeto de Arquitetura Digital de 16 Bits

**Disciplina:** Projeto Digital I  
**Professor:** Fábio Ramos

Este repositório contém a implementação de um **microprocessador didático de 16 bits**, com um conjunto de instruções customizado, semelhante ao MIPS. O projeto foi desenvolvido como parte da disciplina **Projeto Digital 1** e implementado utilizando a ferramenta **Logisim**.

---

## 📐 Características da Arquitetura

- **Arquitetura de 16 bits:** Barramento de dados e registradores manipulam 16 bits.
- **Banco de Registradores:** 8 registradores de uso geral (`R0` a `R7`).
- **ISA (Instruction Set Architecture):** Instruções customizadas para operações aritméticas e de controle de fluxo.

---

## 🧾 Conjunto de Instruções (ISA)

### ✅ Grupo 1 — Aritmética e Manipulação de Dados

| Instrução | Sintaxe                             | Descrição |
|-----------|-------------------------------------|-----------|
| `LDA`     | `LDA Rdest, valor_imediato`         | Carrega um valor imediato no registrador destino |
| `SUM`     | `SUM Rdest, Rsrc1, Rsrc2`           | Soma `Rsrc1` com `Rsrc2` e armazena em `Rdest` |
| `SUB`     | `SUB Rdest, Rsrc1, Rsrc2`           | Subtrai `Rsrc2` de `Rsrc1` e armazena em `Rdest` |
| `MUL`     | `MUL Rdest, Rsrc1, Rsrc2`           | Multiplica `Rsrc1` por `Rsrc2` e armazena em `Rdest` |

### ✅ Grupo 2 — Controle de Fluxo

| Instrução | Sintaxe                             | Descrição |
|-----------|-------------------------------------|-----------|
| `JMP`     | `JMP endereço`                      | Desvia incondicionalmente para o endereço indicado |
| `BEQ`     | `BEQ Rsrc1, Rsrc2, endereço`        | Desvia se `Rsrc1` for igual a `Rsrc2` |
| `BNE`     | `BNE Rsrc1, Rsrc2, endereço`        | Desvia se `Rsrc1` for diferente de `Rsrc2` |

---

## 🖼️ Arquitetura

Visão geral do datapath implementado no Logisim:

![Circuito no Logisim](/assets/arquitetura_06072025.png)



---

## 🧪 Como Testar o Projeto

### 🔧 Pré-requisitos

- [Logisim](https://sourceforge.net/projects/circuit/) — Versão recomendada.
- GCC — Compilador C (Recomendável recompilar o código .c).

---

### ▶️ Passos para Execução

#### 1. Clone o Repositório

```bash
git clone https://github.com/Beforg/Projeto-Digital.git

```
Compile o `assembler.c` para garantir o funcionamento.

```bash
gcc assembler.c -o assembler
```

#### 3. Escreva e Monte seu Código

Execute o montador e escreva o código (ao final, insira o nome do arquivo + .txt)

```bash
./assembler
```

Digite seu código linha por linha no terminal:

```
LDA,R1,10
LDA,R2,12
SUM,R3,R1,R2
JMP,3
```

---

#### 4. Abra o Circuito no Logisim

* Inicie o Logisim.
* Abra o arquivo `mini-arquitetura.circ`.
* Verifique se **todos os arquivos `.circ`** do projeto estão na mesma pasta para evitar falhas de carregamento.

---

#### 5. Carregue o Programa na Memória

* Localize o componente de RAM.
* Clique com o botão direito e selecione **Editar Conteúdo -> Carregar**.
* Escolha o arquivo `.txt` gerado pelo montador.

---

#### 6. Execute a Simulação

* Use `Ctrl+K` para avançar o clock manualmente (passo a passo).
* Use `Ctrl+E` para alternar entre simulação contínua e pausada.
* Observe os valores nos registradores e o fluxo de controle.

---


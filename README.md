
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
| `SUM`     | `SUM Rdest, Rf1, Rf2`           | Soma `Rf1` com `Rf2` e armazena em `Rdest` |
| `SUB`     | `SUB Rdest, Rf1, Rf2`           | Subtrai `Rf2` de `Rf1` e armazena em `Rdest` |
| `MUL`     | `MUL Rdest, Rf1, Rf2`           | Multiplica `Rf1` por `Rf2` e armazena em `Rdest` |

### ✅ Grupo 2 — Controle de Fluxo

| Instrução | Sintaxe                             | Descrição |
|-----------|-------------------------------------|-----------|
| `JMP`     | `JMP endereço`                      | Desvia incondicionalmente para o endereço indicado |
| `BEQ`     | `BEQ Rf1, Rf2, endereço`        | Desvia se `Rf1` for igual a `Rf2` |
| `BNE`     | `BNE Rf1, Rf2, endereço`        | Desvia se `Rf1` for diferente de `Rf2` |

---

## 🖼️ Arquitetura

Visão geral do datapath implementado no Logisim:

[Montagem da Parte de Controle](/fsm/controle-fsm.pdf)

![Circuito no Logisim](/assets/arquitetura_08072025.png)



---

## 🧪 Como Testar o Projeto

### 🔧 Pré-requisitos

- [Logisim](https://sourceforge.net/projects/circuit/)
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
[00]> LDA,R1,4
[01]> LDA,R2,5
[02]> SUM,R3,R1,R2
[03]> JMP,3
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

### Créditos

- [Bruno Forgiarini](https://github.com/Beforg)
- [Ely Neto](https://github.com/netoe1)
- [Pablo Henrique](https://github.com/pablo-ferz)
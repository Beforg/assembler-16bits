/*
 * assembler.c
 * * Copyright 2025, Desenvolvido por Bruno Forgiarini <www.beforg.my>
 * * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 * * */
#include <stdio.h>
#include <stdint.h> // Essencial para tipos de dados com tamanho fixo, como uint16_t.
#include <stdlib.h> // Para funções como atoi().
#include <string.h> // Para manipulação de strings, como strtok(), strcpy() e strcasecmp().

/**
 * @brief Definição dos Opcodes da arquitetura.
 * Usar #define torna o código mais legível, substituindo números "mágicos" (0, 1, 2...) por nomes significativos.
 */
#define OPCODE_LDA 0b000
#define OPCODE_SUM 0b001
#define OPCODE_SUB 0b010
#define OPCODE_MUL 0b011
#define OPCODE_JMP 0b100
#define OPCODE_BNE 0b101
#define OPCODE_BEQ 0b110

/**
 * @brief Constantes que definem os limites da arquitetura.
 * Centralizar esses valores facilita a manutenção do código.
 */
#define MAX_INSTRUCOES 16
#define MAX_REGISTRADORES 7 // O índice máximo do registrador (de 0 a 7).
#define MAX_IMMEDIATE 15    // O valor máximo para imediatos e endereços (4 bits).

/**
 * @brief Monta uma única linha de código Assembly para seu equivalente em código de máquina de 16 bits.
 * @param linha A string contendo a instrução a ser montada.
 * @return A instrução de 16 bits montada como um uint16_t, ou -1 em caso de erro.
 */
int16_t montarInstrucao(char *linha) {
    uint16_t instrucao;
    char *token;
    // Delimitadores para quebrar a string: espaço, vírgula, 'R' e nova linha.
    char *delimitadores = " ,R\n"; 
    
    // Pega o primeiro "pedaço" (token) da string, que deve ser o mnemônico (ex: "LDA").
    token = strtok(linha, delimitadores);
    
    // Se não houver token, a linha está vazia ou contém apenas espaços.
    if (token == NULL) {
        printf("Linha vazia\n");
        return -1;
    }
    
    // Compara o token com os mnemônicos conhecidos, ignorando maiúsculas/minúsculas.
    if (strcasecmp(token, "LDA") == 0) {
        uint16_t regD, immediate;
        
        // Pega o próximo token (o número do registrador de destino).
        token = strtok(NULL, delimitadores);
        if (token == NULL) {
            printf("Erro: falta o Registrador Destino.\n");
            return -1;
        }
        regD = atoi(token); // Converte a string (ex: "5") para um inteiro (5).
        
        // Pega o próximo token (o valor imediato).
        token = strtok(NULL, delimitadores);
        if (token == NULL) {
            printf("Erro: falta o valor Imediato.\n");
            return -1;
        }
        immediate = atoi(token);
        
        // --- Validação dos operandos ---
        if (immediate > MAX_IMMEDIATE) {
            printf("Erro: Valor imediato %d excede o valor máximo (%d)\n", immediate, MAX_IMMEDIATE);
            return -1;
        }
        
        if (regD > MAX_REGISTRADORES) {
            printf("Erro: Valor do registrador R%d excede o máximo (R%d)\n", regD, MAX_REGISTRADORES);
            return -1;
        }
        
        // Montagem da instrução usando operações de bits (deslocamento e OU).
        instrucao = (OPCODE_LDA << 13) | (regD << 10) | (immediate);
        
    } else if(strcasecmp(token, "SUM") == 0 || strcasecmp(token, "SUB") == 0 || strcasecmp(token, "MUL") == 0) {
        uint16_t opcode, regD, rf1, rf2;
        
        // Determina o opcode correto com base no mnemônico.
        if (strcasecmp(token, "SUM") == 0) opcode = OPCODE_SUM;
        else if (strcasecmp(token, "SUB") == 0) opcode = OPCODE_SUB;
        else opcode = OPCODE_MUL;
        
        // Lê os três operandos (registradores) sequencialmente.
        token = strtok(NULL, delimitadores);
        if (token == NULL) { printf("Erro: falta o Registrador Destino.\n"); return -1; }
        regD = atoi(token);
        
        token = strtok(NULL, delimitadores);
        if (token == NULL) { printf("Erro: falta o Registrador Fonte 1.\n"); return -1; }
        rf1 = atoi(token);
        
        token = strtok(NULL, delimitadores);
        if (token == NULL) { printf("Erro: falta o Registrador Fonte 2.\n"); return -1; }
        rf2 = atoi(token);
        
        // --- Validação dos registradores ---
        // CORREÇÃO: A validação deve ser '>', não '>='.
        if (regD > MAX_REGISTRADORES) { printf("Erro: Registrador de destino R%d invalido.\n", regD); return -1; }
        if (rf1 > MAX_REGISTRADORES) { printf("Erro: Registrador Fonte 1 R%d invalido.\n", rf1); return -1; }
        if (rf2 > MAX_REGISTRADORES) { printf("Erro: Registrador Fonte 2 R%d invalido.\n", rf2); return -1; }
        
        // Montagem da instrução no formato R (Registrador).
        instrucao = (opcode << 13) | (regD << 10) | (rf1 << 7) | (rf2 << 4);
        
    } else if(strcasecmp(token, "JMP") == 0) {
        uint16_t endereco;
        
        token = strtok(NULL, delimitadores);
        if (token == NULL) { printf("Erro: Falta o endereço para JMP.\n"); return -1; }
        endereco = atoi(token);
        
        if (endereco > MAX_IMMEDIATE) {
            printf("Erro: Endereço de JMP não pode ser maior do que %d\n", MAX_IMMEDIATE);
            return -1;
        }
        
        // Montagem da instrução de desvio incondicional.
        instrucao = (OPCODE_JMP << 13) | (endereco);
    
    } else if(strcasecmp(token, "BEQ") == 0 || strcasecmp(token, "BNE") == 0) {
        uint16_t rf1, rf2, endereco, opcode;

        if (strcasecmp(token, "BNE") == 0) opcode = OPCODE_BNE;
        else opcode = OPCODE_BEQ;

        // Leitura dos operandos (2 registradores e 1 endereço).
        token = strtok(NULL, delimitadores);
        if (token == NULL) { printf("ERRO: Faltando registrador fonte 1.\n"); return -1; }
        rf1 = atoi(token);

        token = strtok(NULL, delimitadores);
        if (token == NULL) { printf("ERRO: Faltando registrador fonte 2.\n"); return -1; }
        rf2 = atoi(token);
        
        token = strtok(NULL, delimitadores);
        if (token == NULL) { printf("ERRO: Faltando endereco de desvio.\n"); return -1; }
        endereco = atoi(token);

        // --- Validação dos operandos ---
        // CORREÇÃO: A validação deve ser '>', não '>='.
        if (rf1 > MAX_REGISTRADORES) { printf("ERRO: Registrador fonte 1 R%d invalido.\n", rf1); return -1; }
        if (rf2 > MAX_REGISTRADORES) { printf("ERRO: Registrador fonte 2 R%d invalido.\n", rf2); return -1; }
        if (endereco > MAX_IMMEDIATE) { printf("ERRO: Endereco de desvio %d eh maior que %d.\n", endereco, MAX_IMMEDIATE); return -1; }

        // Montagem da instrução de desvio condicional.
        // O campo Rd não é usado, então ele não é incluído na montagem (permanece 0).
        instrucao = (opcode << 13) | (rf1 << 7) | (rf2 << 4) | (endereco);
    } else {
        printf("Instrução '%s' inválida.\n", token);
        return -1;
    }
    
    // Se tudo correu bem, retorna a instrução de 16 bits montada.
    return instrucao;
}

/**
 * @brief Exibe um número de 16 bits em formato binário, com espaços para legibilidade.
 */
void exibirBinario(uint16_t n) {
    // Loop de 15 a 0 para varrer todos os bits da esquerda para a direita.
    for (int i = 15; i >= 0; i--) {
        // Usa uma máscara de bits para verificar se o i-ésimo bit está ligado.
        // (n & (1 << i)) resulta em verdadeiro se o bit for 1, e falso se for 0.
        printf("%c", (n & (1 << i)) ? '1' : '0');
        // Adiciona espaços nos limites dos campos da instrução para facilitar a leitura.
        if (i == 13 || i == 10 || i == 7 || i == 4) {
            printf(" "); 
        }
    }
}

/**
 * @brief Exibe um guia de ajuda com todas as instruções disponíveis e seus formatos.
 */
void exibirInstrucoes() {
    printf("\n--- Guia de Instrucoes da Arquitetura ---\n");
    printf("Os operandos devem seguir os seguintes limites:\n");
    printf("  - Rd, Rf1, Rf2: Registradores de R0 a R7.\n");
    printf("  - Imediato, Endereco: Valores numericos de 0 a 15.\n\n");
    printf("Instrucao | Formato de Uso           | Descricao\n");
    printf("----------|--------------------------|--------------------------------------------------\n");
    printf("LDA       | LDA Rd, Imediato         | Carrega um valor Imediato no registrador Rd.\n");
    printf("SUM       | SUM Rd, Rf1, Rf2         | Soma o valor de Rf1 e Rf2 e armazena em Rd.\n");
    printf("SUB       | SUB Rd, Rf1, Rf2         | Subtrai o valor de Rf1 por Rf2 e armazena em Rd.\n");
    printf("MUL       | MUL Rd, Rf1, Rf2         | Multiplica o valor de Rf1 por Rf2 e armazena em Rd.\n");
    printf("JMP       | JMP Endereco             | Salta (pula) incondicionalmente para o Endereco.\n");
    printf("BNE       | BNE Rf1, Rf2, Endereco   | Salta para o Endereco se o valor de Rf1 for diferente de Rf2.\n");
    printf("BEQ       | BEQ Rf1, Rf2, Endereco   | Salta para o Endereco se o valor de Rf1 for igual a Rf2.\n");
    printf("--------------------------------------------------------------------------------------\n");
    printf("Comandos do Montador:\n");
    printf("  - Digite 'ajuda' para ver este guia novamente.\n");
    printf("  - Digite 'fim' para finalizar a entrada e montar o codigo.\n\n");
}

/**
 * @brief Salva o código de máquina em um arquivo de texto compatível com o Logisim.
 * @param nome_arquivo O nome do arquivo a ser criado (ex: "programa.txt").
 * @param codigo_maquina Ponteiro para o array com as instruções montadas.
 * @param contador_instrucao O número de instruções a serem salvas.
 * @return 0 em caso de sucesso, -1 em caso de erro.
 */
int salvarEmArquivo(const char* nome_arquivo, const uint16_t* codigo_maquina, int contador_instrucao) {
    // Abre o arquivo em modo de escrita ("w"). Se o arquivo existir, será sobrescrito.
    FILE* arquivo = fopen(nome_arquivo, "w");

    // É crucial verificar se o arquivo foi aberto com sucesso.
    if (arquivo == NULL) {
        perror("ERRO: Nao foi possivel abrir o arquivo para escrita");
        return -1;
    }
    // Escreve o cabeçalho "v2.0 raw", exigido pelo Logisim.
    fprintf(arquivo, "v2.0 raw\n");
    
    // Itera sobre todas as instruções montadas.
    for (int i = 0; i < contador_instrucao; i++) {
        // Escreve cada instrução em hexadecimal com 4 dígitos, em uma nova linha.
        fprintf(arquivo, "%04X\n", codigo_maquina[i]);
    }
    
    // Fecha o arquivo para garantir que os dados sejam gravados no disco.
    fclose(arquivo);

    printf("\n>>> Codigo salvo com sucesso no arquivo '%s'\n", nome_arquivo);
    return 0;
}

/**
 * @brief Função principal que controla o fluxo do programa.
 */
int main() {
    char linha[100];
    uint16_t codigoAssembly[MAX_INSTRUCOES] = {0}; // Array para armazenar o código de máquina.
    int contadorInstrucao = 0; // Contador para saber quantas instruções já foram inseridas.

    // --- Seção de Boas-Vindas ---
    printf("============ Montador para Mini-Arquitetura 16-bits ============\n");
    printf("Projeto Digital I | Prof. Fábio Ramos | Engenharia de Computação\n");
    printf("================================================================\n");
	exibirInstrucoes(); // Mostra o guia de ajuda logo no início.
	
    // Loop principal para ler a entrada do usuário.
    while (contadorInstrucao < MAX_INSTRUCOES) {
        printf("[%02d]> ", contadorInstrucao); // Exibe o prompt com o endereço atual.
        
        // Lê uma linha inteira da entrada padrão (teclado).
        if (fgets(linha, sizeof(linha), stdin) == NULL) {
            break; // Sai do loop se o usuário pressionar Ctrl+D (fim de entrada).
        }

        // Verifica se o usuário digitou "fim" para encerrar a montagem.
        if (strcasecmp(linha, "fim\n") == 0 || strcasecmp(linha, "fim\r\n") == 0) {
            break;
        }

        // Verifica se o usuário pediu ajuda.
        if (strcasecmp(linha, "ajuda\n") == 0 || strcasecmp(linha, "ajuda\r\n") == 0) {
            exibirInstrucoes();
            continue; // Pula para a próxima iteração do loop, sem incrementar o contador.
        }
        
        // strtok modifica a string original, então criamos uma cópia para preservar a original se necessário.
        char linha_copia[100];
        strcpy(linha_copia, linha);

        // Chama a função principal para montar a instrução.
        int16_t instrucaoMontada = montarInstrucao(linha_copia);

        // Se a montagem foi bem-sucedida (retorno diferente de -1).
        if (instrucaoMontada != -1) {
            codigoAssembly[contadorInstrucao] = instrucaoMontada; // Armazena a instrução.
            contadorInstrucao++; // Incrementa o contador.
        } else {
            printf(">>> ERRO: Instrucao invalida. Digite 'ajuda' para ver os formatos corretos.\n");
        }
    }

    // --- Seção de Exibição e Salvamento ---
    printf("\n=== Codigo de Maquina Gerado ===\n");
    printf("Endereco | Hexadecimal | Binario (Op Rd Rf1 Rf2 Imm)\n");
    printf("----------------------------------------------------------------\n");

    for (int i = 0; i < contadorInstrucao; i++) {
        printf("  %02d     |    %04X     | ", i, codigoAssembly[i]);
        exibirBinario(codigoAssembly[i]);
        printf("\n");
    }
    
    // Só tenta salvar se alguma instrução foi de fato montada.
    if (contadorInstrucao > 0) {
        char nomeArquivo[100];
        printf("\nDigite o nome do arquivo para salvar (ex: programa.txt): ");
        
        if (fgets(nomeArquivo, sizeof(nomeArquivo), stdin) != NULL) {
            // Remove o '\n' do final da string lida por fgets.
            nomeArquivo[strcspn(nomeArquivo, "\n\r")] = 0;
            salvarEmArquivo(nomeArquivo, codigoAssembly, contadorInstrucao);
        }
        printf("\nCarregue os valores na memória do circuito do Logisim.\n");
    } else {
        printf("\nNenhuma instrucao foi montada. Nenhum arquivo foi salvo.\n");
    }
    
    return 0; // Finaliza o programa com sucesso.
}

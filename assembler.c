/*
 * assembler.c
 * 
 * Copyright 2025, Desenvolvido por Bruno Forgiarini <www.beforg.my>
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 * 
 * 
 */
#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

/*
 * Definição das instruções da arquitetura.
 */ 

#define OPCODE_LDA 0b000
#define OPCODE_SUM 0b001
#define OPCODE_SUB 0b010
#define OPCODE_MUL 0b011
#define OPCODE_JMP 0b100
#define OPCODE_BNE 0b101
#define OPCODE_BEQ 0b110

/*
 * --- Definição dos valores máximos ---
 * instruções; 
 * Registradores;
 * Imediato.
 */

#define MAX_INSTRUCOES 16
#define MAX_REGISTRADORES 7
#define MAX_IMMEDIATE 15



int16_t montarInstrucao(char *linha) {
	uint16_t instrucao;
	char *token;
	char *delimitadores = " ,R\n";
	
	token = strtok(linha, delimitadores);
	
	if (token == NULL) {
		printf("Linha vazia");
		return -1;
	}
	
	if (strcasecmp(token, "LDA") == 0) {
		uint16_t regD, immediate;
		
		token = strtok(NULL, delimitadores);
		if (token == NULL) {
			printf("Erro: falta o Registrador Destino.");
			return -1;
		}
		regD = atoi(token);
		
		token = strtok(NULL, delimitadores);
		
		if (token == NULL) {
			printf("Erro: falta o valor Imediato.");
			return -1;
		}
		immediate = atoi(token);
		
		if (immediate > MAX_IMMEDIATE) {
			printf("Erro: Valor %d excede o valor máximo (15)", immediate);
			return -1;
		}
		if (regD > MAX_REGISTRADORES) {
			printf("Valor R%d excede o registrador máximo (7)", immediate);
			return -1;
		}
		
		instrucao = (OPCODE_LDA << 13) | (regD << 10) | (immediate);
		
	} else if(strcasecmp(token, "SUM") == 0 || strcasecmp(token, "SUB") == 0 || strcasecmp(token, "MUL") == 0) {
		uint16_t opcode, regD, rf1, rf2;
		
		if (strcasecmp(token, "SUM") == 0) {
			opcode = OPCODE_SUM;
		} else if (strcasecmp(token, "SUB") == 0) {
			opcode = OPCODE_SUB;
		} else {
			opcode = OPCODE_MUL;
		}		
		
		token = strtok(NULL, delimitadores);
		if (token == NULL) {
			printf("Erro: falta o Registrador Destino.");
			return -1;
		}
		regD = atoi(token);
		
		token = strtok(NULL, delimitadores);
		if (token == NULL) {
			printf("Erro: falta o Registrador Fonte 1.");
			return -1;
		}
		rf1 = atoi(token);
		
		token = strtok(NULL, delimitadores);
		if (token == NULL) {
			printf("Erro: falta o Registrador Fonte 2.");
			return -1;
		}
		rf2 = atoi(token);
		
		if (regD >= MAX_REGISTRADORES) { 
			printf("Erro: Registrador de destino R%d invalido.\n", regD); 
			return -1; 
		}
		
        if (rf1 >= MAX_REGISTRADORES) { 
			printf("Erro: Registrador Fonte 1 R%d invalido.\n", rf1); 
			return -1;
		}
		
        if (rf2 >= MAX_REGISTRADORES) { 
			printf("Erro: Registrador Fonte 2 R%d invalido.\n", rf2); 
			return -1; 
		}
		
		instrucao = (opcode << 13) | (regD << 10) | (rf1 << 7) | (rf2 << 4);
		
	} else if(strcasecmp(token, "JMP") == 0) {
		uint16_t endereco;
		
		token = strtok(NULL, delimitadores);
		
		if (token == NULL) {
			printf("Erro: Falta o endereço");
			return -1;
		}
		endereco = atoi(token);
		
		if (endereco > MAX_IMMEDIATE) {
			printf("Erro: Endereço não pode ser maior do que 15");
			return -1;
		}
		
		instrucao = (OPCODE_JMP << 13) | (endereco);
	
	} else if(strcasecmp(token, "BEQ") == 0 || strcasecmp(token, "BNE") == 0) {
		uint16_t rf1, rf2, endereco, opcode;

        if (strcasecmp(token, "BNE") == 0) {
			opcode = OPCODE_BNE;
		} else {
			opcode = OPCODE_BEQ;
		}

        token = strtok(NULL, delimitadores);
        if (token == NULL) { 
			printf("ERRO: Faltando registrador fonte 1.\n"); 
			return -1; 
		}
        rf1 = atoi(token);

        token = strtok(NULL, delimitadores);
        if (token == NULL) { 
			printf("ERRO: Faltando registrador fonte 2.\n"); 
			return -1; 
		}
        rf2 = atoi(token);
  
        token = strtok(NULL, delimitadores);
        if (token == NULL) { printf("ERRO: Faltando endereco de desvio.\n"); return -1; }
        endereco = atoi(token);

        if (rf1 > MAX_REGISTRADORES) { 
			printf("ERRO: Registrador fonte 1 R%d invalido.\n", rf1); 
			return -1; 
		}
        if (rf2 > MAX_REGISTRADORES) { 
			printf("ERRO: Registrador fonte 2 R%d invalido.\n", rf2); 
			return -1; 
		}
        if (endereco > MAX_IMMEDIATE) { 
			printf("ERRO: Endereco de desvio %d eh maior que 15.\n", endereco); 
			return -1; 
		}

        instrucao = (opcode << 13) | (rf1 << 7) | (rf2 << 4) | (endereco);
	} else {
		printf("Instrução %s inválida.", token);
		return -1;
	}
	
	return instrucao;
}

void exibirBinario(uint16_t n) {
    for (int i = 15; i >= 0; i--) {
        printf("%c", (n & (1 << i)) ? '1' : '0');
        if (i == 13 || i == 10 || i == 7 || i == 4) {
            printf(" "); 
        }
    }
}

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
 * Salva o código de máquina em um arquivo de texto compatível com o Logisim.
 * @param nome_arquivo O nome do arquivo a ser criado (ex: "programa.txt").
 * @param codigo_maquina Ponteiro para o array com as instruções montadas.
 * @param contador_instrucao O número de instruções a serem salvas.
 * @return 0 em caso de sucesso, -1 em caso de erro.
 */
int salvarEmArquivo(const char* nome_arquivo, const uint16_t* codigo_maquina, int contador_instrucao) {
    FILE* arquivo = fopen(nome_arquivo, "w");

    if (arquivo == NULL) {
        perror("ERRO: Nao foi possivel abrir o arquivo para escrita");
        return -1;
    }
    fprintf(arquivo, "v2.0 raw\n");
    for (int i = 0; i < contador_instrucao; i++) {
        fprintf(arquivo, "%04X\n", codigo_maquina[i]);
    }
    fclose(arquivo);

    printf("\n>>> Codigo salvo com sucesso no arquivo '%s'\n", nome_arquivo);

    return 0;
}

int main() {
    char linha[100];
    uint16_t codigoAssembly[MAX_INSTRUCOES] = {0};
    int contadorInstrucao = 0;

    printf("============ Montador para Mini-Arquitetura 16-bits ============\n");
    printf("Projeto Digital I | Prof. Fábio Ramos | Engenharia de Computação\n");
    printf("================================================================\n");
    printf("Digite seu codigo em assembly, uma instrucao por linha.\n");
    printf("Maximo de %d instrucoes. Digite 'fim' para terminar.\n\n", MAX_INSTRUCOES);
	printf("----------------------------------------------------------------\n");
	//sleep(2);
	exibirInstrucoes();
	
    while (contadorInstrucao < MAX_INSTRUCOES) {
        printf("[%02d]> ", contadorInstrucao);
        if (fgets(linha, sizeof(linha), stdin) == NULL) {
            break; 
        }

        if (strcasecmp(linha, "fim\n") == 0 || strcasecmp(linha, "fim\r\n") == 0) {
            break;
        }

        if (strcasecmp(linha, "ajuda\n") == 0 || strcasecmp(linha, "ajuda\r\n") == 0) {
            exibirInstrucoes();
            continue; 
        }
        
        char linha_copia[100];
        strcpy(linha_copia, linha);

        int16_t instrucaoMontada = montarInstrucao(linha_copia);

        if (instrucaoMontada != -1) {
            codigoAssembly[contadorInstrucao] = instrucaoMontada;
            contadorInstrucao++;
        } else {
            printf("Instrucao invalida. Tente novamente.\n");
        }
    }

    printf("\n=== Codigo de Maquina Gerado ===\n");
    printf("Endereco | Hexadecimal | Binario (Op Rd Rf1 Rf2 Imm)\n");
    printf("----------------------------------------------------------------\n");

    for (int i = 0; i < contadorInstrucao; i++) {
        printf("  %02d     |    %04X     | ", i, codigoAssembly[i]);
        exibirBinario(codigoAssembly[i]);
        printf("\n");
    }
    if (contadorInstrucao > 0) {
        char nomeArquivo[100];
        printf("\nDigite o nome do arquivo para salvar (ex: programa.txt): ");
        
        if (fgets(nomeArquivo, sizeof(nomeArquivo), stdin) != NULL) {
            nomeArquivo[strcspn(nomeArquivo, "\n")] = 0;
            salvarEmArquivo(nomeArquivo, codigoAssembly, contadorInstrucao);
        }
        printf("\nCarregue os valores na memória do circuito do Logisim.\n");
    } else {
        printf("\nNenhuma instrucao foi montada. Nenhum arquivo foi salvo.\n");
    }

    

    return 0;
 }

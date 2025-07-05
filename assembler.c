/*
 * assembler.c
 * 
 * Copyright 2025 Bruno Forgiarini <www.beforg.my>
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
#include <stdlib>
#include <string.h>

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
		perror("Linha vazia");
		return -1;
	}
	
	if (strcasecmp(token, "LDA") == 0) {
		uint16_t regD, immediate;
		
		token = strtok(NULL delimitadores);
		if (token == NULL) {
			perror("Erro: falta o Registrador Destino.");
			return -1;
		}
		regD = atoi(token);
		
		token = strtok(NULL, delimitadores);
		
		if (token == NULL) {
			perror("Erro: falta o valor Imediato.");
			return -1;
		}
		immediate = atoi(token);
		
		if (immediate > MAX_IMMEDIATE) {
			perror("Valor %d excede o valor máximo (15)", immediate);
			return -1;
		}
		if (regD > MAX_REGISTRADORES) {
			perror("Valor R%d excede o registrador máximo (7)", immediate);
			return -1;
		}
		
		instrucao = (OPCODE_LDA << 13) | (regD << 10) | (immediate);
		
	} else if(strcasecmp(token, "SUM") == 0 || strcasecmp(token, "SUB") == 0 || strcasecmp(token, "MUL") == 0) {
		uint16_t opcode, regD, rf1, rf2
		
		if (strcasecmp(token, "SUM") == 0) {
			opcode = OPCODE_SUM;
		} else if (strcasecmp(token, "SUB") == 0) {
			opcode = OPCODE_SUB;
		} else {
			opcode = OPCODE_MUL;
		}		
		
		token = strtok(NULL, delimitadores);
		if (token == NULL) {
			perror("Erro: falta o Registrador Destino.");
			return -1;
		}
		regD = atoi(token);
		
		token = strtok(NULL, delimitadores);
		if (token == NULL) {
			perror("Erro: falta o Registrador Fonte 1.");
			return -1;
		}
		rf1 = atoi(token);
		
		token = strtok(NULL, delimitadores);
		if (token == NULL) {
			perror("Erro: falta o Registrador Fonte 2.");
			return -1;
		}
		rf2 = atoi(token);
		
		if (regD >= MAX_REGISTERS) { 
			perror("Erro: Registrador de destino R%d invalido.\n", rd); 
			return -1; 
		}
		
        if (rf1 >= MAX_REGISTERS) { 
			printf("Erro: Registrador Fonte 1 R%d invalido.\n", rf1); 
			return -1;
		}
		
        if (rf2 >= MAX_REGISTERS) { 
			perror("Erro: Registrador Fonte 2 R%d invalido.\n", rf2); 
			return -1; 
		}
		
		instrucao = (opcode << 13) | (regD << 10) | (rf1 << 7) | (rf2 << 4);
		
	} else if(strcasecmp(token, "JMP") == 0) {
		uint16_t endereco;
		
		token = strtok(NULL, delimitadores);
		
		if (token == NULL) {
			perror("Erro: Falta o endereço");
			return -1;
		}
		endereco = atoi(token);
		
		if (endereco > MAX_IMMEDIATE) {
			perror("Erro: Endereço não pode ser maior do que 15");
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

        if (rf1 >= MAX_REGISTERS) { 
			printf("ERRO: Registrador fonte 1 R%d invalido.\n", rf1); 
			return -1; 
		}
        if (rf2 >= MAX_REGISTERS) { 
			printf("ERRO: Registrador fonte 2 R%d invalido.\n", rf2); 
			return -1; 
		}
        if (endereco > MAX_IMMEDIATE) { 
			printf("ERRO: Endereco de desvio %d eh maior que 15.\n", endereco); 
			return -1; 
		}

        instrucao = (opcode << 13) | (rf1 << 7) | (rf2 << 4) | (endereco);
	} else {
		perror("Instrução %s inválida.", token);
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

int main() {
    char linha[100];
    uint16_t codigoAssembly[MAX_INSTRUCTIONS] = {0};
    int contadorInstrucao = 0;

    printf("========== Montador para Mini-Arquitetura 16-bits ==========\n");
    printf("Projeto Digital I | Prof. Fábio Ramos | Engenharia de Computação\n");
    printf("Digite seu codigo em assembly, uma instrucao por linha.\n");
    printf("Maximo de %d instrucoes. Digite 'fim' para terminar.\n\n", MAX_INSTRUCTIONS);

    while (contadorInstrucao < MAX_INSTRUCTIONS) {
        printf("[%02d]> ", contadorInstrucao);
        if (fgets(linha, sizeof(linha), stdin) == NULL) {
            break; 
        }

        if (strcasecmp(linha, "fim\n") == 0 || strcasecmp(linha, "fim\r\n") == 0) {
            break;
        }

        
        char linha_copia[100];
        strcpy(linha_copia, linha);

        int16_t instrucao_montada = montar_instrucao(linha_copia);

        if (instrucao_montada != -1) {
            codigoAssembly[contadorInstrucao] = instrucao_montada;
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
        print_binario(codigoAssembly[i]);
        printf("\n");
    }

    printf("\nCopie e cole os valores hexadecimais na memoria do Logisim.\n");

    return 0;

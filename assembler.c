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
		
		
		
	} else if(strcasecmp(token, "JMP") == 0) {
	
	} else if(strcasecmp(token, "BEQ") == 0 || strcasecmp(token, "BNE") == 0) {
	
	} else {
		perror("Instrução %s inválida.", token);
		return -1;
	}
	
	return instrucao;
}


int main(int argc, char **argv)
{
	
	return 0;
}


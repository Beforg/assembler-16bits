#include "coder.h"
#include "instruction-set.h"
#include "map.h"
#include "assembler.h"
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <ctype.h>

static void settingOpcode(const char opcode[4], char output[17])
{
    memcpy(output, opcode, 3);
}

void codeinstruction_typeJ(const char *instruction_treated, const char *address_treated, char output[MAX_BITS_INSTRUCTION + 1])
{
    if (!instruction_treated || !address_treated || !output)
    {
        fprintf(stderr, "Erro: parâmetro nulo em codeinstruction_typeJ.\n");
        return;
    }

    memset(output, '0', MAX_BITS_INSTRUCTION);
    output[MAX_BITS_INSTRUCTION] = '\0';

    AddressMap address = getAddressStructData(address_treated);
    settingOpcode(OP_JMP_CODE, output);

    if (strcmp(address.code, "-1") == 0)
    {
        fprintf(stderr, "Erro: Código de endereço inválido em codeinstruction_typeJ.\n");
        return;
    }

    if (strlen(address.code) != 4)
    {
        fprintf(stderr, "Erro: Largura de endereço inválido em codeinstruction_typeJ.\n");
        return;
    }

    memcpy(&output[3], address.code, 4);
}

void codeinstruction_typeI(const char *instruction_treated, const char *rd_treated, const char *imed_treated, char output[MAX_BITS_INSTRUCTION + 1])
{
    if (!instruction_treated || !rd_treated || !imed_treated || !output)
    {
        fprintf(stderr, "Erro: parâmetro nulo em codeinstruction_typeI.\n");
        return;
    }

    memset(output, '0', MAX_BITS_INSTRUCTION);
    output[MAX_BITS_INSTRUCTION] = '\0';

    // bits 0–2: opcode
    InstructionMap instr_struct = getInstructionStructData(instruction_treated);
    ImmediateMap imed_struct = getImmediateStructData(imed_treated);
    RegMap rd_struct = getRegStructData(rd_treated);

    if (strcmp(instr_struct.code, "-1") == 0)
    {
        fprintf(stderr, "Erro: instrução inválida em getInstructionStructData\n");
        return;
    }

    if (strcmp(instr_struct.label, I_LDA_LABEL) == 0)
    {
        settingOpcode(OP_LDA_CODE, output);
    }
    else if (strcmp(instr_struct.label, I_STA_LABEL) == 0)
    {
        settingOpcode(OP_STA_CODE, output);
    }

    if (strlen(rd_struct.code) != 3)
    {
        fprintf(stderr, "Erro: Registrador destino com tamanho inválido em codeInstruction_typeI.\n");
        return;
    }

    if (strlen(imed_struct.code) != 4)
    {
        fprintf(stderr, "Erro: Imediato com tamanho inválido em codeInstruction_typeI.\n");
        return;
    }
    memcpy(&output[3], rd_struct.code, 3);
    memcpy(&output[6], imed_struct.code, 4);
}

void codeinstruction_typeB(const char *instruction_treated, const char *r1, const char *r2, const char *address, char output[MAX_BITS_INSTRUCTION + 1])
{
    memset(output, '0', MAX_BITS_INSTRUCTION);
    output[MAX_BITS_INSTRUCTION] = '\0';

    if (!instruction_treated)
    {
        fprintf(stderr, "Erro: instrução inválida nos parametros de codeinstruction_typeB.\n");
        return;
    }

    if (!r1)
    {
        fprintf(stderr, "Erro: r1 inválido nos parametros de codeinstruction_typeB.\n");
        return;
    }

    if (!r2)
    {
        fprintf(stderr, "Erro: r2 inválido nos parametros de codeinstruction_typeB.\n");
        return;
    }

    if (!address)
    {
        fprintf(stderr, "Erro: address inválido nos parametros de codeinstruction_typeB.\n");
        return;
    }

    if (!output)
    {
        fprintf(stderr, "Erro: output inválido nos parametros de codeinstruction_typeB.\n");
        return;
    }

    if (strcmp(B_BNE_LABEL, instruction_treated) == 0)
    {
        settingOpcode(OP_BNE_CODE, output);
    }
    else if (strcmp(B_BEQ_LABEL, instruction_treated) == 0)
    {
        settingOpcode(OP_BEQ_CODE, output);
    }
    else
    {
        fprintf(stderr, "Erro: instrução inválida para typeB.\n");
        return;
    }

    InstructionMap instr_struct = getInstructionStructData(instruction_treated);
    RegMap r1_struct = getRegStructData(r1);
    RegMap r2_struct = getRegStructData(r2);
    AddressMap address_struct = getAddressStructData(address);

    if (strcmp(instr_struct.code, "-1") == 0)
    {
        fprintf(stderr, "Erro: instrução inválida em codeinstruction_typeB.\n");
        return;
    }

    if (strcmp(r1_struct.code, "-1") == 0)
    {
        fprintf(stderr, "Erro: r1 inválido em codeinstruction_typeB.\n");
        return;
    }

    if (strcmp(r2_struct.code, "-1") == 0)
    {
        fprintf(stderr, "Erro: r2 inválido em codeinstruction_typeB.\n");
        return;
    }

    if (strcmp(address_struct.code, "-1") == 0)
    {
        fprintf(stderr, "Erro: address inválido em codeinstruction_typeB.\n");
        return;
    }

    memcpy(&output[3], r1_struct.code, 3);
    memcpy(&output[6], r2_struct.code, 3);
    memcpy(&output[9], address_struct.code, 4);
}

void codeinstruction_typeR(const char *instruction_treated, const char *rd, const char *rf1, const char *rf2, char output[MAX_BITS_INSTRUCTION + 1])
{
    if (!instruction_treated)
    {
        fprintf(stderr, "Erro: Parâmetro de instrução inválido em codeinstruction_typeR.\n");
        return;
    }

    if (!rd)
    {
        fprintf(stderr, "Erro: Parâmetro de Registrador Destino inválido em codeinstruction_typeR.\n");
        return;
    }

    if (!rf1)
    {
        fprintf(stderr, "Erro: Parâmetro de Registrador Fonte inválido em codeinstruction_typeR.\n");
        return;
    }

    if (!rf2)
    {
        fprintf(stderr, "Erro: Parâmetro de Registrador Fonte inválido em codeinstruction_typeR.\n");
        return;
    }

    memset(output, '0', MAX_BITS_INSTRUCTION);
    output[MAX_BITS_INSTRUCTION] = '\0';

    InstructionMap instr_struct = getInstructionStructData(instruction_treated);
    RegMap rf1_struct = getRegStructData(rf1);
    RegMap rf2_struct = getRegStructData(rf2);
    RegMap rd_struct = getRegStructData(rd);

    if (strcmp(instr_struct.code, "-1") == 0)
    {
        fprintf(stderr, "Erro: Código de endereço inválido em codeinstruction_typeR.\n");
        return;
    }

    if (strcmp(rf1_struct.code, "-1") == 0)
    {
        fprintf(stderr, "Erro: RF1 inválido em codeinstruction_typeR.\n");
        return;
    }

    if (strcmp(rf2_struct.code, "-1") == 0)
    {
        fprintf(stderr, "Erro: RF2 inválido em codeinstruction_typeR.\n");
        return;
    }

    if (strcmp(rd_struct.code, "-1") == 0)
    {
        fprintf(stderr, "Erro: RD inválido em codeinstruction_typeR.\n");
        return;
    }

    if (strcmp(R_SUB_LABEL, instr_struct.label) == 0)
    {
        settingOpcode(OP_SUB_CODE, output);
    }
    else if (strcmp(R_SUM_LABEL, instr_struct.label) == 0)
    {
        settingOpcode(OP_SUM_CODE, output);
    }
    else if (strcmp(R_MUL_LABEL, instr_struct.label) == 0)
    {
        settingOpcode(OP_MUL_CODE, output);
    }
    else
    {
        fprintf(stderr, "Erro: Instrução inválida em codeinstruction_typeR.\n");
        return;
    }

    memcpy(&output[3], rd_struct.code, 3);
    memcpy(&output[6], rf1_struct.code, 3);
    memcpy(&output[9], rf2_struct.code, 3);
}

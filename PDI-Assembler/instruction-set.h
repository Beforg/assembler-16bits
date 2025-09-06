#ifndef INSTRUCTION_SET_H
#define INSTRUCTION_SET_H
#include "map.h"

// Defining label for use in table

#define R_SUB_LABEL "sub"
#define R_MUL_LABEL "mul"
#define R_SUM_LABEL "sum"
#define I_LDA_LABEL "lda"
#define I_STA_LABEL "sta"
#define J_JMP_LABEL "jmp"
#define B_BNE_LABEL "bne"
#define B_BEQ_LABEL "beq"

// Defining the codes for architecture.

#define OP_LDA_CODE "000"
#define OP_SUM_CODE "001"
#define OP_SUB_CODE "010"
#define OP_MUL_CODE "011"
#define OP_JMP_CODE "100"
#define OP_BNE_CODE "101"
#define OP_BEQ_CODE "110"
#define OP_STA_CODE "111"

#define R0_LABEL "r0"
#define R1_LABEL "r1"
#define R2_LABEL "r2"
#define R3_LABEL "r3"
#define R4_LABEL "r4"
#define R5_LABEL "r5"
#define R6_LABEL "r6"
#define R7_LABEL "r7"

#define R0_CODE "000"
#define R1_CODE "001"
#define R2_CODE "010"
#define R3_CODE "011"
#define R4_CODE "100"
#define R5_CODE "101"
#define R6_CODE "110"
#define R7_CODE "111"

#define MEM_ADDRESS_0_BIN "0000"
#define MEM_ADDRESS_1_BIN "0001"
#define MEM_ADDRESS_2_BIN "0010"
#define MEM_ADDRESS_3_BIN "0011"
#define MEM_ADDRESS_4_BIN "0100"
#define MEM_ADDRESS_5_BIN "0101"
#define MEM_ADDRESS_6_BIN "0110"
#define MEM_ADDRESS_7_BIN "0111"
#define MEM_ADDRESS_8_BIN "1000"
#define MEM_ADDRESS_9_BIN "1001"
#define MEM_ADDRESS_10_BIN "1010"
#define MEM_ADDRESS_11_BIN "1011"
#define MEM_ADDRESS_12_BIN "1100"
#define MEM_ADDRESS_13_BIN "1101"
#define MEM_ADDRESS_14_BIN "1110"
#define MEM_ADDRESS_15_BIN "1111"

#define MEM_ADDRESS_0_DECIMAL "0"
#define MEM_ADDRESS_1_DECIMAL "1"
#define MEM_ADDRESS_2_DECIMAL "2"
#define MEM_ADDRESS_3_DECIMAL "3"
#define MEM_ADDRESS_4_DECIMAL "4"
#define MEM_ADDRESS_5_DECIMAL "5"
#define MEM_ADDRESS_6_DECIMAL "6"
#define MEM_ADDRESS_7_DECIMAL "7"
#define MEM_ADDRESS_8_DECIMAL "8"
#define MEM_ADDRESS_9_DECIMAL "9"
#define MEM_ADDRESS_10_DECIMAL "10"
#define MEM_ADDRESS_11_DECIMAL "11"
#define MEM_ADDRESS_12_DECIMAL "12"
#define MEM_ADDRESS_13_DECIMAL "13"
#define MEM_ADDRESS_14_DECIMAL "14"
#define MEM_ADDRESS_15_DECIMAL "15"
#define MEM_ADDRESS_16_DECIMAL "16"

// Defines para valores imediatos em decimal (4 bits: 0-15)
#define IMMEDIATE_0_DECIMAL "0"
#define IMMEDIATE_1_DECIMAL "1"
#define IMMEDIATE_2_DECIMAL "2"
#define IMMEDIATE_3_DECIMAL "3"
#define IMMEDIATE_4_DECIMAL "4"
#define IMMEDIATE_5_DECIMAL "5"
#define IMMEDIATE_6_DECIMAL "6"
#define IMMEDIATE_7_DECIMAL "7"
#define IMMEDIATE_8_DECIMAL "8"
#define IMMEDIATE_9_DECIMAL "9"
#define IMMEDIATE_10_DECIMAL "10"
#define IMMEDIATE_11_DECIMAL "11"
#define IMMEDIATE_12_DECIMAL "12"
#define IMMEDIATE_13_DECIMAL "13"
#define IMMEDIATE_14_DECIMAL "14"
#define IMMEDIATE_15_DECIMAL "15"

// Defines para valores imediatos em binário (4 bits)
#define IMMEDIATE_0_BIN "0000"
#define IMMEDIATE_1_BIN "0001"
#define IMMEDIATE_2_BIN "0010"
#define IMMEDIATE_3_BIN "0011"
#define IMMEDIATE_4_BIN "0100"
#define IMMEDIATE_5_BIN "0101"
#define IMMEDIATE_6_BIN "0110"
#define IMMEDIATE_7_BIN "0111"
#define IMMEDIATE_8_BIN "1000"
#define IMMEDIATE_9_BIN "1001"
#define IMMEDIATE_10_BIN "1010"
#define IMMEDIATE_11_BIN "1011"
#define IMMEDIATE_12_BIN "1100"
#define IMMEDIATE_13_BIN "1101"
#define IMMEDIATE_14_BIN "1110"
#define IMMEDIATE_15_BIN "1111"

#define MAX_REG 8
#define MAX_INSTRUCTIONS 8
#define MAX_INSTRUCTION_LABEL_SIZE 3
#define MAX_INSTRUCTION_CODE_SIZE 3
#define MAX_BITS_INSTRUCTION 16
#define MAX_REG_LABEL_SIZE 2
#define MAX_REG_CODE_SIZE 3
#define MAX_IMED_TYPE_I_SIZE 15
#define MAX_ADDRESS_SIZE 16
#define MAX_PROGRAM_MEM_SIZE 15
#define MAX_PROGRAM_STA_SIZE 15

// Labels to use, if you want to make your code more readable.
#define SUCCESSFUL_OPERATION 0
#define FAILURE_OPERATION -1
#define INVALID_REGISTER -1
#define INVALID_INSTRUCTION -1
// Instruct-Set-TypeR => TOKEN_PARSE

// New Feature IGNORE AT ALL!

typedef struct
{
    char op[MAX_INSTRUCTION_LABEL_SIZE + 1];
    char rd[MAX_REG_LABEL_SIZE + 1];
    char rf1[MAX_REG_LABEL_SIZE + 1];
    char rf2[MAX_REG_LABEL_SIZE + 1];

} InstructToken_TypeR;

// Instruct-Set-TypeI => TOKEN_PARSE

typedef struct
{
    char op[MAX_INSTRUCTION_LABEL_SIZE + 1];
    char rd[MAX_REG_CODE_SIZE + 1];
    int imed;
} InstructToken_TypeI;

// Instruct-Set-TypeJ => TOKEN_PARSE
typedef struct
{
    char op[MAX_INSTRUCTION_LABEL_SIZE + 1];
    int end;
} InstructToken_TypeJ;

// Instruct-Set-TypeJ => TOKEN_PARSE
typedef struct
{
    char op[MAX_INSTRUCTION_LABEL_SIZE + 1];
    char reg1[MAX_INSTRUCTION_LABEL_SIZE + 1];
    char reg2[MAX_INSTRUCTION_LABEL_SIZE + 1];
} InstructToken_TypeB;

extern const InstructionMap VALID_INSTRUCTIONS[8];
extern const RegMap VALID_REGISTERS[8];
extern const InstructionMap TYPE_R_INSTR[8];
extern const AddressMap VALID_ADDRESS[16];
extern const ImmediateMap VALID_IMMEDIATE[16];
extern const int MAX_VALID_INSTRUCTIONS;
extern const int MAX_VALID_IMMEDIATE;
extern const int MAX_VALID_ADDRESS;
extern const int MAX_VALID_REGISTER;

// Verify if the code is correct. In another case, returns -1 (INVALID_REGISTER)
int getRegByLabel(char label[3]); // Search if label's statement is correct, returns the index of VALID_REGISTERS; send -1 to an error.
int getRegByCode(char code[4]);
int getInstructionByLabel(char label[4]); // Search if label's statement is correct, returns the index of VALID_REGISTERS send -1 to an error.
int getInstructionByCode(char code[4]);

// This make the same, but return structs. Can be useful in some cases.
InstructionMap getInstructionStructData(const char *instruction_treated); // Returns the struct, returns parsed.
RegMap getRegStructData(const char *reg_treated);                         // Get register, returns parsed;
AddressMap getAddressStructData(const char *address);
ImmediateMap getImmediateStructData(const char *immediate);

void printInstr_typej(InstructToken_TypeJ instr);
void printInstr_typeb(InstructToken_TypeB instr);
void printInstr_typei(InstructToken_TypeI instr);
void printInstr_typer(InstructToken_TypeR instr);
#endif
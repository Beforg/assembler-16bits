#ifndef DEBUG_MODULES_H
#define DEBUG_MODULES_H
#include "instruction-set.h"

#define ERROR_LINE(fmt, ...) \
    fprintf(stderr, "ERR-%s:l.%d: " fmt "\n", currentFile, lineRead, ##__VA_ARGS__)

void print_debug_typeR(char *inst, int line, char *rd, char *rf1, char *rf2);      // Print formatted a instruction TypeR
void print_debug_typeJ(char *inst, int line, int mem_address);                     // Print formatted a instruction TypeJ
void print_debug_typeI(char *inst, int line, char *rd, int imed);                  // Print formatted a instruction Type
void print_debug_typeB(char *inst, int line, char *r1, char *r2, int mem_address); // Print formatted a instruction TypeB
void printdiv(const char *c, int times_side, const char *message);                 // Print divisories.
void print_slash(char *str);                                                       // Print null caracters.
void printInstr_typeb(InstructToken_TypeB instr);
void printInstr_typei(InstructToken_TypeI instr);
void printInstr_typer(InstructToken_TypeR instr);
#endif
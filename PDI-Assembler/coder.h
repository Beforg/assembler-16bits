#ifndef CODER_H
#define CODER_H

#include "instruction-set.h"
#include "map.h"
#include <stdlib.h>

void codeinstruction_typeJ(const char *instruction_treated, const char *address_treated, char output[MAX_BITS_INSTRUCTION + 1]); // Tested!
void codeinstruction_typeI(const char *instruction_treated, const char *rd_treated, const char *imed_treated, char output[MAX_BITS_INSTRUCTION + 1]); // Tested!
void codeinstruction_typeB(const char *instruction_treated, const char *r1, const char *r2, const char *address, char output[MAX_BITS_INSTRUCTION + 1]); // tested!
void codeinstruction_typeR(const char *instruction_treated, const char *rd, const char *rf1, const char *rf2, char output[MAX_BITS_INSTRUCTION + 1]);


#endif
#include "coder.c"
#include <stdio.h>

void testCoderTypeJ(char *instr, char *address, char output[MAX_BITS_INSTRUCTION + 1])
{
    codeinstruction_typeJ(instr, address, output);
    puts(output);
}

void testCoderTypeI(char *instr, char *rd, char *imed, char output[MAX_BITS_INSTRUCTION + 1])
{
    codeinstruction_typeI(instr, rd, imed, output);
    puts(output);
}

void testCoderTypeB(char *instr, char *r1, char *r2, char *address, char output[MAX_BITS_INSTRUCTION + 1])
{
    codeinstruction_typeB(instr, r1, r2, address, output);
    puts(output);
}

void testCoderTypeR(char *instr, char *rd, char *rf1, char *rf2, char output[MAX_BITS_INSTRUCTION + 1])
{
    codeinstruction_typeR(instr, rd, rf1, rf2, output);
    puts(output);
}

int main(void)
{
    char output[17];
    // testCoderTypeJ("jmp", "10", output);
    // testCoderTypeI("lda", "r0", "10", output);
    // testCoderTypeB("beq", "r0", "r1", "10", output);

    testCoderTypeR("sum", "r0", "r0", "r1", output);

    return 0;
}
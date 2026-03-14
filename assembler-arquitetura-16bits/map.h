#ifndef MAP_H
#define MAP_H
#include "instruction-set.h"

typedef enum
{
    TYPE_R,
    TYPE_I,
    TYPE_J,
    TYPE_B,
    INVALID_INSTRUCTION
} InstructionTypes;
typedef struct
{
    char label[2 + 1];
    char code[3 + 1];
} RegMap;

typedef struct
{
    char label[3 + 1];
    char code[3 + 1];
    InstructionTypes type;
} InstructionMap;

typedef struct
{
    char *address;
    char *code;
} AddressMap;

typedef struct
{
    char *value;
    char *code;
} ImmediateMap;
#endif

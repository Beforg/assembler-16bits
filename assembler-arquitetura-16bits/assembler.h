#ifndef ASSEMBLER_H
#define ASSEMBLER_H
#include <stdio.h>
#include <stdlib.h>
#include <locale.h>
#include <string.h>
#include "debug-modules.h"

#define EXTENSION_SUPPORTED ".asmth"

void printCodeOnFile(const char *filename, const char *content);
FILE *readFile(const char *filename);
void assemble(FILE *openedFile);
void closeFile(FILE *closeFile);
void show_help(const char *progname);
void clearFile(const char *filename);
#endif
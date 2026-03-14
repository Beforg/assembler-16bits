#include "assembler.h"
#include "utils.h"
#include "instruction-set.h"
#include "coder.h"
#include "interactive-mode.c"
#include <getopt.h>

#define LINE_LIMIT 256

char *input_file = NULL;
char *output_file = NULL;
void printCodeOnFile(const char *filename, const char *content)
{

    if (filename[0] == '\0' || filename == NULL)
    {
        fprintf(stderr, "Erro: Nome de arquivo inválido em printCodeOnFile.\n");
        return;
    }

    if (content[0] == '\0' || filename == NULL)
    {
        fprintf(stderr, "Erro: Conteúdo para imprimir inválido em printCodeOnFile.\n");
        return;
    }

    char filename_parsed[30];
    filename_parsed[30] = '\0';
    strncpy(filename_parsed, filename, 29);

    FILE *fileToPrint = fopen(filename, "a");

    if (!fileToPrint)
    {
        fprintf(stderr, "Erro: Erro ao abrir o arquivo em printCodeOnFile.\n");
        return;
    }

    fprintf(fileToPrint, "%s\n", content);

    if (fclose(fileToPrint) == -1)
    {

        if (!fileToPrint)
        {
            fprintf(stderr, "Erro: O arquivo já foi fechado ou teve erros ao fechar!\n");
            return;
        }
    }
}
FILE *
readFile(const char *filename)
{

    if (!filename)
    {
        fprintf(stderr, "assembler.c: readfile(): Ponteiro inválido para ler.\n");
        exit(-1);
    }

    FILE *f = fopen(filename, "r");
    if (!f)
    {
        fprintf(stderr, "assembler.c: readfile(): Sem permissão para ler ou o arquivo não existe.\n");
        exit(-1);
    }
    strncpy(input_file, filename, sizeof(input_file) - 1);
    return f;
}
void assemble(FILE *openedFile)
{
    char line[LINE_LIMIT];
    char parsedLine[LINE_LIMIT];
    char output[MAX_BITS_INSTRUCTION + 1];
    char *instruction = NULL;
    char *reg = NULL;
    char *valStr = NULL;
    int val;
    int lineRead = 0;
    unsigned int err = 0;

    while (fgets(line, sizeof(line), openedFile) != NULL)
    {
        lower_str(parsedLine, line, LINE_LIMIT - 1);
        lineRead++;
        if (parsedLine[0] == '\n' || parsedLine[0] == '#' || is_line_empty(parsedLine))
        {
            err = 1;
            continue;
        }

        instruction = strtok(parsedLine, " ");

        if (!instruction) // Verifica se o strtok() foi concluído com sucesso
        {
            fprintf(stderr, "ERR-%s:l.%d: Instrução inválida: %s.\n", input_file, lineRead, instruction);
            err = 1;
            continue;
        }
        sanitize_buffer(instruction);
        InstructionMap instructionParsed = getInstructionStructData(instruction);

        if (strcmp(instructionParsed.code, "-1") == 0)
        {
            fprintf(stderr, "ERR-%s:l.%d: A instrução %s não existe na arquitetura.\n", input_file, lineRead, instruction);
            err = 1;
            continue;
        }

        if (instructionParsed.type == TYPE_I)
        {
            reg = strtok(NULL, ",");

            if (!reg || strlen(reg) == 0)
            {
                fprintf(stderr, "ERR%s:l.%d:Faltando registrador na instrução %s.\n", input_file, lineRead, instruction);
                err = 1;
                continue;
            }
            sanitize_buffer(reg);
            int regn = getRegByLabel(reg);
            if (regn == FAILURE_OPERATION)
            {
                fprintf(stderr, "ERR-%s:l.%d: Registrador %s não existe.\n", input_file, lineRead, reg);
                err = 1;
                continue;
            }

            valStr = strtok(NULL, ",");
            sanitize_buffer(valStr);

            if (!valStr || strlen(valStr) == 0)
            {
                fprintf(stderr, "ERR-%s:l.%d: Valor imediato ausente após registrador '%s'.\n", input_file, lineRead, reg);
                err = 1;
                continue;
            }

            if (!is_number(valStr))
            {
                fprintf(stderr, "ERR-%s:l.%d: Valor %s não é um número válido.\n", input_file, lineRead, valStr);
                err = 1;
                continue;
            }

            val = atoi(valStr);
            if (val < 0 || val > MAX_IMED_TYPE_I_SIZE)
            {
                fprintf(stderr, "ERR-%s:l.%d: Valor imediato fora do intervalo permitido: %d\n", input_file, lineRead, val);
                err = 1;
                continue;
            }
            // print_debug_typeI(instruction, lineRead, reg, val);
            codeinstruction_typeI(instruction, reg, valStr, output);
        }

        else if (instructionParsed.type == TYPE_R) // Instrução TIPO R
        {
            char *rd = strtok(NULL, ",");
            if (!rd)
            {
                fprintf(stderr, "ERR-%s:l.%d: Registrador destino não definido\n", input_file, lineRead);
                err = 1;
                continue;
            }

            char *rf1 = strtok(NULL, ",");
            if (!rf1)
            {
                fprintf(stderr, "ERR-%s:l.%d: Registrador fonte 1 não definido:\n", input_file, lineRead);
                err = 1;
                continue;
            }

            char *rf2 = strtok(NULL, ",");
            if (!rf2)
            {
                fprintf(stderr, "ERR-%s:l.%d: Registrador fonte 2 não definido\n", input_file, lineRead);
                err = 1;
                continue;
            }

            sanitize_buffer(rd);
            sanitize_buffer(rf1);
            sanitize_buffer(rf2);
            // print_debug_typeR(instruction, lineRead, rd, rf1, rf2);

            if (getRegByLabel(rf1) == -1 || getRegByLabel(rf2) == -1)
            {
                fprintf(stderr, "ERR-%s:l.%d: Registradores RF1 E RF2 definidos de forma incorreta!\n", input_file, lineRead);
                err = 1;
                continue;
            }
            codeinstruction_typeR(instruction, rd, rf1, rf2, output);
        }

        else if (instructionParsed.type == TYPE_J) // Instrução TIPO J
        {
            char *mem_address = strtok(NULL, "\n");
            if (!is_number(mem_address))
            {
                fprintf(stderr, "ERR-%s:l.%d: Valor '%s' não é um endereço de memória válido.\n", input_file, lineRead, mem_address);
                err = 1;
                continue;
            }

            int mem_address_int = atoi(mem_address);
            if (mem_address_int < 0 || mem_address_int > MAX_PROGRAM_MEM_SIZE)
            {
                fprintf(stderr, "ERR-%s:l.%d: O valor está fora do limite para a memória de programa.\n", input_file, lineRead);
                err = 1;
                continue;
            }

            // print_debug_typeJ(instruction, lineRead, mem_address_int);
            codeinstruction_typeJ(instruction, mem_address, output);
        }

        else if (instructionParsed.type == TYPE_B) // Instrução TIPO B
        {
            char *r1 = strtok(NULL, ",");
            char *r2 = strtok(NULL, ",");
            char *memAddress = strtok(NULL, ",");

            if (!r1)
            {
                fprintf(stderr, "ERR-%s:l.%d:\nRegistrador r1 não especificado corretamentte.\n", input_file, lineRead);
                err = 1;
                continue;
            }

            if (!r2)
            {
                fprintf(stderr, "ERR-%s:l.%d:\nRegistrador r2 não especificado corretamente.\n", input_file, lineRead);
                err = 1;
                continue;
            }

            if (!memAddress)
            {
                fprintf(stderr, "ERR-%s:l.%d:\nEndereço de memória não válido.\n", input_file, lineRead);
                err = 1;
                continue;
            }

            sanitize_buffer(r1);
            sanitize_buffer(r2);
            sanitize_buffer(memAddress);

            if (!is_number(memAddress))
            {
                fprintf(stderr, "ERR-%s:l.%d:\nEndereço de memória não é um número!\n", input_file, lineRead);
                err = 1;
                continue;
            }

            if (getRegByLabel(r1) == -1 || getRegByLabel(r2) == -1)
            {
                fprintf(stderr, "ERR-%s:l.%d:\nRegistradores não válidos!\n", input_file, lineRead);
                err = 1;
                continue;
            }

            int mem_address_int = atoi(memAddress);

            if (mem_address_int < 0 || mem_address_int > 15)
            {
                fprintf(stderr, "ERR-%s:l.%d:\nA região de memória válida vai de [0-15]!\n", input_file, lineRead);
                err = 1;
                continue;
            }

            // print_debug_typeB(instruction, lineRead, r1, r2, mem_address_int);
            codeinstruction_typeB(instruction, r1, r2, memAddress, output);
        }

        char *sobrou = strtok(NULL, " \n");
        if (sobrou != NULL)
        {
            fprintf(stderr, "ERR-%s:l.%d:\nApenas uma instrução e seus argumentos devem estar por linha.\n", input_file, lineRead);
            err = 1;
            continue;
        }
        if (!err)
            printCodeOnFile(output_file, output);
    }

    puts("assembler-info: ending-parse...");
}
void closeFile(FILE *closeFile)
{
    if (!closeFile)
    {
        perror("assembler.c: closeFile(): Invalid pointer to file.");
        exit(-1);
    }

    if (fclose(closeFile) != 0)
    {
        perror("assembler.c: closeFile(): Error while closing a file.");
        exit(-1);
    }
}
void show_help(const char *progname)
{
    printf("==================Creators:==================\n\n");
    printf("Fábio Luiz Ramos\n");
    printf("Ely Torres Neto             github.com/netoe1\n");
    printf("Bruno Forgiarini            github.com/beforg\n");
    printf("Pablo Fernandez             github.com/pablo_ferz\n\n");
    printf("===================Flags:====================\n\n");
    printf("Usage: %s [options]\n", progname);
    printf("  -c <filename>             File .asm to mount.\n");
    printf("  -o <filename>             Output file.\n");
    printf("  --help or -h              Show flags and how to use.\n");
    printf("  -i or --interactive       Execute assembler on interactive mode!\n\n");
    printf("===================Usage:====================\n\n");
    printf(" %s -c input.asm -o output.txt\n", progname);
}

void clearFile(const char *filename)
{
    FILE *f = fopen(filename, "w");

    if (!f)
    {
        fprintf(stderr, "Erro ao limpar buffer em clearFile.");
        exit(-1);
    }
    if (fclose(f) == -1)
    {
        fprintf(stderr, "Erro ao fechar arquivo em clearFile.");
        exit(-1);
    }
}
int main(int argc, char *argv[])
{
    setlocale(LC_ALL, "");
    int opt;
    if (argc == 1)
    {
        show_help(argv[0]);
        return 0;
    }

    if (argc == 2)
    {
        if (strcmp(argv[1], "--help") == 0 || strcmp(argv[1], "-h") == 0)
        {
            puts("show help");
            show_help(argv[0]);
        }
        else if (strcmp(argv[1], "--interactive") == 0 || strcmp(argv[1], "-i") == 0)
        {
            clearTerminal();
            exec_interactive();
        }
        else
        {
            puts("assembler-err: Invalid statement for 2 args. Check --help!");
        }
        return 0;
    }

    for (int i = 1; i < argc; i++)
    {
        if (strcmp(argv[i], "--help") == 0 || strcmp(argv[i], "-i") == 0 || strcmp(argv[i], "--interactive") == 0)
        {
            puts("assembler-err: --help and --interactive(-i) are used with 2 arguments!");
            return -1;
        }
    }

    while ((opt = getopt(argc, argv, "c:o:")) != -1)
    {
        switch (opt)
        {
        case 'c':
            input_file = optarg;
            break;
        case 'o':
            output_file = optarg;
            break;
        default:
            printf("assembler-err: Incomplete args! Use --help to info!\n");
            return -1;
        }
    }

    if (!input_file || !output_file)
    {
        printf("assembler-err: Incomplete args! Use --help to info!\n");
        return -1;
    }
    FILE *f = readFile(input_file);
    assemble(f);
    closeFile(f);
}

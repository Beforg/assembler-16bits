#include "utils.h"
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>
#include <stddef.h>

void lower_str(char *dst, const char *src, size_t dst_size)
{
    if (!src || !dst || dst_size == 0)
    {
        return; // proteção contra ponteiros nulos e tamanho inválido
    }

    size_t i = 0;
    while (src[i] != '\0' && i < dst_size - 1)
    {
        dst[i] = (char)tolower((unsigned char)src[i]);
        i++;
    }

    dst[i] = '\0'; // garante terminação nula
}
bool is_number(const char *str)
{
    if (!str || *str == '\0')
        return false;

    // Pula o sinal, se existir
    if (*str == '-' || *str == '+')
        str++;

    // Após o sinal, precisa haver ao menos um dígito
    if (*str == '\0')
        return false;

    // Verifica se todos os caracteres restantes são dígitos
    while (*str)
    {
        if (!isdigit((unsigned char)*str))
            return false;
        str++;
    }

    return true;
}
char *trim(char *str)
{
    if (str == NULL)
        return NULL;

    // Ponteiro para o início
    char *start = str;
    while (isspace((unsigned char)*start))
    {
        start++;
    }

    // Ponteiro para o fim
    char *end = str + strlen(str) - 1;
    while (end > start && isspace((unsigned char)*end))
    {
        end--;
    }

    // Novo fim da string
    *(end + 1) = '\0';

    // Se precisar mover a string para o início do buffer:
    if (start != str)
    {
        memmove(str, start, end - start + 2); // +1 para o caractere final, +1 porque end é inclusivo
    }

    return str;
}
void sanitize_buffer(char *str) // Trim e sanitiza.
{
    if (str == NULL)
        return;

    // Etapa 1: Remover '\n' e '\r'
    char *src = str;
    char *dst = str;

    while (*src)
    {
        if (*src != '\n' && *src != '\r')
        {
            *dst++ = *src;
        }
        src++;
    }
    *dst = '\0';

    // Etapa 2: Trim (remover espaços do início e do fim)
    // Ponteiros para o início e fim da string útil
    char *start = str;
    while (isspace((unsigned char)*start))
        start++;

    char *end = str + strlen(str) - 1;
    while (end >= start && isspace((unsigned char)*end))
        end--;

    *(end + 1) = '\0'; // Finalizar a string

    // Se o início mudou, move o conteúdo para o início do buffer
    if (start != str)
        memmove(str, start, end - start + 2); // +1 para incluir o último caractere, +1 para '\0'
}
bool is_line_empty(const char *line)
{
    for (; *line != '\0'; line++)
    {
        if (!isspace((unsigned char)*line))
        {
            return false; // achou caractere que não é espaço, linha não é vazia
        }
    }
    return true; // só espaços e/ou fim da string
}
void remove_white_space(char *buffer)
{
    char *aux = buffer;
    while (*aux != '\0')
    {
        if (*aux == '\n')
        {
            *aux = ' ';
        }

        aux++;
    }
}
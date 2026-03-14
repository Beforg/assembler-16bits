package com.unipampa.compiladorarquitetura16bits.utils.exceptions;

/**
 * Exceção personalizada para erros de compilação
 */
public class CompilationException extends RuntimeException {

    private final ErrorType errorType;

    public enum ErrorType {
        SYNTAX_ERROR,           // Erro de sintaxe
        SEMANTIC_ERROR,         // Erro semântico (variável não declarada, etc)
        VALUE_OUT_OF_RANGE,     // Valor fora do range (0-15)
        UNSUPPORTED_OPERATION,  // Operação não suportada
        MEMORY_LIMIT,           // Limite de memória/registradores
        UNKNOWN                 // Erro desconhecido
    }

    public CompilationException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    public CompilationException(String message) {
        super(message);
        this.errorType = ErrorType.UNKNOWN;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * Cria uma mensagem formatada para exibição
     */
    public String getFormattedMessage() {
        String prefix;
        switch (errorType) {
            case SYNTAX_ERROR:
                prefix = "Erro de Sintaxe";
                break;
            case SEMANTIC_ERROR:
                prefix = "Erro Semântico";
                break;
            case VALUE_OUT_OF_RANGE:
                prefix = "Valor Inválido";
                break;
            case UNSUPPORTED_OPERATION:
                prefix = "Operação não suportada";
                break;
            case MEMORY_LIMIT:
                prefix = "Limite de recursos";
                break;
            default:
                prefix = "Erro";
                break;
        }
        return prefix + ": " + getMessage();
    }
}


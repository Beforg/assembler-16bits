package com.unipampa.compiladorarquitetura16bits.utils;

import com.unipampa.compiladorarquitetura16bits.model.CompiladorSintaxe;
import com.unipampa.compiladorarquitetura16bits.model.Opcode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CondicionalUtils {

    private static final Pattern CONDITION =
            Pattern.compile("se\\s*\\(\\s*(.*?)\\s*(==|!=)\\s*(.*?)\\s*\\)\\s*\\{?");
    private static final Pattern FOR_CONDITION =
            Pattern.compile("para\\s*\\(\\s*(.*?)\\s*(==|!=)\\s*(.*?)\\s*\\)\\s*\\{?");

    private CondicionalUtils() {}

    public static void verificarEstruturaDaCondicaoPara(String linha) {
        Matcher matcher = FOR_CONDITION.matcher(linha);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Estrutura da condicional 'para' inválida: " + linha);
        }
    }

    public static void verificarEstruturaDaCondicao(String linha) {
        Matcher matcher = CONDITION.matcher(linha);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Estrutura da condicional inválida: " + linha);
        }
    }

    public static String extrairOperandoEsquerdo(String linha) {
        Matcher matcher = CONDITION.matcher(linha);
        if (matcher.matches()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    public static String extrairOperandoDireito(String linha) {
        Matcher matcher = CONDITION.matcher(linha);
        if (matcher.matches()) {
            return matcher.group(3).trim();
        }
        return null;
    }

    public static String extrairOperadorCondicional(String linha) {
        Matcher matcher = CONDITION.matcher(linha);
        if (matcher.matches()) {
            String op = matcher.group(2).trim();
            if ("==".equals(op)) {
                return Opcode.BEQ.getCode();
            } else if ("!=".equals(op)) {
                return Opcode.BNE.getCode();
            }
        }
        return null;
    }
}

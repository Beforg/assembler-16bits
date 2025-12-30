package com.unipampa.compiladorarquitetura16bits.utils;

public final class IntegerUtils {
    public static boolean verificaSeEhInteiro(String elemento) {
        if (elemento == null) return false;
        elemento = elemento.trim();
        if (elemento.isEmpty()) return false;
        try {
            Integer.parseInt(elemento);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

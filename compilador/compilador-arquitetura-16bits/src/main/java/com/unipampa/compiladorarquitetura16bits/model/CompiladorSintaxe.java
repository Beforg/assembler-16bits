package com.unipampa.compiladorarquitetura16bits.model;

public enum CompiladorSintaxe {
    
    INTEIRO("inteiro"),
    ATRIBUICAO("="),
    SOMA("+"),
    SUBTRACAO("-"),
    MULTIPLICACAO("*");
    
    private String sintaxeEmString;
    
    CompiladorSintaxe(String sintaxeEmString) {
        this.sintaxeEmString = sintaxeEmString;
    }
    
    public String getSintaxe() {
        return sintaxeEmString;
    }
    
}

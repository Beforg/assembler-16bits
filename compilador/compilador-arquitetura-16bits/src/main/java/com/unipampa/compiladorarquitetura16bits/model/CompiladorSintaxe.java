package com.unipampa.compiladorarquitetura16bits.model;

public enum CompiladorSintaxe {
    
    INTEIRO("inteiro"),
    ATRIBUICAO("="),
    SOMA("+"),
    SUBTRACAO("-"),
    MULTIPLICACAO("*"),
    SE("se"),
    SENAO("senao"),
    ENQUANTO("enquanto"),
    FIM("fim"),
    PARA("para"),
    ATE("ate"),
    IGUALDADE("=="),
    DESIGUALDADE("!=");
    
    private String sintaxeEmString;
    
    CompiladorSintaxe(String sintaxeEmString) {
        this.sintaxeEmString = sintaxeEmString;
    }
    
    public String getSintaxeEmString() {
        return sintaxeEmString;
    }
    
}

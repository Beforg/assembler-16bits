package com.unipampa.compiladorarquitetura16bits.model;

public enum Opcode {
    LDA("LDA,"),
    STA("STA,"),
    SUM("SUM,"),
    SUB("SUB,"),
    JMP("JMP,"),
    BEQ("BEQ,"),
    BNE("BNE,"),
    MUL("MUL,");

    public final String code;

    Opcode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

package com.unipampa.compiladorarquitetura16bits.model;

public class Variavel {
    private String nome;
    private Registrador registrador;

    public Variavel(String nome, Registrador registrador) {
        this.nome = nome;
        this.registrador = registrador;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Registrador getRegistrador() {
        return registrador;
    }

    public void setRegistrador(Registrador registrador) {
        this.registrador = registrador;
    }
}

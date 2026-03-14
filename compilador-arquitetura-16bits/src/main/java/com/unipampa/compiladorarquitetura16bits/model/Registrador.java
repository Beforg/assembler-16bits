package com.unipampa.compiladorarquitetura16bits.model;

public class Registrador {
    private String nome;
    private int valor;
    private int indice = 0;
    private final int maxRegs = 8;

    public Registrador(String nome, int valor) {
        this.nome = nome;
        this.valor = valor;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }


    public int getValor() {
        return valor;
    }

    public void setValor(int valor) {
        this.valor = valor;
    }
}

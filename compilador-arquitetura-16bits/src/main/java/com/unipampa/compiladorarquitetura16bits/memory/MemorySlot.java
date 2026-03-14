package com.unipampa.compiladorarquitetura16bits.memory;

/**
 * Representa um slot de memória
 * Endereços: 0-15
 */
public class MemorySlot {
    private final int address;
    private int value;
    private final String varName;

    public MemorySlot(int address, int value, String varName) {
        if (address < 0 || address > 15) {
            throw new IllegalArgumentException("Endereço de memória inválido: " + address);
        }
        if (value < 0 || value > 15) {
            throw new IllegalArgumentException("Valor inválido para arquitetura 16-bits: " + value);
        }

        this.address = address;
        this.value = value;
        this.varName = varName;
    }

    public int getAddress() {
        return address;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        if (value < 0 || value > 15) {
            throw new IllegalArgumentException("Valor inválido para arquitetura 16-bits: " + value);
        }
        this.value = value;
    }

    public String getVarName() {
        return varName;
    }

    @Override
    public String toString() {
        return String.format("MemorySlot[addr=%d, value=%d, var=%s]", address, value, varName);
    }
}


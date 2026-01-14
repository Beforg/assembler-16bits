package com.unipampa.compiladorarquitetura16bits.memory;

import com.unipampa.compiladorarquitetura16bits.model.Registrador;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resultado de uma alocação de registrador
 * Contém o registrador alocado + instruções geradas (spill/load)
 */
public class AllocationResult {
    private final Registrador registrador;
    private final List<String> instructions;

    public AllocationResult(Registrador registrador, List<String> instructions) {
        this.registrador = registrador;
        this.instructions = new ArrayList<>(instructions);
    }

    /**
     * Construtor para caso sem instruções extras
     */
    public AllocationResult(Registrador registrador) {
        this(registrador, Collections.emptyList());
    }

    public Registrador getRegistrador() {
        return registrador;
    }

    public List<String> getInstructions() {
        return new ArrayList<>(instructions);
    }

    public boolean hasInstructions() {
        return !instructions.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("AllocationResult[reg=%s, instructions=%d]",
            registrador.getNome(), instructions.size());
    }
}


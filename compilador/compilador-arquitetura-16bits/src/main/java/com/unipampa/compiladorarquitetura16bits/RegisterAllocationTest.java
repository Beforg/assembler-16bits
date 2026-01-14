package com.unipampa.compiladorarquitetura16bits;

import com.unipampa.compiladorarquitetura16bits.memory.AllocationResult;
import com.unipampa.compiladorarquitetura16bits.memory.MemoryManager;
import com.unipampa.compiladorarquitetura16bits.memory.VariableLocationTracker;
import com.unipampa.compiladorarquitetura16bits.model.Registrador;
import com.unipampa.compiladorarquitetura16bits.model.Variavel;
import com.unipampa.compiladorarquitetura16bits.utils.RegisterAllocator;

import java.util.HashMap;
import java.util.Map;

/**
 * Teste de integração da Funcionalidade 1: LRU Register Allocation com Spill/Load
 *
 * Demonstra:
 * - Alocação de 8 registradores
 * - Spill automático quando exceder 8
 * - Reload da memória quando variável é usada novamente
 * - LRU na memória também
 */
public class RegisterAllocationTest {

    public static void main(String[] args) {
        System.out.println("=== TESTE DE ALOCAÇÃO LRU COM SPILL/LOAD ===\n");

        // Criar componentes
        Map<Variavel, Registrador> symbolTable = new HashMap<>();
        MemoryManager memoryManager = new MemoryManager();
        VariableLocationTracker locationTracker = new VariableLocationTracker();
        RegisterAllocator allocator = new RegisterAllocator(symbolTable, memoryManager, locationTracker);

        // Teste 1: Alocar 8 variáveis (preenchercomplete todos os registradores)
        System.out.println("--- Teste 1: Alocar 8 variáveis (R0-R7) ---");
        for (int i = 0; i < 8; i++) {
            String varName = "var" + i;
            AllocationResult result = allocator.allocate(varName, i);
            System.out.printf("Alocado: %s → %s (valor=%d)\n",
                varName, result.getRegistrador().getNome(), i);
        }
        System.out.println(allocator.getStatistics());
        System.out.println();

        // Teste 2: Alocar 9ª variável → SPILL (LRU)
        System.out.println("--- Teste 2: Alocar 9ª variável (SPILL) ---");
        AllocationResult result9 = allocator.allocate("var8", 8);
        System.out.printf("Alocado: var8 → %s (valor=8)\n", result9.getRegistrador().getNome());
        System.out.println(allocator.getStatistics());
        System.out.println();

        // Teste 3: Usar variável que sofreu spill → RELOAD
        System.out.println("--- Teste 3: Usar variável que sofreu spill (RELOAD) ---");
        AllocationResult resultReload = allocator.allocate("var0", 0); // var0 foi para memória
        System.out.println("Instruções geradas:");
        resultReload.getInstructions().forEach(System.out::println);
        System.out.printf("Recarregado: var0 → %s\n", resultReload.getRegistrador().getNome());
        System.out.println(allocator.getStatistics());
        System.out.println();

        // Teste 4: Alocar mais variáveis → múltiplos spills
        System.out.println("--- Teste 4: Alocar mais variáveis (múltiplos spills) ---");
        for (int i = 9; i < 12; i++) {
            String varName = "var" + i;
            AllocationResult result = allocator.allocate(varName, i);
            System.out.printf("Alocado: %s → %s (valor=%d)\n",
                varName, result.getRegistrador().getNome(), i);
        }
        System.out.println();

        // Estatísticas finais
        System.out.println("--- Estatísticas Finais ---");
        System.out.println(allocator.getStatistics());
        System.out.println("\nInstruções de spill geradas:");
        allocator.getSpillInstructions().forEach(System.out::println);

        System.out.println("\n=== TESTE CONCLUÍDO ===");
    }
}


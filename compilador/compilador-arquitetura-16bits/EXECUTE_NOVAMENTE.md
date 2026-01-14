# ✅ CORREÇÃO APLICADA - Quebra de Linha no STA

## 🔧 Código Corrigido

### RegisterAllocator.java - Linha 222
```java
// ✅ CORRETO AGORA
String spillInstruction = String.format("STA,%s,%d\n", lruReg.getNome(), memAddr);
```

## ⚠️ IMPORTANTE: Execute novamente!

A correção foi aplicada, mas você precisa **EXECUTAR NOVAMENTE** o compilador para ver o efeito.

### Passos para testar:

1. **Pare a aplicação** se estiver rodando
2. **Execute novamente** (mvn javafx:run ou pelo IDE)
3. **Compile o código** de teste
4. Verifique a saída

## 📊 Resultado Esperado

### ANTES (errado):
```asm
STA,R0,0LDA,R0,0
```

### DEPOIS (correto):
```asm
STA,R0,0
LDA,R0,0
```

## 🔍 Como Verificar

Cole este código e compile:

```java
inteiro a = 1;
inteiro x = 2;
inteiro b = 2;
inteiro c = 1;
inteiro d = 3;
inteiro e = 4;

x = c + d;
b = c + e;
c = x + e;
d = x + d;
e = c + e;

inteiro valor;
se (x == 3) {
    valor = x + d;
} senao {
    valor = x + e;
}

inteiro ultimoReg;
ultimoReg = valor + e;

inteiro subt;
```

**Procure na saída por:**
```asm
STA,R0,0
LDA,R0,0
```

Se ainda aparecer `STA,R0,0LDA,R0,0` (tudo junto), significa que a JVM está usando a versão antiga do .class.

## 🚀 Solução se ainda não funcionar

Execute este comando para forçar rebuild:

```bash
cd /home/bruno-forgiarini/Documentos/estudos/2o\ Semestre/pd-i/gh/compilador/compilador-arquitetura-16bits
mvn clean package
mvn javafx:run
```

Ou no IDE:
1. **Build > Rebuild Project**
2. **Run** novamente

## ✅ Confirmação

O código-fonte está 100% correto:

**Linha 222:**
```java
String spillInstruction = String.format("STA,%s,%d\n", lruReg.getNome(), memAddr);
                                                    ^^^^ 
                                                   AQUI!
```

**Linha 272:**
```java
String spillInstruction = String.format("STA,%s,%d\n", lruReg.getNome(), memAddr);
                                                    ^^^^
                                                   AQUI!
```

Ambos os métodos têm o `\n` corretamente!

---

**Execute novamente e confirme se funcionou!** 🎯


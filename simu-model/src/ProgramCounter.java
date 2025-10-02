/*
*================================================================================================
* ProgramCounter.java
* Autor: Ely Torres Neto
* Propósito: Modelar o ProgramCounter de uma arquitetura de computador em alto nível.
*================================================================================================
* */

// Interface da Classe

 interface IProgramCounter{
     int getCurrentPos();                 // get: retorna a posição que o PC se encontra
    int getLimit();                      // get: retorna o limite teórico informado no construtor. O real será sempre limit -1, assim como um vetor
    int getRealLimit();                  // get: retorna o limite real do programCounter.
    void setCurrentPos(int newPos);      // set: Troca o valor da posição corrente. É comparável a um JMP de assembly.     // method: capaz de avançar uma posição na memória.
    void nextPos();                      // method: Soma mais um
    void jumpTo(int address);            // method: Pula para um endereço válido.
};

// Declaração da classe ProgramCounter
public class ProgramCounter implements IProgramCounter {
    private int currentPosition;    // Atributo: posição atual do
    private final int limit;        // Atributo: para o limite.

    ProgramCounter(int _limit){
        if(_limit <= 0){
            throw new Error("[ProgramCounter()-constructor]: O limitador não pode ser <= a zero!");
        }

        // Seta a posição atual para zero, no inicio
        this.currentPosition = 0;
        // O limite é o valor de endereço máximo que ele pode percorrer;
        // O valor máximo é de [0,limit - 1], assim como o tamanho de um vetor;
        this.limit = _limit;
    }

    // Gets
    public int getCurrentPos() {
        return this.currentPosition;
    }
    public int getLimit()       {
        return this.limit;
    }
    public int getRealLimit()   {
        return this.limit - 1;
    }

    // Sets
    public void setCurrentPos(int newPos){
        validAddress(newPos);
        this.currentPosition = newPos;
    }

    // Methods
    public void nextPos(){
        validAddress(this.currentPosition + 1);
        this.currentPosition++;
    }
    public void jumpTo(int address){
        validAddress(address);
        this.setCurrentPos(address);
    }

    private Boolean validAddress(int addr){
        if(addr < 0 || addr> this.limit){
            throw new Error("[PC-validAdress-err]: O endereço '" + addr + "' não é válido!");
        }
        return true;
    }
}

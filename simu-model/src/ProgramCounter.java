/*
*================================================================================================
* ProgramCounter.java
* Autor: Ely Torres Neto
* Propósito: Modelar o ProgramCounter de uma arquitetura de computador em alto nível.
*================================================================================================
* */


// Interface da Classe
interface IProgramCounter{
    public int getCurrentPos();         // get: retorna a posição que o PC se encontra
    public int getLimit();              // get: retorna o limite teórico informado no construtor. O real será sempre limit -1, assim como um vetor
    public int getRealLimit();          // get: retorna o limite real do programCounter.
    public void setLimit();             // set: Seta um novo limite de forma incondicional.
    public void setCurrentPos();        // set: Troca o valor da posição corrente. É comparável a um JMP de assembly.     // method: capaz de avançar uma posição na memória.
    public void nextPos();              // method: Soma mais um
    public void jumpTo(int address);    // method: Pula para um endereço válido.

};

// Declaração da classe ProgramCounter
public class ProgramCounter implements IProgramCounter {
    private int currentPosition;    // Atributo: posição atual do
    private int limit;              // Atributo: para o limite.



    ProgramCounter(int _limit){
        if(_limit <= 0){
            throw new Error("[ProgramCounter()-constructor]: O limitador não pode ser <= a zero!");
        }

        // Seta a posição atual para zero, no inicio
        this.currentPosition = 0;
        // O limite é o valor de endereço máximo que ele pode percorrer;
        // O valor máximo é de [0,limit - 1], assim como o tamanho de um vetor;

        // A
        this.limit = _limit;
    }

    // Gets
    public int getCurrentPos(){
        return this.currentPosition;
    }
    public int getLimit(){
        return this.limit;
    }

    public int getRealLimit(){
        return this.limit - 1;
    }


    // Sets
    public void setCurrentPos(){}
    public void setLimit(){};


    // Methods
    public void nextPos();
    public void jumpTo(int address);



}

public class ProgramCounter {
    private String label; // Nome para chamarmos o PC, se necessário usar mais de um.
    private int currentAddr; // O endereço atual que o PC está na memória.
    private int limitAddr; // O endereço limite, o qual ele não pode ser igual.

    // Definição do construtor padrão, recebendo todos os atributos da classe.
    // Por enquanto, não vamos fazer nenhum tratamento de erro.

    ProgramCounter(String _label, int _currentAddr, int _limitAddr) {
        this.label = _label;
        this.currentAddr = _currentAddr;
        this.limitAddr = _limitAddr;
    }

    // Gets para cada atributo.
    public String getLabel() {
        return this.label;
    }

    public int getCurrentAddr() {
        return this.currentAddr;
    }

    public int getLimitAddr() {
        return this.limitAddr;
    }

    // Sets para cada atributo
    public void setLabel(String _newLabel) {

        if (_newLabel.isEmpty()) {
            throw new Error("ProgramCounter:setLabel(): A label que você quer setar está vazia!");
        }
        this.label = _newLabel;
    }

    public void setCurrentAddr(int currentAddr) {

        if (currentAddr >= limitAddr) {
            throw new Error("ProgramCounter:setCurrentAddr(): O endereço normal não pode ser maior que o limite!");
        }
        currentAddr = Math.abs(currentAddr); // Força apenas números positivos!
        this.currentAddr = currentAddr;

    }

    public void setLimitAddr(int limitAddr) {
        limitAddr = Math.abs(limitAddr);
        this.limitAddr = limitAddr;
    }

    /* Métodos para uso da classe */

    public void nextInstruction() {         // Vai para a próxima instrução de memória.

        if (currentAddr + 1 < limitAddr) { // Verifica se o próximo endereço está dentro do limite declarado.
            this.currentAddr++;
            return;
        }

        System.out.println("ProgramCounter:NextInstruction(): A memória chegou ao limite.");

    }

    public void resetCurrentAddr() { // Volta para 0, o endereço atual.
        this.currentAddr = 0;
    }
}

//-----INTERFACE-----

//-----CLASSES-----

class ProgramCounter {
  private _currentAddress: number;
  private _beginAddr: number;
  private _endAddr: number;

  constructor(__beginAddr: number, __endAddr: number) {
    console.debug(
      "ProgramCounter:constructor(): Criação do objeto ProgramCounter!"
    );
    this._beginAddr = __beginAddr;
    this._endAddr = __endAddr;
    this._currentAddress = 0;
  }

  private verifyOutOfBounds(_currentAddress: number): void {
    // console.debug(
    //   "ProgramCounter:outOfBounts(): Entrada na função outOfBounds()!"
    // );

    if (_currentAddress < this._beginAddr || _currentAddress > this._endAddr) {
      throw new Error(
        `ProgramCounter-err:outOfBounds()-> O limite para o ProgramCounter é de [${this._beginAddr},${this._endAddr}].\nVocê adicionou um valor fora do limite.`
      );
    }
  }

  public get currentAddress(): number {
    return this._currentAddress;
  }

  public set currentAddress(new_addr: number) {
    if (!Number.isInteger(new_addr)) {
      throw new Error(
        "ProgramCounter: currentAddress():  O new_addr informado deve ser um inteiro."
      );
    }
    this.verifyOutOfBounds(new_addr);
    this._currentAddress = new_addr;
  }

  public get beginAddr(): number {
    return this._beginAddr;
  }

  public set beginAddr(limit: number) {
    if (limit < 0) {
      throw new Error(
        "ProgramCounter:beginAddr(): O endereço inicial de memória não pode ser x < 0."
      );
    }

    this._beginAddr = limit;
  }

  public get endAddr(): number {
    return this._endAddr;
  }

  public set endAddr(limit: number) {
    if (limit <= this.beginAddr) {
      throw new Error(
        "ProgramCounter:beginAddr(): O endereço final de memória não pode ser igual <= ao inicial."
      );
    }
    this._endAddr = limit;
  }

  public nextInstruction(): void {
    if (
      this.currentAddress >= this.beginAddr ||
      this.currentAddress < this.endAddr
    ) {
      this.currentAddress++;
      return;
    }

    throw new Error(
      "ProgramCounter-err: nextInstruction(): Você chegou ao limite da memória de programa!"
    );
  }

  public jumpToAddress(newAddr: number): void {
    Validation.isInteger(newAddr);
    newAddr = Math.abs(newAddr);

    if (newAddr >= this.beginAddr || newAddr < this.endAddr) {
      this.currentAddress = newAddr;
      return;
    }
  }
}

//-----EXPORT-----
export { ProgramCounter };

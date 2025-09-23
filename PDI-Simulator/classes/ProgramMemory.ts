import { ProgramCounter } from "./ProgramCounter";

class ProgramMemory {
  public _pc: ProgramCounter;
  public _addresses: number[];
  private _mem_size: number;
  constructor(__mem_size:number){
    // Instanciar PC
    this._pc = new ProgramCounter(0,16);
  }
}

import { LockState } from "./IRegister";

class MemoryElement {
  public value: number;
  public lockState: LockState;
  constructor() {
    this.value = 0;
    this.lockState = LockState.UNLOCK;
  }
}

export {MemoryElement};


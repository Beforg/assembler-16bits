class Validation {
  public static isInteger(x: number) {
    if (!Number.isInteger(x)) {
      throw new Error(
        "ProgramCounter: currentAddress():  O new_addr informado deve ser um inteiro."
      );
    }
  }
}

/*-----INTERFACES-----*/
const LOCK = 1;
const UNLOCK = 0;

/*-----CLASSE-----*/
class Register {
  private _id: number;
  private _data: number;
  private _lock: number;

  constructor(__id: number, __data: number, __lock: number) {
    this._id = __id;
    this._data = __data;
    this._lock = __lock;
  }

  get id(): number {
    console.info(`simu-info: register.id(): ${this._id}`);
    return this._id;
  }

  get data(): number {
    console.info(`simu-info: register.data(): ${this._data}`);
    return this._data;
  }

  set id(_id: number) {
    this._id = _id;
  }

  get lockState() {
    console.info(`simu-info: register.lockState(): ${this._lock}`);
    return this._lock;
  }

  set lockState(state: number) {
    this._lock = state;
    console.info(`simu-info: register.lockState(): ${this._lock}`);
  }

  set data(_data: number){
    if (_data < 0 || _data > 15) {
      throw new Error(
        `simu-err: register.data():[reg ${this._id}] O registrador apenas consegue suportar valores entre 0 e 15.`
      );
    }

    if (this._lock == LOCK) {
      console.warn(
        `simu-err: register.data():[reg ${this._id}] Você não pode modificar um registrador que está travado.`
      );
      return;
    }
    this._data = _data;
  }
}

/*-----EXPORT-----*/

export { Register };
export { LOCK, UNLOCK };

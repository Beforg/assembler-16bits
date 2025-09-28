void main(){
    ProgramCounter pc = new ProgramCounter("pc",0,16);
    for(int i = 0; i < 15;i++){
        pc.nextInstruction();
    }

}
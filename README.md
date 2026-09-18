# Logism Compiler
hammer compiler is a personal project designed to handle compiling the human-legible instructions from a fully customizable ISA into readable machine code for my personally designed amateur computer architectures based in the `logism evolution` software

# How it works

## secondary files
three files are required for the proper execution of the logism Compiler:
- "MI.hss": holding the microinstructions that are present in each instruction of the ISA
- "STA.hss": holding the known instructions in the current ISA
- "code.hss": file holding the actual code used in the program

### Instructions
the instructions created

### Microinstructions
the microinstruction is the mnemonic of a signal that is sent out of the control logic after the decoding phase, an example is the "CO" micro instruction, that sends a signal in the clock rising edge to the program counter, making it output its current value.

## future of logism Compiler
updates are planned for the future including
### simple variables -> values will represent a binary value (ex: a = 0010)
i plan to use them mostly to store address, since during the code compiling they will be simply replaced by their value
#### before compiling: posA = 0010. jump: posA. 
#### after compiling: jump: 0010.
### creation of a proper executable app
### result being displayed as an text file instead of the console (current priority)
### HSS basic documentation(first finish language)
### better HSS code debugging and a proper UI

# IMPORTANT NOTE:
the creation and the main development of both my logism based computer architecture and logism compiler predates my formal education and were made purely for entertainment.

# HammerCompiler
hammer compiler is a personal project design to handle compiling the human language hammer assembly(HSS) into readable machine code for logism evolution, originally made for the Hammer V1, a personal 4 bit computer idealized by me
## secondary files
three files are required for the proper execution of HammerCompiler: a "MI.hss" file holding the microinstructions, a "STA.hss" file holding the statements and a "code.hss" file holding the actual code.
## hammer assembly(HSS)
all HSS commands used in the final code are completely customizable to ensure that the code can be used in many different small computers
### Hammer Statements
a HSS statement is a collection of steps to be executed when the statement is called, each step is a binary value that chooses to send a on(1) or a off(0) signal according to the user input
### Hammer Microinstructions
a HSS microinstruction is the name of signal that is sent out of the control logic, an example is the "CO" micro instruction, when its 1, the program counter value is sent into BUS. In the "MI.hss" acts like a enum where the MI code is the number of the line
## future of HammerCompiler
updates are planned for the future including
### simple variables -> values will represent a binary value (ex: a = 0010)
i plan to use them mostly to store address, since during the code compiling they will be simply replaced by their value
#### before compiling: posA = 0010. jump: posA. 
#### after compiling: jump: 0010.
### creation of a proper executable app
### result being displayed as an text file instead of the console (current priority)
### HSS basic documentation(first finish language)
### better HSS code debugging and a proper UI
# CURRENT VERSION: version 0.1

# LVM
Lua virtualsation toolset in Java

## Parts
- [x] bytecode parsing libary (5.x)
- [x] instruction sets (DONE: 5.1)
- [ ] emulation of execution (IN PROGRESS)
- [ ] access of frames and interception

## Goals
To be used to analyse and other things on compiled lua scripts

## Usages

### File parser
The file parser works for all luac compiled files from version 5.0 -> 5.3   
Here is some simple code to read a compiled file:
```java
...
byte[] data = ...;
LuaDataStream stream = new LuaDataStream(data);
LuaFileReader reader = new LuaFileReader(stream);

LuaFile file = reader.read();
```

### Disassembly and intstruction operations
Disassembly gives direct insight into the programs behaviour and operations
```java
...
LuaFunction main = file.getFunction();
Disassembler disasm = new Disassembler(main, System.out);
disasm.disassemble(); // print disassembly to main

// assuming that some function calls get set up in this sample
ClosureInstruction inst = main.getInstructions().get(0); 
// get the function referenced by this instruction
LuaFunction refFunction = inst.getProto();
...
```

### Virtual machine & code exection
The Virtual machine allows for virtualizing and executing compiled bytecode allowing for full static analysis of lua binaries
```java
// prepare a vm
VM vm = new VM();
vm.initialize();
// execute the main function
ExecutionContext result = vm.getHelper().invoke(main, vm.getGlobal());
Value val = results.get(0); // get R0

System.out.println(val.asString()); // print R0
```

#### Function overwriting
In the vm it is possible to overwrite functions to give control what these do, this includes library functions
```java
// get the vm libraries
Libraries libs = vm.getLibraries();
Library base = libs.getTop(); // get the top library (the base library)
// choose a method (in this case 'print')
// then provide a new method to execute
base.set("print", (ctx) -> {
    System.out.println("Custom print call!");	
});

// or during/before execution access the global table and overwrite functions directly
Table global = vm.getGlobal();
// first define a new Closure
Closure customClosure = new Closure((ctx) -> {   
	System.out.println("Intercepted call!");
});
// wrap in ClosureValue
ClosureValue value = new ClosureValue(customClosure);        
// set new closure
global.set("customFunction", value);
```

#### Instruction overwriting
It is also possible to overwrite the behaviour of specific instructions entirely
```java
vm.getInterpreter().install(Opcodes.MOVE, (Executor<LoadInstruction>) (insn, ctx) -> {
	System.out.println("Custom instructions");
});
```



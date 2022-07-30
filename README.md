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
```java
// prepare a vm
VM vm = new VM();
vm.initialize();
Table global = ... // prepare global environment
// execute the main function
ExecutionContext result = vm.getHelper().invoke(main, global);
Value val = results.get(0); // get R0

System.out.println(val.asString()); // print R0
```




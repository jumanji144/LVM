# LVM
Lua virtualsation toolset in Java

## Parts
- [x] bytecode parsing libary (5.x)
- [x] instruction sets (DONE: 5.1)
- [ ] emulation of execution
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


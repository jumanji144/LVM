import me.darknet.lua.file.LuaDataStream;
import me.darknet.lua.file.LuaFile;
import me.darknet.lua.file.LuaFileReader;
import me.darknet.lua.file.util.Disassembler;
import me.darknet.lua.vm.VM;
import me.darknet.lua.vm.VMException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TestRunner {

	@BeforeAll
	public static void setup() {
		if(!Luac.initalize()) {
			System.out.println("Failed to initialize Luac");
			System.exit(1);
		}
	}

	@ParameterizedTest
	@MethodSource("paths")
	public void testExample(Path path) throws Exception {
		String name = path.getFileName().toString();
		byte[] bytes = Files.readAllBytes(path);
		String source = new String(bytes);
		byte[] compiled = Luac.compile(source);
		LuaFileReader reader = new LuaFileReader(new LuaDataStream(new ByteArrayInputStream(compiled)));
		LuaFile luaFile = reader.read();
		Disassembler disassembler = new Disassembler(luaFile.getFunction(), System.out);
		System.out.println("Testing: " + name);
		try {
			VM vm = new VM();
			vm.initialize();
			vm.getHelper().invoke(luaFile.getFunction(), vm.getGlobal());
		} catch (VMException e) {
			disassembler.disassemble();
			throw new RuntimeException(e.constructStackTrace(), e);
		} catch (Exception e) {
			disassembler.disassemble();
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static List<Path> paths() throws IOException {
		return Files.walk(Paths.get("src/test/resources/"))
				.filter(p -> p.toString().endsWith(".lua"))
				.collect(Collectors.toList());
	}

}

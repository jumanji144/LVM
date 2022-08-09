import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@UtilityClass
public class Luac {

	public boolean initalize() {
		// try to find luac in the system path
		String luac = "luac.exe";
		try {
			Process process = Runtime.getRuntime().exec(luac);
			process.waitFor();
			return true;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	public byte[] compile(String source) {
		try {
			Process process = Runtime.getRuntime().exec(new String[]{"luac.exe", "-"});
			process.getOutputStream().write(source.getBytes());
			process.getOutputStream().close();
			process.waitFor();
			// it compiles to a file called 'luac.out'
			return Files.readAllBytes(Paths.get("luac.out"));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return new byte[0];
	}

}

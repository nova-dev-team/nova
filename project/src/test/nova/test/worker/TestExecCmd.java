package nova.test.worker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

public class TestExecCmd {
	@Test
	public void test() {
		Process p;
		String cmd = "cd run";

		try {
			p = Runtime.getRuntime().exec(cmd);
			InputStream fis = p.getInputStream();
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
package nova.test.worker;

import java.io.IOException;

import nova.worker.models.StreamGobbler;

import org.apache.log4j.Logger;
import org.junit.Test;

public class TestExecCmd {
	@Test
	public void test() {
		Logger log = Logger.getLogger(TestExecCmd.class);

		Process p;
		String cmd = "ifconfig";
		System.out.println(cmd);
		try {
			p = Runtime.getRuntime().exec(cmd);
			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),
					"ERROR");
			errorGobbler.start();
			StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(),
					"STDOUT");
			outGobbler.start();
			try {
				if (p.waitFor() != 0) {
					log.error("pack iso returned abnormal value!");
				}
			} catch (InterruptedException e1) {
				log.error("pack iso process terminated", e1);
			}
		} catch (IOException e1) {
			log.error("exec mkisofs cmd error!", e1);
		}
	}
}
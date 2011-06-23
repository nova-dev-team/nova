package nova.test.worker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import nova.common.util.Utils;
import nova.worker.models.StreamGobbler;

import org.junit.Test;

public class TestPackISO {

	@Test
	public void test() {

		String cmd = "mkisofs -J -T -R -V cdrom -o "
				+ Utils.pathJoin(Utils.NOVA_HOME, "run", "agentcd",
						"agent-cd.iso") + " "
				+ Utils.pathJoin(Utils.NOVA_HOME, "run", "softwares");
		// String cmd = "ifconfig";
		System.out.println(cmd);

		try {
			Process p = Runtime.getRuntime().exec(cmd);
			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),
					"ERROR");
			errorGobbler.start();
			StreamGobbler outGobbler = new StreamGobbler(p.getErrorStream(),
					"STDOUT");
			outGobbler.start();
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("here");

			InputStream fis = p.getInputStream();
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			while (br.ready()) {
				System.out.println(br.readLine());
			}

			// String line = null;
			// while ((line = br.readLine()) != null) {
			// System.out.println(line);
			// }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

package nova.test.unit.common;

import java.io.File;

import junit.framework.Assert;
import nova.common.util.Utils;

import org.junit.Test;

public class TestUtils {

	private void testPathSplitHelper(String path, String split0, String split1) {
		String[] splt = Utils.pathSplit(path);
		System.out.println(path + " -> " + splt[0] + ", " + splt[1]
				+ "; expect: " + split0 + ", " + split1);
		Assert.assertEquals(splt[0], split0);
		Assert.assertEquals(splt[1], split1);
	}

	@Test
	public void testPathSplit() {
		testPathSplitHelper(Utils.pathJoin("a", "b"), "a", "b");
		testPathSplitHelper("a", null, "a");
		testPathSplitHelper("a" + File.separator, null, "a");
		testPathSplitHelper(File.separator + "a", File.separator, "a");
		testPathSplitHelper(Utils.pathJoin("a", "b", "c"), "a" + File.separator
				+ "b", "c");
		testPathSplitHelper(File.separator + Utils.pathJoin("a", "b", "c"),
				File.separator + "a" + File.separator + "b", "c");
	}

	private void testPathJoinHelper(String expect, String... paths) {
		System.out.print("expected: " + expect);
		String actual = Utils.pathJoin(paths);
		System.out.print("; actual: " + actual);
		System.out.print("; from: ");
		for (String path : paths) {
			System.out.print(path + ", ");
		}
		System.out.println();
		Assert.assertEquals(actual, expect);
	}

	private void testPathJoinThrowsException(String... paths) {
		try {
			Utils.pathJoin(paths);
			Assert.fail("failed to throw exception!");
		} catch (IllegalArgumentException ex) {
			// do nothing
		}
	}

	@Test
	public void testPathJoin() {
		testPathJoinHelper("abc", "abc");
		testPathJoinHelper("abc" + File.separator + "add", "abc", "add");
		testPathJoinHelper(File.separator + "abc", "123", File.separator
				+ "abc");
		testPathJoinHelper(File.separator + "abc", File.separator, "abc");

		testPathJoinHelper("abc" + File.separator + "def", "abc", "", "", "",
				"def");

		testPathJoinHelper("abc" + File.separator + "ghi", "abc", "", "", "",
				"def", "..", "", "", "ghi");

		testPathJoinHelper("abc" + File.separator + "ghi", "abc", "", "", "",
				"def", "..", "", "", "ghi", ".", ".", "bst", "..");

		testPathJoinHelper(".", ".");

		testPathJoinHelper("a", ".", "a");

		testPathJoinThrowsException("..");

		testPathJoinThrowsException(".", "a", "b", "..", "c", "..", "..", "..");

	}

	@Test
	public void testRmDir() {
		String testRoot = Utils
				.pathJoin(Utils.NOVA_HOME, "build", "test-rmdir");
		Utils.mkdirs(Utils.pathJoin(testRoot, "1", "2", "3", "4"));

		Utils.rmdir(testRoot);
		File f = new File(testRoot);
		Assert.assertTrue(f.exists() == false);
	}
}

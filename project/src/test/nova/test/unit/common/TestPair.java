package nova.test.unit.common;

import junit.framework.Assert;
import nova.common.util.Pair;

import org.junit.Test;

public class TestPair {

	@Test
	public void testEqualFunction() {
		Pair<Integer, Integer> p1 = new Pair<Integer, Integer>(2, 3);
		Pair<Integer, Integer> p2 = new Pair<Integer, Integer>(2, 3);
		Assert.assertEquals(p1, p1);
		Assert.assertEquals(p2, p2);
		Assert.assertEquals(p1, p2);

		Pair<Integer, Integer> p3 = new Pair<Integer, Integer>(2, 2);
		Assert.assertFalse(p1.equals(p3));

	}

	@Test
	public void testHashCodeFunction() {
		Pair<Integer, Integer> p1 = new Pair<Integer, Integer>(2, 3);
		Pair<Integer, Integer> p2 = new Pair<Integer, Integer>(2, 3);
		Assert.assertEquals(p1.hashCode(), p2.hashCode());
	}
}

package nova.test;

import org.junit.Test;
import junit.framework.Assert;

public class ExampleTest {
	
	@Test
	public void exampleTest() {
		Assert.assertTrue(1 > 0);
		Assert.assertFalse(1 < 0);
		Assert.assertEquals(1, 1);
		Assert.assertNotNull(new Object());
		Assert.assertNull(null);
		Assert.assertNotSame(new String(), new String());
		String str = new String();
		Assert.assertSame(str, str);
	}

}

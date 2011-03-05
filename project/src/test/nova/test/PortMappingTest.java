package nova.test;

import nova.tools.PortMapping;

import org.junit.Test;

public class PortMappingTest {
	
	@Test
	public void testMappingToLocalhost() {
		PortMapping.addMapping(1234, "localhost:4321");
	}
	
	@Test
	public void testMappingToGoogle() {
		PortMapping.addMapping(1234, "www.google.com");
	}
	
	@Test
	public void testMappingControlling() {
		
	}
	
}

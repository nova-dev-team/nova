package nova.client.java;

public class Main {
	
	/**
	 * Just for demo & test use.
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Hello! This is Nova client library, Java version");
		Client cli = new Client("10.0.1.199:3000", "root", "monkey");
		cli.listPmachines();
	}

}

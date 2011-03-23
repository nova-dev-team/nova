package nova.common.service;

public class HandlerNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 7631134559411786683L;

	String klass;

	public HandlerNotFoundException(String klass) {
		super("Handler not found for type: " + klass);
		this.klass = klass;
	}

	public String getClassName() {
		return this.klass;
	}

}

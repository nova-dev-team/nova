package nova.common.service;

/**
 * Exception triggered when handler for a certain request is not found.
 * 
 * @author santa
 * 
 */
public class HandlerNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 7631134559411786683L;

    /**
     * The request object type.
     */
    String klass;

    public HandlerNotFoundException(String klass) {
        super("Handler not found for type: " + klass);
        this.klass = klass;
    }

    public String getClassName() {
        return this.klass;
    }

}

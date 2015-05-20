package framework;

/**
 * Represents an error when loading an OBJ object.
 *
 * @author Robert C. Duvall
 */
public class OBJException extends RuntimeException {
    // for serialization
    private static final long serialVersionUID = 1L;

    /**
     * Create an exception based on an issue in our code.
     */
    public OBJException (String message, Object... values) {
        super(String.format(message, values));
    }

    /**
     * Create an exception based on a caught exception with a different message.
     */
    public OBJException (Throwable cause, String message, Object... values) {
        super(String.format(message, values), cause);
    }

    /**
     * Create an exception based on a caught exception, with no additional message.
     */
    public OBJException (Throwable exception) {
        super(exception);
    }
}

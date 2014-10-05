package exceptions;

/**
 * Created by conor on 04/10/2014.
 *
 * Custom exception to signify generically the case where generating a PDF failed.
 */
public class PdfGenerationException extends Exception {

    /**
     *
     */
    public PdfGenerationException() {
        super();
    }

    /**
     *
     * @param message exception message
     */
    public PdfGenerationException(String message) {
        super(message);
    }

    /**
     *
     * @param message exception message
     * @param cause exception cause
     */
    public PdfGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     *
     * @param cause exception cause
     */
    public PdfGenerationException(Throwable cause) {
        super(cause);
    }
}

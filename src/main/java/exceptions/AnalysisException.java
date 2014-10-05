package exceptions;

/**
 * Created by conor on 04/10/2014.
 *
 * Exception to signify generically when graph analysis has encountered a problem.
 */
public class AnalysisException extends Exception {

    /**
     *
     */
    public AnalysisException() {
        super();
    }

    /**
     *
     * @param message exception message
     */
    public AnalysisException(String message) {
        super(message);
    }

    /**
     *
     * @param message exception message
     * @param cause exception cause
     */
    public AnalysisException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     *
     * @param cause exception cause
     */
    public AnalysisException(Throwable cause) {
        super(cause);
    }
}

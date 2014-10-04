package exceptions;

/**
 * Created by conor on 04/10/2014.
 */
public class AnalysisException extends Exception {

    public AnalysisException() {
        super();
    }

    public AnalysisException(String message) {
        super(message);
    }

    public AnalysisException(String message, Throwable cause) {
        super(message, cause);
    }

    public AnalysisException(Throwable cause) {
        super(cause);
    }
}

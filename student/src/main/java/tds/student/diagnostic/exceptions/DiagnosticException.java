package tds.student.diagnostic.exceptions;

public class DiagnosticException extends Exception {

    public DiagnosticException(String message) {
        super(message);
    }

    public DiagnosticException(String message, Throwable cause) {
        super(message, cause);
    }
}

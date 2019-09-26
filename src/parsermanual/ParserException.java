package parsermanual;

public class ParserException extends RuntimeException {
    public ParserException(String msg, Object... args) {
        super(String.format(msg, args));
    }
}

package zejfseis4.exception;

public class FatalIOException extends FatalApplicationException{

	private static final long serialVersionUID = 1L;

	public FatalIOException(Throwable cause) {
		super(cause);
	}
	
	public FatalIOException(String message, Throwable cause) {
		super(message, cause);
	}

}
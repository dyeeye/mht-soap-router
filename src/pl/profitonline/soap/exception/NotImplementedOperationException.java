package pl.profitonline.soap.exception;

public class NotImplementedOperationException extends SoapException {
	private static final long serialVersionUID = 3513384258708220735L;

	public NotImplementedOperationException(String operation) {
		super("Operation " + operation + " not implemented");
	}

}

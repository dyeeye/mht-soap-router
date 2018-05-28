package pl.profitonline.soap.exception;

public class MissingHeaderException extends SoapException {
	private static final long serialVersionUID = 5959654053117088074L;

	public MissingHeaderException(String header) {
		super("Header " + header + " is missing");
	}
}

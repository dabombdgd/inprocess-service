package gov.va.bsms.cwinr.exceptions;

public class ConnectionManagerException extends Exception {
	private static final long serialVersionUID = -6378262342625609245L;

	public ConnectionManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectionManagerException(String message) {
		super(message);
	}

}

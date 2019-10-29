package gov.va.bsms.cwinr.exceptions;

public class SoapClientException extends Exception {
	private static final long serialVersionUID = -6378262342625609245L;

	public SoapClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public SoapClientException(String message) {
		super(message);
	}

}

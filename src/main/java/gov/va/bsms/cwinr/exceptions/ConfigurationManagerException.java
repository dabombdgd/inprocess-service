package gov.va.bsms.cwinr.exceptions;

public class ConfigurationManagerException extends Exception {
	private static final long serialVersionUID = 1L;

	public ConfigurationManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigurationManagerException(String message) {
		super(message);
	}

}

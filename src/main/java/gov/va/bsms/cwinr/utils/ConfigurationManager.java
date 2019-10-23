package gov.va.bsms.cwinr.utils;

import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Singleton is responsible for reading the eva.properties file.
 * 
 */
public enum ConfigurationManager {
	INSTANCE;

	static Logger LOGGER = LoggerFactory.getLogger(ConfigurationManager.class);
	private ResourceBundle resources;

	public ResourceBundle getResources() {
		return resources;
	}

	ConfigurationManager() {
		if(resources == null) {
			this.resources = ResourceBundle.getBundle("eva");
		}
	}

}

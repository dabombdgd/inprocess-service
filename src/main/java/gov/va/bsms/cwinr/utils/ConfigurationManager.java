package gov.va.bsms.cwinr.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.PropertyResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.bsms.cwinr.exceptions.ConfigurationManagerException;

/**
 * This Singleton is responsible for reading the eva.properties file.
 * 
 */
public enum ConfigurationManager {
	INSTANCE;

	private static Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);
	private PropertyResourceBundle resources;

	public PropertyResourceBundle getResources() throws ConfigurationManagerException {
		if(this.resources == null) {
			try {
				this.resources = new  PropertyResourceBundle(Files.newInputStream(Paths.get("eva.properties")));
			} catch (IOException e) {
				throw new ConfigurationManagerException("COnfiguration file is not accessible.", e);
			}
		}
		
		return this.resources;
	}

	ConfigurationManager() {
	}

}

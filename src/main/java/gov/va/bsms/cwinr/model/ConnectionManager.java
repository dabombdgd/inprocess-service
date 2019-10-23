package gov.va.bsms.cwinr.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.bsms.cwinr.exceptions.ConnectionManagerException;
import gov.va.bsms.cwinr.utils.ConfigurationManager;
import oracle.jdbc.driver.OracleConnection;

public class ConnectionManager {
	private static Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

	public static Connection getConnection() throws ConnectionManagerException {
		Connection returnVal = null;
		
		// set database connection properties
		Properties props = new Properties();
		props.setProperty("user", ConfigurationManager.INSTANCE.getResources().getString("jdbc-user"));
		props.setProperty("password", ConfigurationManager.INSTANCE.getResources().getString("jdbc-password"));
		props.setProperty(OracleConnection.CONNECTION_PROPERTY_THIN_NET_CONNECT_TIMEOUT, "5000");
		
		try {
			returnVal = DriverManager.getConnection(ConfigurationManager.INSTANCE.getResources().getString("jdbc-url"),props);

			if (returnVal != null) {
				LOGGER.debug("Connected to the database!");
			} else {
				LOGGER.debug("Failed to make connection!");
			}

		} catch (SQLException e) {
			LOGGER.error("SQL State: {}, Message: {}", e.getSQLState(), e.getMessage());
			throw new ConnectionManagerException("Database is not accessible.");
		}

		return returnVal;
	}
}

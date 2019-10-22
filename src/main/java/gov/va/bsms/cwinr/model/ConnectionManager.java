package gov.va.bsms.cwinr.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.bsms.cwinr.utils.ConfigurationManager;

public class ConnectionManager {
	private static Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

	public static Connection getConnection() {
		Connection returnVal = null;
		try {
			returnVal = DriverManager.getConnection(ConfigurationManager.INSTANCE.getResources().getString("jdbc-url"),
					ConfigurationManager.INSTANCE.getResources().getString("jdbc-user"),
					ConfigurationManager.INSTANCE.getResources().getString("jdbc-password"));

			if (returnVal != null) {
				LOGGER.debug("Connected to the database!");
			} else {
				LOGGER.debug("Failed to make connection!");
			}

		} catch (SQLException e) {
			LOGGER.error("SQL State: {}\n{}", e.getSQLState(), e.getMessage());
		}

		return returnVal;
	}
}

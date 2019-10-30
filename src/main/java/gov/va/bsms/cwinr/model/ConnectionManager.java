package gov.va.bsms.cwinr.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.bsms.cwinr.exceptions.ConfigurationManagerException;
import gov.va.bsms.cwinr.exceptions.ConnectionManagerException;
import gov.va.bsms.cwinr.utils.ConfigurationManager;
import oracle.jdbc.driver.OracleConnection;

public class ConnectionManager {
	private static Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

	private ConnectionManager() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Method creates the Oracle {@link Connection} object with a set of properties.
	 * 
	 * @return
	 * @throws ConnectionManagerException
	 */
	public static Connection getConnection(Boolean autoCommit) throws ConnectionManagerException, ConfigurationManagerException {
		Connection returnVal = null;

		// set database connection properties
		Properties props = new Properties();
		props.setProperty("user", ConfigurationManager.INSTANCE.getResources().getString("jdbc-user"));
		props.setProperty("password", ConfigurationManager.INSTANCE.getResources().getString("jdbc-password"));
		props.setProperty(OracleConnection.CONNECTION_PROPERTY_THIN_NET_CONNECT_TIMEOUT, "5000");

		try {
			returnVal = DriverManager.getConnection(ConfigurationManager.INSTANCE.getResources().getString("jdbc-url"),
					props);
			returnVal.setAutoCommit(autoCommit);
		} catch (SQLException e) {
			logger.error("SQL State: {}, Message: {}", e.getSQLState(), e.getMessage());
			throw new ConnectionManagerException("Database is not accessible.", e);
		}

		return returnVal;
	}

	/**
	 * Closes the {@link PreparedStatement} and {@link Connection} objects
	 * 
	 * @param prepStmnt
	 * @param conn
	 */
	public static void closeDatabaseObjects(PreparedStatement prepStmnt, Connection conn) {
		if(prepStmnt != null) {
			try {
				prepStmnt.close();
			} catch (SQLException e) {
				logger.warn(e.getMessage());
			}
		}
		if(conn!=null) {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.warn(e.getMessage());
			}
		}
	}
}

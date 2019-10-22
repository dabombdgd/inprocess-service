package gov.va.bsms.cwinr.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionManager {
	private static Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

	private static final String URL = "jdbc:mysql://localhost:3306/testdb";
	public static final String USER = "testuser";
	public static final String PASS = "testpass";

	public static Connection getConnection() {
		Connection returnVal = null;
		try {
			returnVal = DriverManager.getConnection(URL, USER, PASS);

			if (returnVal != null) {
				LOGGER.debug("Connected to the database!");
			} else {
				LOGGER.debug("Failed to make connection!");
			}

		} catch (SQLException e) {
			LOGGER.error("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		}

		return returnVal;
	}
}

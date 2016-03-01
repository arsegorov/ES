package org.reasoningmind.diagnostics;

import javax.swing.*;
import java.io.File;
import java.sql.*;

//import org.apache.commons.csv.CSVFormat;
//import java.nio.charset.StandardCharsets;

/**
 * This class handles operations with the student data.
 */
public class StudentDataManager {
	Diagnostics hostApp;
	Connection connection;

	StudentDataManager(Diagnostics hostApp) {
		this.hostApp = hostApp;
	}

	void readCSV(File csv) {

	}

	void readCLP(File clp) {

	}

	void dbConnectionTest() {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:test.db");

			Statement stat = connection.createStatement();
			stat.executeUpdate("drop table if exists people;");
			stat.executeUpdate("create table people (name, occupation);");

			PreparedStatement prep = connection.prepareStatement(
					"insert into people values (?, ?);");
			prep.setString(1, "Gandhi");
			prep.setString(2, "politics");
			prep.addBatch();
			prep.setString(1, "Turing");
			prep.setString(2, "computers");
			prep.addBatch();
			prep.setString(1, "Wittgenstein");
			prep.setString(2, "smartypants");
			prep.addBatch();

			connection.setAutoCommit(false);
			prep.executeBatch();
			connection.setAutoCommit(true);

			ResultSet rs = stat.executeQuery("select * from people;");
			while (rs.next()) {
				System.out.println("name = " + rs.getString("name"));
				System.out.println("job = " + rs.getString("occupation"));
			}
			rs.close();
			connection.close();
		} catch (ClassNotFoundException classNotFoundException) {
			JOptionPane.showMessageDialog(
					hostApp.getMainWindow(),
					"Class \"org.sqlite.JDBC\" is not found.",
					"Error",
					JOptionPane.ERROR_MESSAGE);

		} catch (Exception e) {
			JOptionPane.showMessageDialog(hostApp.getMainWindow(),
					e.getClass().getName() + " in StudentDataManager.dbConnectionTest(): " + e.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}

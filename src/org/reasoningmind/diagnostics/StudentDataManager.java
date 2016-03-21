package org.reasoningmind.diagnostics;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Vector;

/**
 * This class handles operations with the student data.
 */
public class StudentDataManager
		extends HashMap<String, StudentHistory>
{
	Diagnostics hostApp;

	private Vector<String> studentIDs;
	private Vector<String> questionIDs;
	private HashMap<String, Vector<Skill>> questionTags;

	StudentDataManager(Diagnostics hostApp) {
		this.hostApp = hostApp;
	}

	void readCSV(File csv) {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection connection = DriverManager.getConnection("jdbc:sqlite:studentRecords.db");

			Statement stat = connection.createStatement();
			stat.executeUpdate("drop table if exists outcomes;");
			stat.executeUpdate(
					"create table outcomes (studentID, timestamp NUMERIC, questionID, isCorrect NUMERIC, skill);");

			PreparedStatement prep = connection.prepareStatement(
					"insert into people values (?, ?, ?, ?, ?);");

			CSVParser csvParser =
					CSVParser.parse(csv, StandardCharsets.UTF_8, CSVFormat.DEFAULT.withHeader().withEscape('"'));

			for (CSVRecord record : csvParser) {
				prep.setString(1, record.get("studentID"));
				prep.setLong(2, Long.parseLong(record.get("timestamp")));
				prep.setString(3,
				               record.get("lesson") + " & " + record.get("problem") + " & " + record.get("question") +
				               " & " + record.get("attempt"));
				prep.setBoolean(4, record.get("isCorrect").equals("correct"));
				prep.setString(5, record.get("skill"));
				prep.addBatch();
			}

			csvParser.close();

			connection.setAutoCommit(false);
			prep.executeBatch();
			connection.setAutoCommit(true);

			connection.close();
		}
		catch (IOException | ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	private void lookupStudentIDs() {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection connection = DriverManager.getConnection("jdbc:sqlite:studentRecords.db");

			Statement stat = connection.createStatement();
			ResultSet rs = stat.executeQuery("select distinct studentID from outcomes;");

			studentIDs = new Vector<>();

			while (rs.next()) {
				studentIDs.add(rs.getString(1));
			}

			connection.close();
		}
		catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	private void lookupQuestionIDs() {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection connection = DriverManager.getConnection("jdbc:sqlite:studentRecords.db");

			Statement stat = connection.createStatement();
			ResultSet rs = stat.executeQuery("select distinct questionID from outcomes;");

			questionIDs = new Vector<>();

			while (rs.next()) {
				questionIDs.add(rs.getString(1));
			}

			connection.close();
		}
		catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	private void lookupSkills() {
		// TODO
//		try {
//			Class.forName("org.sqlite.JDBC");
//			Connection connection = DriverManager.getConnection("jdbc:sqlite:studentRecords.db");
//
//			Statement stat = connection.createStatement();
//			for (String questionID : questionIDs) {
//				ResultSet rs = stat.executeQuery("select distinct skill from outcomes where questionID = '" + questionID + "';");
//
//				Vector<Skill> skills = new Vector<>();
//
//				while (rs.next()) {
//					skills.add(new Skill(rs.getString(1)));
//				}
//			}
//
//			connection.close();
//		}
//		catch (ClassNotFoundException | SQLException e) {
//			e.printStackTrace();
//		}
	}

	void readCLP(File clp) {

	}

	void dbConnectionTest() {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection connection = DriverManager.getConnection("jdbc:sqlite:test.db");

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
		}
		catch (ClassNotFoundException classNotFoundException) {
			JOptionPane.showMessageDialog(
					hostApp,
					"Class \"org.sqlite.JDBC\" is not found.",
					"Error",
					JOptionPane.ERROR_MESSAGE);

		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(hostApp,
			                              e.getClass().getName() + " in StudentDataManager.dbConnectionTest(): " +
			                              e.getMessage(),
			                              "Error",
			                              JOptionPane.ERROR_MESSAGE);
		}
	}
}

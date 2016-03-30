package org.reasoningmind.diagnostics;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

/**
 * This class handles operations with the student data.
 */
class StudentDataManager
		extends HashMap<String, StudentHistory>
{
//	private Diagnostics hostApp;

	private Vector<String> studentIDs;
	private Vector<String> questionIDs;
	private HashMap<String, Vector<String>> questionSkills;
//	private HashMap<String, Integer> questionMultiplicity;

//	StudentDataManager(Diagnostics hostApp) {
//		this.hostApp = hostApp;
//	}

	void loadCSV(File csv) {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection connection = DriverManager.getConnection("jdbc:sqlite:studentRecords.db");

			Statement stat = connection.createStatement();
			stat.executeUpdate("DROP TABLE IF EXISTS outcomes;");
			stat.executeUpdate(
					"CREATE TABLE outcomes (studentID, timestamp NUMERIC, questionID, isCorrect NUMERIC, skill);");

			PreparedStatement prep = connection.prepareStatement(
					"INSERT INTO outcomes VALUES (?, ?, ?, ?, ?);");

			CSVParser csvParser =
					CSVParser.parse(csv, StandardCharsets.UTF_8, CSVFormat.DEFAULT.withHeader());

			for (CSVRecord record : csvParser) {
				prep.setString(1, record.get("studentID"));
				prep.setLong(2, Long.parseLong(record.get("timestamp")));
				prep.setString(3,
				               record.get("lesson") + "/" + record.get("problem") + ": " + record.get("question") +
				               "\t#" + record.get("attempt"));
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

	private void lookupStudentIDs()
			throws ClassNotFoundException {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection connection = DriverManager.getConnection("jdbc:sqlite:studentRecords.db");

			Statement stat = connection.createStatement();
			ResultSet rs = stat.executeQuery("SELECT DISTINCT studentID FROM outcomes;");

			if (studentIDs == null) {
				studentIDs = new Vector<>();
			}

			while (rs.next()) {
				studentIDs.add(rs.getString(1));
			}

			connection.close();
		}
		catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

	private void lookupQuestions()
			throws ClassNotFoundException {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection connection = DriverManager.getConnection("jdbc:sqlite:studentRecords.db");

			Statement stat = connection.createStatement();
			ResultSet rs = stat.executeQuery("SELECT DISTINCT questionID FROM outcomes;");

			if (questionIDs == null) {
				questionIDs = new Vector<>();
			}

			while (rs.next()) {
				questionIDs.add(rs.getString(1));
			}

			connection.close();
		}
		catch (SQLException sqle) {
			sqle.printStackTrace();
		}

		lookupQuestionSkills();
	}

	private void lookupQuestionSkills()
			throws ClassNotFoundException {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection connection = DriverManager.getConnection("jdbc:sqlite:studentRecords.db");
			Statement stat = connection.createStatement();
			ResultSet rs;

			if (questionSkills == null) {
				questionSkills = new HashMap<>();
//				questionMultiplicity = new HashMap<>();
			}

			for (String questionID : questionIDs) {
				rs = stat.executeQuery(
						"SELECT DISTINCT skill FROM outcomes WHERE questionID = '" + questionID + "';");

				Vector<String> skills = new Vector<>();

				while (rs.next()) {
					skills.add(rs.getString(1));
				}

				questionSkills.put(questionID, skills);
//				questionMultiplicity.put(questionID, skills.size());
			}

			connection.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	Vector<String> getStudentIDs() {
		return studentIDs;
	}

//	Vector<String> getQuestionIDs() {
//		return questionIDs;
//	}

//	Vector<String> getQuestionSkills(String questionID) {
//		if (questionID != null && questionSkills != null) {
//			return questionSkills.get(questionID);
//		}
//		else {
//			System.out.println(
//					"Question skills are missing for " + (questionID != null ?"\"" + questionID + "\"" :"NULL"));
//			return null;
//		}
//	}

//	int getQuestionMultiplicity(String questionID) {
//		if (questionID != null && questionMultiplicity != null) {
//			return questionMultiplicity.get(questionID);
//		}
//		else {
//			System.out.println(
//					"Question skills are missing for " + (questionID != null ?"\"" + questionID + "\"" :"NULL"));
//			return 0;
//		}
//	}

	void refresh() {
		studentIDs = null;
		questionIDs = null;
		questionSkills = null;

		try {
			lookupStudentIDs();
			lookupQuestions();
		}
		catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
	}

	void initHistory() {
		for (String student : studentIDs) {
			StudentHistory history = new StudentHistory();
			put(student, history);

			try {
				Class.forName("org.sqlite.JDBC");
				Connection connection = DriverManager.getConnection("jdbc:sqlite:studentRecords.db");

				Statement stat = connection.createStatement();
				ResultSet rs = stat.executeQuery(
						"SELECT DISTINCT timestamp, questionID, isCorrect FROM outcomes WHERE studentID='" +
						student + "';");

				while (rs.next()) {
					Long timestamp = rs.getLong("timestamp");
					String questionID = rs.getString("questionID");
					boolean isCorrect = rs.getBoolean("isCorrect");

					history.put(timestamp, questionID, new HashSet<>(questionSkills.get(questionID)), isCorrect);
				}
			}
			catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}

			history.buildWeights();
		}
	}

//	void readCLP(File clp) {
//
//	}

//	void dbConnectionTest() {
//		try {
//			Class.forName("org.sqlite.JDBC");
//			Connection connection = DriverManager.getConnection("jdbc:sqlite:test.db");
//
//			Statement stat = connection.createStatement();
//			stat.executeUpdate("drop table if exists people;");
//			stat.executeUpdate("create table people (name, occupation);");
//
//			PreparedStatement prep = connection.prepareStatement(
//					"insert into people values (?, ?);");
//			prep.setString(1, "Gandhi");
//			prep.setString(2, "politics");
//			prep.addBatch();
//			prep.setString(1, "Turing");
//			prep.setString(2, "computers");
//			prep.addBatch();
//			prep.setString(1, "Wittgenstein");
//			prep.setString(2, "smartypants");
//			prep.addBatch();
//
//			connection.setAutoCommit(false);
//			prep.executeBatch();
//			connection.setAutoCommit(true);
//
//			ResultSet rs = stat.executeQuery("select * from people;");
//			while (rs.next()) {
//				System.out.println("name = " + rs.getString("name"));
//				System.out.println("job = " + rs.getString("occupation"));
//			}
//			rs.close();
//			connection.close();
//		}
//		catch (ClassNotFoundException classNotFoundException) {
//			JOptionPane.showMessageDialog(
//					hostApp,
//					"Class \"org.sqlite.JDBC\" is not found.",
//					"Error",
//					JOptionPane.ERROR_MESSAGE);
//
//		}
//		catch (Exception e) {
//			JOptionPane.showMessageDialog(hostApp,
//			                              e.getClass().getName() + " in StudentDataManager.dbConnectionTest(): " +
//			                              e.getMessage(),
//			                              "Error",
//			                              JOptionPane.ERROR_MESSAGE);
//		}
//	}
}

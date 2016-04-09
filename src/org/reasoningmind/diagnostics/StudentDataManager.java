package org.reasoningmind.diagnostics;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Vector;

/**
 * This class handles operations with the student data.
 */
class StudentDataManager
		extends HashMap<String, StudentHistory>
{
	private Vector<String> studentIDs;
	private Vector<String> questionIDs;
	private HashMap<String, Vector<String>> questionSkills;

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
				               record.get("lesson") + "/" + record.get("problem") + ": " +
				               "        ".substring(record.get("question").length()) + record.get("question") +
				               "  #" + record.get("attempt"));
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
			throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");

		try (Connection connection = DriverManager.getConnection("jdbc:sqlite:studentRecords.db")) {
			if (studentIDs == null) {
				studentIDs = new Vector<>();
			}

			Statement stat = connection.createStatement();

			ResultSet rs = stat.executeQuery("SELECT DISTINCT studentID FROM outcomes;");
			while (rs.next()) {
				studentIDs.add(rs.getString(1));
			}
		}

	}

	private void lookupQuestions()
			throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");

		try (Connection connection = DriverManager.getConnection("jdbc:sqlite:studentRecords.db")) {
			if (questionIDs == null) {
				questionIDs = new Vector<>();
			}

			Statement stat = connection.createStatement();
			ResultSet rs = stat.executeQuery("SELECT DISTINCT questionID FROM outcomes;");
			while (rs.next()) {
				questionIDs.add(rs.getString(1));
			}
		}

		lookupQuestionSkills();
	}

	private void lookupQuestionSkills()
			throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");

		try (Connection connection = DriverManager.getConnection("jdbc:sqlite:studentRecords.db")) {
			Statement stat = connection.createStatement();
			ResultSet rs;

			if (questionSkills == null) {
				questionSkills = new HashMap<>();
			}

			for (String questionID : questionIDs) {
				Vector<String> skills = new Vector<>();
				questionSkills.put(questionID, skills);

				rs = stat.executeQuery("SELECT DISTINCT skill FROM outcomes WHERE questionID = '" + questionID + "';");
				while (rs.next()) {
					skills.add(rs.getString(1));
				}
			}
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

	void fetchStudentsAndQuestions(Diagnostics host) {
		studentIDs = new Vector<>();
		questionIDs = new Vector<>();
		questionSkills = new HashMap<>();

		try {
			lookupStudentIDs();
			lookupQuestions();
		}
		catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
		catch (SQLException sqle) {
			JOptionPane.showMessageDialog(host,
			                              Locale.getDefault().equals(new Locale("ru", "RU"))
			                              ?"Данные об учениках не были загружены."
			                              :"The student data has not been loaded.",
			                              Locale.getDefault().equals(new Locale("ru", "RU")) ?"Нет данных" :"No data",
			                              JOptionPane.WARNING_MESSAGE);

			host.actionPerformed(new ActionEvent(this, 0, "BrowseCSV"));
		}
	}

	StudentHistory getHistory(String student) {
		if (get(student) != null) {
			return get(student);
		}

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

		return history;
	}
//
//	void initHistory() {
//		for (String student : studentIDs) {
//			StudentHistory history = new StudentHistory();
//			put(student, history);
//
//			try {
//				Class.forName("org.sqlite.JDBC");
//				Connection connection = DriverManager.getConnection("jdbc:sqlite:studentRecords.db");
//
//				Statement stat = connection.createStatement();
//				ResultSet rs = stat.executeQuery(
//						"SELECT DISTINCT timestamp, questionID, isCorrect FROM outcomes WHERE studentID='" +
//						student + "';");
//
//				while (rs.next()) {
//					Long timestamp = rs.getLong("timestamp");
//					String questionID = rs.getString("questionID");
//					boolean isCorrect = rs.getBoolean("isCorrect");
//
//					history.put(timestamp, questionID, new HashSet<>(questionSkills.get(questionID)), isCorrect);
//				}
//			}
//			catch (ClassNotFoundException | SQLException e) {
//				e.printStackTrace();
//			}
//
//			history.buildWeights();
//		}
//	}
}
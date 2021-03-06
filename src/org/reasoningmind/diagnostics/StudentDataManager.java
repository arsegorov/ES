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
	Vector<String> getStudentIDs() {
		return studentIDs;
	}

	private Vector<String> questionIDs;
	private HashMap<String, Integer> lastLesson;
	private HashMap<String, Vector<String>> questionSkills;


	void loadCSV(File csv) {
		try {
			Class.forName("org.sqlite.JDBC");

			try (Connection connection = DriverManager.getConnection("jdbc:sqlite:studentRecords.db")) {

				Statement stat = connection.createStatement();
				stat.executeUpdate("DROP TABLE IF EXISTS outcomes;");
				stat.executeUpdate(
						"CREATE TABLE outcomes (studentID, timestamp NUMERIC, questionID, isCorrect NUMERIC, skill);");

				PreparedStatement prep = connection.prepareStatement(
						"INSERT INTO outcomes VALUES (?, ?, ?, ?, ?);");

				try (CSVParser csvParser =
						     CSVParser.parse(csv, StandardCharsets.UTF_8, CSVFormat.DEFAULT.withHeader())) {

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
				}

				connection.setAutoCommit(false);
				prep.executeBatch();
				connection.setAutoCommit(true);
			}
		}
		catch (IOException | ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}


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
			                              ?"Данные об учениках не загружены."
			                              :"The student data has not been loaded.",
			                              Locale.getDefault().equals(new Locale("ru", "RU")) ?"Нет данных" :"No data",
			                              JOptionPane.WARNING_MESSAGE);

			host.actionPerformed(new ActionEvent(this, 0, "BrowseCSV"));
		}
	}

	private void lookupStudentIDs()
			throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");

		try (Connection connection = DriverManager.getConnection("jdbc:sqlite:studentRecords.db")) {
			if (studentIDs == null) {
				studentIDs = new Vector<>();
			}
			if (lastLesson == null) {
				lastLesson = new HashMap<>();
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

	int getLastLesson(String student) {
		if (lastLesson.get(student) != null) {
			return lastLesson.get(student);
		}

		lastLesson.put(student, 0);

		try {
			Class.forName("org.sqlite.JDBC");

			try (Connection connection = DriverManager.getConnection("jdbc:sqlite:studentRecords.db")) {

				Statement stat = connection.createStatement();
				ResultSet rs = stat.executeQuery(
						"SELECT DISTINCT questionID FROM outcomes WHERE studentID='" + student +
						"' ORDER BY questionID DESC LIMIT 1;");

				if (rs.next()) {
					lastLesson.put(student, Integer.parseInt(rs.getString("questionID").substring(0, 3)));
				}
			}
		}
		catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

		return lastLesson.get(student);
	}

	StudentHistory getHistory(String student) {
		if (get(student) != null) {
			return get(student);
		}

		StudentHistory history = new StudentHistory();
		put(student, history);

		try {
			Class.forName("org.sqlite.JDBC");

			try (Connection connection = DriverManager.getConnection("jdbc:sqlite:studentRecords.db")) {

				Statement stat = connection.createStatement();
				ResultSet rs = stat.executeQuery(
						"SELECT DISTINCT timestamp, questionID, isCorrect FROM outcomes WHERE studentID='" + student +
						"';");

				while (rs.next()) {
					Long timestamp = rs.getLong("timestamp");
					String questionID = rs.getString("questionID");
					boolean isCorrect = rs.getBoolean("isCorrect");

					history.put(timestamp, questionID, new HashSet<>(questionSkills.get(questionID)), isCorrect);
				}
			}
		}
		catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

		history.buildWeights();

		return history;
	}
}
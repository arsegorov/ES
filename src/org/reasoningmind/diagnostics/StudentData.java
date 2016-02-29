package org.reasoningmind.diagnostics;

import org.apache.commons.csv.CSVParser;
//import org.apache.commons.csv.CSVFormat;
//import java.nio.charset.StandardCharsets;

import javax.swing.JOptionPane;

import java.io.File;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * This class handles operations with the student data.
 */
public class StudentData {
    Diagnostics hostApp;
    Connection connection;

    StudentData(Diagnostics hostApp) {
        this.hostApp = hostApp;
    }

    void readCSV(File csv) {

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

            throw new Exception("Sample exception");
        } catch (ClassNotFoundException classNotFoundException) {
            JOptionPane.showMessageDialog(hostApp.getMainWindow(), "Class \"org.sqlite.JDBC\" is not found.", "Error", JOptionPane.ERROR_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(hostApp.getMainWindow(), e.getClass().getName() + " in StudentData.dbConnectionTest(): " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

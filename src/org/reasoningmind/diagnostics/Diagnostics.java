/**
 * The root class for the Diagnostics project
 */
package org.reasoningmind.diagnostics;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

//import net.sf.clipsrules.jni.*;

//import org.apache.commons.csv.CSVParser;
//import org.apache.commons.csv.CSVFormat;
//import java.nio.charset.StandardCharsets;

//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.Statement;

public class Diagnostics implements ActionListener {
    JFrame mainWindow;
    JButton browseButton;
    JTextField filePathText;

    ResourceBundle resources;

//    Environment clips;

    final JFileChooser fileChooser = new JFileChooser();

    Diagnostics() {
        try {
            resources = ResourceBundle.getBundle("org.reasoningmind.diagnostics.resources.Diagnostics", Locale.getDefault());
        }
        catch (MissingResourceException mre) {
            mre.printStackTrace();
            return;
        }

        mainWindow = new JFrame(resources.getString("MainWindowTitle"));
        mainWindow.getContentPane().setLayout(new BorderLayout());
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        FileNameExtensionFilter fnef = new FileNameExtensionFilter("Comma Separated Values (.csv)", "csv");
        fileChooser.setFileFilter(fnef);

        filePathText = new JTextField("...");
        filePathText.setPreferredSize(new Dimension(500, 24));
        browseButton = new JButton("Browse");
        browseButton.addActionListener(this);

        JPanel input = new JPanel(new FlowLayout());
        input.add(filePathText);
        input.add(browseButton);
        input.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Select a file to read student data from"));
        mainWindow.add(input, BorderLayout.CENTER);

        mainWindow.pack();
        mainWindow.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
                                       public void run() {
                                           new Diagnostics();
                                       }
                                   }
        );
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == browseButton) {
            int returnVal = fileChooser.showOpenDialog(mainWindow);

            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                filePathText.setText(file.getPath());
            }
            else {
                filePathText.setText("");
            }
        }
    }
}

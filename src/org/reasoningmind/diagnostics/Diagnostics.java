/**
 * The root class for the Diagnostics project
 */
package org.reasoningmind.diagnostics;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sf.clipsrules.jni.Environment;

/**
 * This is the main class of the diagnostics application.
 * It provides user interface and an interface with the CLIPS system.
 */
public class Diagnostics implements ActionListener {
    //
    // Parts of user interface
    //
    JFrame mainWindow;
    public JFrame getMainWindow() {
        return mainWindow;
    }

//    JButton browseButton;
    JTextField filePathText;

    //
    // Text resources
    //
    ResourceBundle resources;

    //
    // CLIPS related
    //
    Environment clips;
    boolean isExecuting = false;
    Thread executionThread;

    //
    // Data handlers
    // Need only one of each for the life of the application
    //
    final JFileChooser fileChooser = new JFileChooser();
    final StudentDataManager sdm = new StudentDataManager(this);


    Diagnostics() {
        try {
            resources = ResourceBundle.getBundle(
                    "org.reasoningmind.diagnostics.resources.Diagnostics",
                    Locale.getDefault()
            );
        } catch (MissingResourceException mre) {
            mre.printStackTrace();
            return;
        }

        mainWindow = new JFrame(resources.getString("MainWindowTitle"));
        mainWindow.getContentPane().setLayout(new BorderLayout());
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel inputLabel = new JLabel();
        inputLabel.setIcon(
                new ImageIcon(
                        getClass().getClassLoader()
                                .getResource("org/reasoningmind/diagnostics/resources/data_input_label.png")
                )
        );
        inputLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainWindow.add(inputLabel, BorderLayout.NORTH);

        JPanel input = new JPanel(new FlowLayout());
        input.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Select a file to read student data from"
        ));

        filePathText = new JTextField("(no file selected)");
        filePathText.setPreferredSize(new Dimension(500, 24));
        input.add(filePathText);

        JButton browseButton = new JButton("Browse");
        browseButton.setActionCommand("BrowseCLP");
        browseButton.addActionListener(this);
        input.add(browseButton);

        mainWindow.add(input, BorderLayout.CENTER);
        mainWindow.pack();
        mainWindow.setLocation(300, 200);
        mainWindow.setVisible(true);

        FileNameExtensionFilter fnef = new FileNameExtensionFilter(
                resources.getString("CLIPSFile"),
                resources.getString("CLIPSFileExt")
        );
        fileChooser.setFileFilter(fnef);

        sdm.dbConnectionTest();
//
        clips = new Environment();
        clips.loadFromResource("org/reasoningmind/diagnostics/resources/defs.clp");
        clips.loadFromResource("org/reasoningmind/diagnostics/resources/rules.clp");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        new Diagnostics();
                    }
                }
        );
    }

    public void runDiagnostics()
    {
        Runnable runThread =
                new Runnable()
                {
                    public void run()
                    {
                        clips.run();
//
//                        SwingUtilities.invokeLater(
//                                new Runnable()
//                                {
//                                    public void run()
//                                    {
//                                        try
//                                        { updateGrid(); }
//                                        catch (Exception e)
//                                        { e.printStackTrace(); }
//                                    }
//                                });
                    }
                };

        isExecuting = true;
        executionThread = new Thread(runThread);
        mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        executionThread.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("BrowseCLP")) {
            int returnVal = fileChooser.showOpenDialog(mainWindow);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                filePathText.setText(file.getPath());
            }
            else {
                filePathText.setText("(no file selected)");
            }
        }
    }
}

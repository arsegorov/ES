/**
 * The root class for the Diagnostics project
 */
package org.reasoningmind.diagnostics;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Diagnostics implements ActionListener {
    JFrame mainWindow;
    JButton browseButton;
    JTextField filePathText;

    ResourceBundle resources;

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

        JPanel input = new JPanel(new FlowLayout());

        filePathText = new JTextField("...");
        filePathText.setPreferredSize(new Dimension(500, 24));
        input.add(filePathText);

        browseButton = new JButton("Browse");
        browseButton.setPreferredSize(new Dimension(100, 30));
        input.add(browseButton);
        browseButton.addActionListener(this);

        FileNameExtensionFilter fnef = new FileNameExtensionFilter("Text files", "txt");
        fileChooser.setFileFilter(fnef);

        mainWindow.add(input, BorderLayout.CENTER);

        mainWindow.pack();
        mainWindow.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(
                new Runnable()
                {
                    public void run() { new Diagnostics(); }
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

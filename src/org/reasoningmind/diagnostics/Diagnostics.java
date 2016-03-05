/**
 * The root class for the Diagnostics project
 */
package org.reasoningmind.diagnostics;

import net.sf.clipsrules.jni.Environment;
import net.sf.clipsrules.jni.MultifieldValue;
import net.sf.clipsrules.jni.PrimitiveValue;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This is the main class of the diagnostics application.
 * It provides user interface and an interface with the CLIPS system.
 */
public class Diagnostics implements ActionListener {
	//
	// Data handlers
	// Need only one of each for the life of the application
	//
	final JFileChooser fileChooser = new JFileChooser();
	//
	// Parts of user interface
	//
	JFrame mainWindow;
	JTextField filePathTF, clipsCommandTF, factFilterTF;
	JTextArea outputArea;

	//
	// Text resources
	//
	ResourceBundle resources;

	public ResourceBundle getResources() {
		return resources;
	}

	//
	// CLIPS execution related fields
	//

	/**
	 * Make sure to specify -Djava.library.path="&lt;path to CLIPSJNI directory&gt;."
	 * This is where the CLIPSJNI.dll is.
	 * In IntelliJ Idea, the option is specified under VM Options in "Run/Debug Configurations"
	 */
	Environment clips;

	public Environment getClips() {
		return clips;
	}

	boolean isExecuting = false;
	Thread executionThread;

	Diagnostics() {
		//
		// Accessing the resources file
		//
		try {
			resources = ResourceBundle.getBundle(
					"org.reasoningmind.diagnostics.resources.Diagnostics",
					Locale.getDefault()
			);
		} catch (MissingResourceException mre) {
			mre.printStackTrace();
			return;
		}

		// ************************** //
		// Setting up the main window //
		// ************************** //
		mainWindow = new JFrame(resources.getString("MainWindowTitle"));
		mainWindow.getContentPane().setLayout(new BorderLayout());
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

		//
		// A cheerful image on the main window :)
		//
		JLabel hintLabel = new JLabel();
		hintLabel.setIcon(
				new ImageIcon(
						getClass().getClassLoader()
								.getResource("org/reasoningmind/diagnostics/resources/data_input_label.png")
				)
		);
		topPanel.add(hintLabel, BorderLayout.WEST);

		// ***************** //
		// The log text area //
		// ***************** //
		outputArea = new JTextArea();
		outputArea.setFont(Font.decode("Consolas-Plain-12"));
		outputArea.setColumns(80);
		outputArea.setEditable(false);
		outputArea.setLineWrap(true);
		outputArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		JScrollPane scrPane = new JScrollPane(outputArea);
		outputArea.setAutoscrolls(true);

		// **************************** //
		// The "command line interface" //
		// **************************** //
		clipsCommandTF = new JTextField("");
		clipsCommandTF.setPreferredSize(new Dimension(outputArea.getWidth(), 24));
		clipsCommandTF.setFont(Font.decode("Consolas-Plain-12"));
		clipsCommandTF.addActionListener(this);

		JPanel promptPanel = new JPanel(new BorderLayout());
		promptPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				"Enter a CLIPS expression to evaluate"
		));
		promptPanel.add(clipsCommandTF, BorderLayout.CENTER);

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(scrPane, BorderLayout.CENTER);
		rightPanel.add(promptPanel, BorderLayout.SOUTH);

		topPanel.add(rightPanel, BorderLayout.CENTER);

		mainWindow.add(topPanel, BorderLayout.CENTER);

		// *********************** //
		// The file selection area //
		// *********************** //
		JPanel fileSelectionPanel = new JPanel(new BorderLayout());
		fileSelectionPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				"Select a file to read student data from"
		));

		// File path text field
		filePathTF = new JTextField("");
		filePathTF.setPreferredSize(new Dimension(topPanel.getWidth(), 24));
		filePathTF.addActionListener(this);
		fileSelectionPanel.add(filePathTF, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
		fileSelectionPanel.add(buttonPanel, BorderLayout.EAST);

		// File chooser button, opens the file chooser
		JButton browseButton = new JButton("Browse");
		browseButton.setActionCommand("BrowseCLP");
		browseButton.addActionListener(this);
		buttonPanel.add(browseButton);

		// Load button, tells CLIPS to load a file
		JButton loadButton = new JButton("Load");
		loadButton.setActionCommand("LoadCLP");
		loadButton.addActionListener(this);
		buttonPanel.add(loadButton);

		mainWindow.add(fileSelectionPanel, BorderLayout.NORTH);

		// ****************** //
		// The filtering line //
		// ****************** //
		JPanel filterPanel = new JPanel(new BorderLayout());
		filterPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				"Enter a fact filtering string (use \"?fact\" as the variable)"
		));

		// ... text field
		factFilterTF = new JTextField("");
		factFilterTF.setPreferredSize(new Dimension(topPanel.getWidth(), 24));
		factFilterTF.addActionListener(this);
		filterPanel.add(factFilterTF, BorderLayout.CENTER);

		// ... button
		JButton showFacts = new JButton("Show Facts");
		showFacts.setActionCommand("ShowFacts");
		showFacts.addActionListener(this);
		filterPanel.add(showFacts, BorderLayout.EAST);

		mainWindow.add(filterPanel, BorderLayout.SOUTH);

		mainWindow.pack();
		mainWindow.setLocation(300, 200);
		mainWindow.setVisible(true);

		FileNameExtensionFilter fnef = new FileNameExtensionFilter(
				resources.getString("CLIPSFile"),
				resources.getString("CLIPSFileExt")
		);
		fileChooser.setFileFilter(fnef);

		// Uncomment this to test the database setup
//        sdm.dbConnectionTest();

		clips = new Environment();
		outputArea.append(clips.getVersion() + "\n\nCLIPS> ");
		clips.watch("rules");
		clips.loadFromResource("/org/reasoningmind/diagnostics/resources/defs.clp");
		clips.loadFromResource("/org/reasoningmind/diagnostics/resources/rules.clp");
	}
//    final StudentDataManager sdm = new StudentDataManager(this);

	public static void main(String[] args) {
		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						new Diagnostics();
					}
				}
		);
	}

	public JFrame getMainWindow() {
		return mainWindow;
	}

	/**
	 * Passes the <code>expression</code> onto the <code><b>net.sf.clipsrules.jni.Environment.eval</b>(String)</code>
	 * method and displays
	 * the result in the <a
	 * href="#outputArea">output area</a>.
	 *
	 * @param expression
	 * 		the CLIPS expression to pass onto the <code><b>Environment.eval</b>(String)</code> method
	 *
	 * @return the result of the <code><b>Environment.eval</b>(String)</code> method
	 *
	 * @see net.sf.clipsrules.jni.Environment#eval(String)
	 */
	PrimitiveValue eval(String expression) {
		PrimitiveValue res = clips.eval(expression);

		if (res.getClass().getSimpleName().equals("MultifieldValue")) {
			MultifieldValue mv = (MultifieldValue) res;

			outputArea.append(expression + "\n\n");
			for (int i = 0; i < mv.size(); i++) {
				outputArea.append(mv.get(i).toString() + "\n");
			}

			outputArea.append("\nCLIPS> ");
		}
		else if (res.getClass().getSimpleName().equals("VoidValue")) {
			outputArea.append(expression + "\n\n/* Void Value */\n\nCLIPS> ");
		}
		else {
			outputArea.append(expression + "\n\n" + res.toString() + "\n\nCLIPS> ");
		}

		return res;
	}

	public void runDiagnostics() {
		Runnable runThread =
				new Runnable() {
					public void run() {
						clips.run();

						SwingUtilities.invokeLater(
								new Runnable() {
									@Override
									public void run() {
										isExecuting = false;
										mainWindow.setCursor(Cursor.getDefaultCursor());
									}
								}
						);
					}
				};

		isExecuting = true;
		executionThread = new Thread(runThread);
		mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		executionThread.start();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (isExecuting) {
			return;
		}

		if (e.getActionCommand().equals("BrowseCLP")) {
			int returnVal = fileChooser.showOpenDialog(mainWindow);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();

				eval("(load \"" + file.getPath().replace("\\", "\\\\") + "\")");
				clips.reset();

//				runDiagnostics();
			}
		}
		else if (e.getActionCommand().equals("LoadCLP") || e.getSource() == filePathTF) {
			File file = new File(filePathTF.getText());

			if (file.exists()) {
				eval("(load \"" + filePathTF.getText().replace("\\", "\\\\") + "\")");
				clips.reset();

//				runDiagnostics();
			}
			else {
				JOptionPane.showMessageDialog(mainWindow,
						"Specified file doesn't exist.",
						"Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (e.getActionCommand().equals("ShowFacts") || e.getSource() == factFilterTF) {
			new FactsFrame(this, factFilterTF.getText());
		}
		else if (e.getSource() == clipsCommandTF) {
			eval(clipsCommandTF.getText().replace("\\", "\\\\"));
			clipsCommandTF.setText("");
			clips.printBanner();
		}
	}
}

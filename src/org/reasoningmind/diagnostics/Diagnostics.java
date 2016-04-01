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
import java.util.Set;

/**
 * This is the main class of the diagnostics application.
 * It provides the user interface and communication with the CLIPS system.
 */
public class Diagnostics extends JFrame implements ActionListener
{

	// Data handlers
	// Need only one of each for the life of the application
	private final JFileChooser fileChooser = new JFileChooser();

	// Parts of user interface
	private JTextField filePathTF, clipsCommandTF, factFilterTF;
	private JTextArea outputArea;

	// Application text resources
	private ResourceBundle resources;

	ResourceBundle getResources() {
		return resources;
	}

	// CLIPS execution-related fields
	/**
	 * Make sure to specify -Djava.library.path="&lt;path to CLIPSJNI directory&gt;."
	 * This is where the CLIPSJNI.dll is.
	 * In IntelliJ Idea, the option is specified under VM Options in "Run/Debug Configurations"
	 */
	private Environment clips;

	Environment getClips() {
		return clips;
	}

	// Concurrency-related fields
	private boolean isExecuting = false;
	private Thread executionThread;

	private final StudentDataManager dataManager = new StudentDataManager();


	// ============
	// Constructors
	// ============

	/**
	 * The default constructor that sets up the main window of the application and creates a CLIPS environment with the
	 * diagnostics <i>defrules</i> and <i>deftemplates</i> loaded from the resources.
	 */
	private Diagnostics() {
		// Accessing the resources file
		try {
			resources = ResourceBundle.getBundle(
					"org.reasoningmind.diagnostics.resources.Diagnostics",
					Locale.getDefault()
			);
		}
		catch (MissingResourceException mre) {
			mre.printStackTrace();
			return;
		}

		// **************************
		// Setting up the main window
		// **************************
		this.setTitle(resources.getString("MainWindowTitle"));
		this.getContentPane().setLayout(new BorderLayout());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// The main area of the layout
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		// A cheerful image on the main window :)
//		this.addCheerfulIcon(mainPanel);
		// The "command line interface"
		this.addClipsPanel(mainPanel);
		// The input file selection area
//		this.addFileSelectionPanel();
		// The facts filter area
//		this.addFilterPanel();

		this.add(mainPanel, BorderLayout.CENTER);
		// Aligning the layout properly
		this.pack();
		this.setLocation(300, 200);
		// Showing the main window
		this.setVisible(true);

		clips = new Environment();
		outputArea.append(Environment.getVersion() + "\n" + resources.getString("CLIPSPrompt"));
		clips.loadFromResource("/org/reasoningmind/diagnostics/resources/defs.clp");
		clips.loadFromResource("/org/reasoningmind/diagnostics/resources/rules.clp");

//		TODO: all the action is here
//		dataManager.loadCSV(new File(System.getProperty("user.home") + "\\Desktop\\sample outcomes.csv"));
		dataManager.refresh();
		dataManager.initHistory();
	}

	private void addCheerfulIcon(JPanel mainPanel) {
		JLabel iconLabel = new JLabel();
		iconLabel.setIcon(
				new ImageIcon(
						getClass().getClassLoader()
						          .getResource("org/reasoningmind/diagnostics/resources/data_input_label.png")
				)
		);

		mainPanel.add(iconLabel, BorderLayout.WEST);
	}

	private void addFilterPanel() {
		JPanel filterPanel = new JPanel(new BorderLayout());
		addBorder(filterPanel, "MainWindowFilterPrompt");

		// Filter definition text field
		factFilterTF = new JTextField("");
		factFilterTF.addActionListener(this);
		filterPanel.add(factFilterTF, BorderLayout.CENTER);

		// Filter run button
		JButton showFacts = new JButton("Show Facts");
		showFacts.setActionCommand("ShowFacts");
		showFacts.addActionListener(this);
		filterPanel.add(showFacts, BorderLayout.EAST);

		this.add(filterPanel, BorderLayout.SOUTH);
	}

	private void addFileSelectionPanel() {
		JPanel fileSelectionPanel = new JPanel(new BorderLayout());
		addBorder(fileSelectionPanel, "MainWindowInputFilePrompt");

		// File path text field
		filePathTF = new JTextField("");
		filePathTF.addActionListener(this);
		fileSelectionPanel.add(filePathTF, BorderLayout.CENTER);

		// File chooser button, opens the file chooser
		JButton browseButton = new JButton("Browse");
		browseButton.setActionCommand("BrowseCLP");
		browseButton.addActionListener(this);
		fileSelectionPanel.add(browseButton, BorderLayout.EAST);

		// The file filter used for browsing the system for input file
		FileNameExtensionFilter fnef = new FileNameExtensionFilter(
				resources.getString("CLIPSFileDescription"),
				resources.getString("CLIPSFileExt")
		);
		fileChooser.setFileFilter(fnef);

		this.add(fileSelectionPanel, BorderLayout.NORTH);
	}

	private void addClipsPanel(JPanel mainPanel) {
		JPanel clipsPanel = new JPanel(new BorderLayout());

		// The log text area
		outputArea = new JTextArea();
		outputArea.setFont(Font.decode("Consolas-Plain-12"));
		outputArea.setColumns(80);
		outputArea.setRows(25);
		outputArea.setEditable(false);
		outputArea.setLineWrap(true);
		outputArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		outputArea.setAutoscrolls(true);
		JScrollPane outputScrollPane = new JScrollPane(outputArea);
		clipsPanel.add(outputScrollPane, BorderLayout.CENTER);

		// The input line
		JPanel promptPanel = new JPanel(new BorderLayout());
		addBorder(promptPanel, "MainWindowCLIPSPrompt");

		clipsCommandTF = new JTextField("");
		clipsCommandTF.setPreferredSize(new Dimension(outputArea.getWidth(), 24));
		clipsCommandTF.setFont(Font.decode("Consolas-Plain-12"));
		clipsCommandTF.addActionListener(this);
		promptPanel.add(clipsCommandTF, BorderLayout.CENTER);

		clipsPanel.add(promptPanel, BorderLayout.SOUTH);

		mainPanel.add(clipsPanel, BorderLayout.CENTER);
	}

	private void addBorder(JPanel panel, String resourceID) {
		panel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				resources.getString(resourceID)
		));
	}


	// =======
	// Methods
	// =======

	/**
	 * Creates the main window of the application.
	 *
	 * @param args
	 * 		ignored
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(
				Diagnostics::new
		);
	}


	/**
	 * This is a convenience method. It passes the <code>expression</code> onto the
	 * <code><b>net.sf.clipsrules.jni.Environment.eval</b>(String)</code>
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
	private PrimitiveValue eval(String expression) {
		// Escaping the back slashes in the expression before passing the string to the environment
		// (each '\' should be changed to "\\"),
		// then running CLIPS and getting the result
		PrimitiveValue res = clips.eval(expression.replace("\\", "\\\\"));

		// Checking if the result is a multi-filed value
		if (res.getClass().getSimpleName().equals("MultifieldValue")) {
			MultifieldValue mv = (MultifieldValue) res;

			outputArea.append(expression + "\n\n");
			for (int i = 0; i < mv.size(); i++) {
				outputArea.append(mv.get(i).toString() + "\n");
			}

			outputArea.append(resources.getString("CLIPSPrompt"));
		}
		// ... or a void value
		else if (res.getClass().getSimpleName().equals("VoidValue")) {
			outputArea.append(expression + "\n\n/* Void Value */\n" + resources.getString("CLIPSPrompt"));
		}
		// ... or a single value
		else {
			outputArea.append(expression + "\n\n" + res.toString() + "\n" + resources.getString("CLIPSPrompt"));
		}

		return res;
	}


//	/**
//	 * Invokes the <code>(run)</code> command in the CLIPS environment in a separate thread.
//	 */
//	private void runDiagnostics() {
//		Runnable runThread =
//				() -> {
//					clips.run();
//
//					SwingUtilities.invokeLater(
//							() -> {
//								isExecuting = false;
//
//								filePathTF.setEnabled(true);
//								clipsCommandTF.setEnabled(true);
//								filePathTF.setEnabled(true);
//								setCursor(Cursor.getDefaultCursor());
//							}
//					);
//				};
//
//		isExecuting = true;
//		filePathTF.setEnabled(false);
//		clipsCommandTF.setEnabled(false);
//		filePathTF.setEnabled(false);
//
//		executionThread = new Thread(runThread);
//		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//		executionThread.start();
//	}


	/**
	 * This method is inherited from the ActionListener interface, and is invoked whenever a registered event occurs.
	 *
	 * @param e
	 * 		the registered event
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (isExecuting) {
			return;
		}

		if (e.getActionCommand().equals("BrowseCLP")) {
			int returnVal = fileChooser.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				filePathTF.setText(file.getPath());

				eval("(load \"" + file.getPath() + "\")");
				clips.reset();
			}
		}
		else if (e.getSource() == filePathTF) {
			File file = new File(filePathTF.getText());

			if (file.exists()) {
				eval("(load \"" + filePathTF.getText() + "\")");
				clips.reset();
			}
			else {
				JOptionPane.showMessageDialog(this,
				                              "Specified file doesn't exist.",
				                              "Error",
				                              JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (e.getActionCommand().equals("ShowFacts") || e.getSource() == factFilterTF) {
			MultifieldValue mv = (MultifieldValue) clips.eval("(find-fact " + factFilterTF.getText() + ")");
			if (mv.size() == 0) {
				JOptionPane.showMessageDialog(this,
				                              "There are no facts that match the query:\n" + factFilterTF.getText(),
				                              "Facts not found",
				                              JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			new FactsFrame(this, factFilterTF.getText());
		}
		else if (e.getSource() == clipsCommandTF) {
			// TODO: add a separate control for showing student info
//			eval(clipsCommandTF.getText());
//			clipsCommandTF.setText("");
//			clips.printBanner();
			outputArea.setText("");

			String studentID = dataManager.getStudentIDs().get(Integer.parseInt(clipsCommandTF.getText()));
			outputArea.append(studentID + "\n");

			StudentHistory
					history = dataManager.get(studentID);

			Set<String> skills = history.keySet();

			for (String skill : skills) {
				StudentHistory.SkillHistory skillHistory = history.get(skill);
				outputArea.append(String.format("\t%s: %1.3f\n",
				                                skill,
				                                skillHistory.trend()
				));

				Set<StudentHistory.RecordKey> responses = skillHistory.keySet();

				for (StudentHistory.RecordKey
						response : responses) {
					outputArea.append(String.format("\t\t%d:\t%d -> %1.3f\n",
					                                response.getTimestamp(),
					                                skillHistory.get(response).getOutcome(),
					                                skillHistory.getSkillLevel(response)
					));
				}
			}
		}
	}
}

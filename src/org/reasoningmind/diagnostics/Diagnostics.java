/**
 * The root class for the Diagnostics project
 */
package org.reasoningmind.diagnostics;

import net.sf.clipsrules.jni.Environment;
import net.sf.clipsrules.jni.MultifieldValue;
import net.sf.clipsrules.jni.PrimitiveValue;
import net.sf.clipsrules.jni.Router;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * This is the main class of the diagnostics application.
 * It provides the user interface and communication with the CLIPS system.
 */
public class Diagnostics extends JFrame implements ActionListener
{
	// Data handlers
	// Need only one of each for the life of the application
	private final JFileChooser fileChooser = new JFileChooser();

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
	private Router router;

	Environment getClips() {
		return clips;
	}

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
		if (!loadResources()) {
			return;
		}

		// **************************
		// Setting up the main window
		// **************************
		this.setTitle(resources.getString("MainWindow-Title"));
		this.getContentPane().setLayout(new BorderLayout());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// The main panel of the layout
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		this.add(mainPanel, BorderLayout.CENTER);

		// The "command line interface"
		this.addClipsPanel(mainPanel);

		// The input file selection area
		this.addFileSelectionPanel();

		// The facts filter area
//		this.addFilterPanel();

		this.addStudentSelectionPanel();
		studentSelector.setModel(new DefaultComboBoxModel<>(new String[]{""}));

		// Aligning the layout properly
		this.pack();
		this.setLocation(300, 200);
		// Showing the main window
		this.setVisible(true);

		resetData();

		clips = new Environment();

		router = new Router()
		{
			@Override
			public int getPriority() {
				return 40;
			}

			@Override
			public String getName() {
				return "Java";
			}

			@Override
			public boolean query(String routerName) {
				return routerName != null
				       && (routerName.equals("Java") || routerName.equals("t") || routerName.equals("wtrace")
				           || routerName.equals("wprompt") || routerName.equals("wdisplay")
				           || routerName.equals("wdialog"));
			}

			@Override
			public void print(String routerName, String printString) {
				outputArea.append(printString);
			}

			@Override
			public int getchar(String routerName) {
				return 0;
			}

			@Override
			public int ungetchar(String routerName, int theChar) {
				return 0;
			}

			@Override
			public boolean exit(int exitCode) {
				return false;
			}
		};
		clips.addRouter(router);

		clips.loadFromResource("/org/reasoningmind/diagnostics/resources/defs.clp");
		clips.loadFromResource("/org/reasoningmind/diagnostics/resources/skills.clp");
		clips.loadFromResource("/org/reasoningmind/diagnostics/resources/rules.clp");

		outputArea.append(Environment.getVersion() + "\n\n" + resources.getString("CLIPS-Prompt"));

		System.setOut(new PrintStream(new MyOutputStream()));
	}

	private void resetData() { // NOTE: data loading
		dataManager.fetchStudentsAndQuestions(this);

		studentSelector.setModel(new DefaultComboBoxModel<>(new Vector<>(dataManager.getStudentIDs())));
		studentSelector.insertItemAt("", 0);
		studentSelector.setSelectedIndex(0);
		skillSelector.setModel(new DefaultComboBoxModel<>(new String[]{""}));
	}

	private boolean loadResources() {
		try {
			resources = ResourceBundle.getBundle(
					"org.reasoningmind.diagnostics.resources.Diagnostics",
					Locale.getDefault()
			);
		}
		catch (MissingResourceException mre) {
			mre.printStackTrace();
			return false;
		}

		return true;
	}

	private void addBorder(JPanel panel, String resourceID) {
		panel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				resources.getString(resourceID)
		));
	}

	private void addCheerfulIcon(JPanel parent) {
		JLabel iconLabel = new JLabel();
		iconLabel.setIcon(
				new ImageIcon(
						getClass().getClassLoader()
						          .getResource("org/reasoningmind/diagnostics/resources/data_input_label.png")
				)
		);

		parent.add(iconLabel, BorderLayout.SOUTH);
	}

	// Parts of user interface
	private JTextField factFilterTF;

	private void addFilterPanel() {
		JPanel filterPanel = new JPanel(new BorderLayout());
		addBorder(filterPanel, "MainWindowFilterAreaTitle");

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

	private JTextField filePathTF;

	private void addFileSelectionPanel() {
		JPanel fileSelectionPanel = new JPanel(new BorderLayout());
		addBorder(fileSelectionPanel, "MainWindow-FileSelectionArea-Title");

		// File path text field
		filePathTF = new JTextField("");
//		filePathTF.addActionListener(this);
		filePathTF.setEditable(false);
		fileSelectionPanel.add(filePathTF, BorderLayout.CENTER);

		// File chooser button, opens the file chooser
		JButton browseButton = new JButton("Browse");
		browseButton.setActionCommand(resources.getString("BrowseButton-Command"));
		browseButton.addActionListener(this);
		fileSelectionPanel.add(browseButton, BorderLayout.WEST);

		this.add(fileSelectionPanel, BorderLayout.NORTH);
	}

	private JTextField clipsCommandTF;
	private JTextArea outputArea;

	private void addClipsPanel(JPanel parent) {
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
		addBorder(promptPanel, "MainWindow-CLIPSArea-Title");

		clipsCommandTF = new JTextField("");
		clipsCommandTF.setPreferredSize(new Dimension(outputArea.getWidth(), 24));
		clipsCommandTF.setFont(Font.decode("Consolas-Plain-12"));
		clipsCommandTF.addActionListener(this);
		promptPanel.add(clipsCommandTF, BorderLayout.CENTER);

		clipsPanel.add(promptPanel, BorderLayout.SOUTH);

		parent.add(clipsPanel, BorderLayout.CENTER);
	}

	private JComboBox<String> studentSelector;
	private JComboBox<String> skillSelector;
	private JCheckBox showDetailsCheckbox;

	private void addStudentSelectionPanel() {
		JPanel studentSelectionPanel = new JPanel(new BorderLayout());
		addBorder(studentSelectionPanel, "MainWindowStudentSelectionAreaTitle");

		JPanel selectorsPanel = new JPanel(new GridLayout(4, 1));

		studentSelector = new JComboBox<>();
		studentSelector.addActionListener(this);
		studentSelector.setEditable(false);
		studentSelector.setPreferredSize(new Dimension(200, 24));
		selectorsPanel.add(studentSelector);

		skillSelector = new JComboBox<>();
		skillSelector.addActionListener(this);
		skillSelector.setEditable(false);
		skillSelector.setModel(new DefaultComboBoxModel<>(new String[]{""}));
		selectorsPanel.add(skillSelector);

		showDetailsCheckbox = new JCheckBox("Show details", false);
		showDetailsCheckbox.addActionListener(this);
		selectorsPanel.add(showDetailsCheckbox);

		JButton runButton = new JButton(resources.getString("MainWindowRunButtonLabel"));
		runButton.setActionCommand("RunClips");
		runButton.addActionListener(this);
		selectorsPanel.add(runButton);

		studentSelectionPanel.add(selectorsPanel, BorderLayout.NORTH);
		addCheerfulIcon(studentSelectionPanel);

		this.add(studentSelectionPanel, BorderLayout.WEST);
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
		outputArea.append(expression + "\n\n");

		// Escaping the back slashes in the expression before passing the string to the environment
		// (each '\' should be changed to "\\"),
		// then running CLIPS and getting the result
		PrimitiveValue res = clips.eval(expression.replace("\\", "\\\\"));

		outputArea.append(printPrimitiveValue(res));

		return res;
	}

	private String printPrimitiveValue(PrimitiveValue pv) {
		String res = "";

		// Checking if the primitive value is a multi-filed value
		if (pv.getClass().getSimpleName().equals("MultifieldValue")) {
			MultifieldValue mv = (MultifieldValue) pv;
			for (int i = 0; i < mv.size(); i++) {
				res += mv.get(i).toString() + "\n";
			}

			res += "\n" + resources.getString("CLIPS-Prompt");
		}
		// ... or a void value
		else if (pv.getClass().getSimpleName().equals("VoidValue")) {
			res += "\n" + resources.getString("CLIPS-Prompt");
		}
		// ... or a single value
		else {
			res += pv.toString() + "\n\n" + resources.getString("CLIPS-Prompt");
		}

		return res;
	}


	// Concurrency-related fields
	private boolean clipsIsRunning = false;

	/**
	 * Invokes the <code>(run)</code> command in the CLIPS environment in a separate thread.
	 */
	private void runDiagnostics() {
		Runnable runThread =
				() -> {
					clips.run();

					SwingUtilities.invokeLater(
							() -> {
								clips.reset();
								outputArea.append(resources.getString("CLIPS-Prompt"));
								clipsIsRunning = false;

								filePathTF.setEnabled(true);
								clipsCommandTF.setEnabled(true);
								setCursor(Cursor.getDefaultCursor());
							}
					);
				};

		outputArea.setText("\nDiagnostics for " + studentSelector.getSelectedItem() + "\n\n");
		clips.assertString("(diagnose \"" + studentSelector.getSelectedItem() + "\")");
		clipsIsRunning = true;

		filePathTF.setEnabled(false);
		clipsCommandTF.setEnabled(false);

		Thread executionThread = new Thread(runThread);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		executionThread.start();
	}


	private boolean studentStats = false;

	/**
	 * This method is inherited from the ActionListener interface, and is invoked whenever a registered event occurs.
	 *
	 * @param e
	 * 		the registered event
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (clipsIsRunning) {
			return;
		}

		if (e.getActionCommand().equals(resources.getString("CLIPS-LoadingCommand"))) {

			if (browseAndLoad(resources.getString("CLIPS-FileDescription"),
			                  resources.getString("CLIPS-FileExt"))
			    == JFileChooser.APPROVE_OPTION) {

				File file = fileChooser.getSelectedFile();
				filePathTF.setText(file.getPath());

				eval("(load \"" + file.getPath() + "\")");
				clips.reset();
			}
		}
		else if (e.getActionCommand().equals(resources.getString("CSV-LoadingCommand"))) {

			if (browseAndLoad(resources.getString("CSV-FileDescription"),
			                  resources.getString("CSV-FileExt"))
			    == JFileChooser.APPROVE_OPTION) {

				File file = fileChooser.getSelectedFile();
				filePathTF.setText(file.getPath());

				dataManager.loadCSV(file);
				resetData();
			}
		}
		else if (e.getActionCommand().equals("ShowFacts")
		         || e.getSource() == factFilterTF) {
			showFacts();
		}
		else if (e.getSource() == clipsCommandTF) {
			executeClipsCommand();
		}
		else if (e.getSource() == studentSelector
		         || e.getSource() == skillSelector
		         || e.getSource() == showDetailsCheckbox) {
			displayStudentData(e);
		}
		else if (e.getActionCommand().equals("RunClips")) {
			runDiagnostics();
		}
	}

	private void displayStudentData(ActionEvent e) {
		String studentID = (String) studentSelector.getSelectedItem();
		if (studentID.equals("")) {
			return;
		}
		String selectedSkill = (String) skillSelector.getSelectedItem();

		studentStats = true;

		outputArea.setText("\n");
		outputArea.append(studentID + "\n\n");

		StudentHistory history = dataManager.getHistory(studentID);

		Set<String> skills;
		if (selectedSkill.equals("") || e.getSource() == studentSelector) {
			skills = history.keySet();
		}
		else {
			skills = new HashSet<>();
			skills.add(selectedSkill);
		}

		if (e.getSource() == studentSelector) {
			skillSelector.setModel(new DefaultComboBoxModel<>(new Vector<String>(new ConcurrentSkipListSet<>(skills))));
			skillSelector.insertItemAt("", 0);
			skillSelector.setSelectedIndex(0);
		}

		for (String skill : skills) {
			StudentHistory.SkillHistory skillHistory = history.get(skill);
			outputArea.append(String.format("  %s:\n",
			                                skill.toUpperCase()
			));

			Set<StudentHistory.RecordKey> responses = skillHistory.descendingKeySet();

			final String[] OUTCOME = {"F    ", "pass ", "F*   "};
			boolean showDetails = this.showDetailsCheckbox.isSelected();

			for (StudentHistory.RecordKey
					response : responses) {

				outputArea.append(String.format(
						(showDetails ?"          " + response.getQuestionID() + "\n" :"") +
						"          %s -> %1.3f    %s\n",
						OUTCOME[skillHistory.get(response).getOutcome()],
						skillHistory.getSkillLevel(response),
						showDetails ?skillHistory.get(response).printOtherSkills() :""
				));
			}
			outputArea.append(String.format("                      TREND=%1.3f\n" + (showDetails ?"\n\n" :""),
			                                skillHistory.trend()
			));

			outputArea.append("\n");
		}

		outputArea.setCaretPosition(0);
	}

	private void executeClipsCommand() {
		if (studentStats) {
			outputArea.append("\n" + resources.getString("CLIPS-Prompt"));
			studentStats = false;
		}

		eval(clipsCommandTF.getText());
		outputArea.setCaretPosition(outputArea.getText().length());
		clipsCommandTF.setText("");
		clips.printBanner();
	}

	private void showFacts() {
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

	private void loadCLPFromPath() {
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

	private int browseAndLoad(String fileDescription, String fileExt) {
		// The file filter used for browsing the system for input file
		FileNameExtensionFilter fnef = new FileNameExtensionFilter(fileDescription, fileExt);
		fileChooser.setFileFilter(fnef);

		return fileChooser.showOpenDialog(this);
	}

	class MyOutputStream extends OutputStream
	{
		@Override
		public void write(int b) throws IOException {
			outputArea.append("" + (char) b);
		}
	}
}
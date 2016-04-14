/**
 * The root class for the Diagnostics project
 */
package org.reasoningmind.diagnostics;

import net.sf.clipsrules.jni.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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
	private static ResourceBundle resources;

	ResourceBundle getResources() {
		return resources;
	}

	// CLIPS execution-related fields
	/**
	 * @apiNote Make sure to specify {@code -Djava.library.path="<path to CLIPSJNI directory>"}.
	 * This is where the CLIPSJNI.dll is.
	 * In IntelliJ Idea, the option is specified under VM Options in "Run/Debug Configurations"
	 */
	private Environment clips;

	Environment getClips() {
		return clips;
	}

	private final StudentDataManager dataManager = new StudentDataManager();

	// ============
	// Constructors
	// ============

	/**
	 * The default constructor that sets up the main window of the application and creates a CLIPS environment with the
	 * diagnostics' "defrules" and "deftemplates" loaded from the resources.
	 */
	private Diagnostics() {
		// Accessing the resources file
		if (!loadResources()) {
			return;
		}

		/******************************/
		/* Setting up the main window */
		/******************************/
		this.setTitle(resources.getString("MainWindow-Title"));
		this.getContentPane().setLayout(new BorderLayout());
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		// The main panel of the layout
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		this.add(mainPanel, BorderLayout.CENTER);

		// The "command line interface"
		this.addClipsPanel(mainPanel);

		// The input file selection area
		this.addFileSelectionPanel();

		this.addStudentSelectionPanel();
		studentSelector.setModel(new DefaultComboBoxModel<>(new String[]{""}));

		// Setting up the final layout and location
		this.pack();
		this.setLocation(300, 200);
		this.setVisible(true);


		/********************************************/
		/* Loading the data from the local database */
		/********************************************/
		resetData();


		/*************************************/
		/* Starting up the CLIPS environment */
		/*************************************/
		clips = new Environment();

		// This is needed for getting CLIPS output to the main window
		clips.addRouter(new OutputRouter());

		clips.loadFromResource(resources.getString("CLIPS-Definitions"));
		clips.loadFromResource(resources.getString("CLIPS-SkillsData"));
		clips.loadFromResource(resources.getString("CLIPS-Rules"));

		printToOutput(Environment.getVersion(), STYLE_INFO);
		printToOutput("\n\n" + resources.getString("CLIPS-Prompt"), STYLE_DEFAULT);
	}

	private void printToOutput(String printString, String style) {
		try {
			output.insertString(output.getLength(), printString,
			                    output.getStyle(style));// append(printString);
		}
		catch (BadLocationException ble) {
			System.out.println("Couldn't print to outputArea");
		}
	}

	private void resetData() {
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

	private static void addBorder(JPanel panel, String resourceID) {
		panel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				resources.getString(resourceID)
		));
	}

//	private void addCheerfulIcon(JPanel parent) {
//		JLabel iconLabel = new JLabel();
//		URL imageURL = getClass().getClassLoader()
//		                         .getResource("org/reasoningmind/diagnostics/resources/data_input_label.png");
//
//		if (imageURL != null) {
//			iconLabel.setIcon(new ImageIcon(imageURL));
//
//			parent.add(iconLabel, BorderLayout.SOUTH);
//		}
//	}

	// Parts of user interface
//	private JTextField factFilterTF;

//	private void addFilterPanel() {
//		JPanel filterPanel = new JPanel(new BorderLayout());
//		addBorder(filterPanel, "MainWindowFilterAreaTitle");
//
//		// Filter definition text field
//		factFilterTF = new JTextField("");
//		factFilterTF.addActionListener(this);
//		filterPanel.add(factFilterTF, BorderLayout.CENTER);
//
//		// Filter run button
//		JButton showFacts = new JButton("Show Facts");
//		showFacts.setActionCommand("ShowFacts");
//		showFacts.addActionListener(this);
//		filterPanel.add(showFacts, BorderLayout.EAST);
//
//		this.add(filterPanel, BorderLayout.SOUTH);
//	}

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
	private JTextPane outputArea;
	private StyledDocument output;

	private void addClipsPanel(JPanel parent) {
		JPanel clipsPanel = new JPanel(new BorderLayout());

		// The log text area

		StyleContext styleContext = new StyleContext();

		output = new DefaultStyledDocument(styleContext);
		outputArea = new JTextPane(output);

		// note: this line should remain after the line "new outputArea = JTextPane(output);"
		// otherwise, the FontFamily = "monospaced" in defineOutputStyles() doesn't appear to work
		defineOutputStyles(styleContext);

		outputArea.setPreferredSize(new Dimension(500, 400));
		outputArea.setEditable(false);
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

	private static final String STYLE_DEFAULT = "default";
	private static final String STYLE_BOLD = "bold";
	private static final String STYLE_NICE = "nice";
	private static final String STYLE_INFO = "info";
	private static final String STYLE_WARNING = "warning";
	private static final String STYLE_ERROR = "error";
	private static final String MY_ORANGE = "0xD88844";
	private static final String MY_GREEN = "0x007F00";

	private void defineOutputStyles(StyleContext styleContext) {
		final Style defaultStyle = styleContext.addStyle(STYLE_DEFAULT, null);
		defaultStyle.addAttribute(StyleConstants.FontFamily, "Courier New");
		defaultStyle.addAttribute(StyleConstants.FontSize, 12);

		final Style boldStyle = styleContext.addStyle(STYLE_BOLD, defaultStyle);
		boldStyle.addAttribute(StyleConstants.Italic, true);
		boldStyle.addAttribute(StyleConstants.Bold, true);

		final Style errorStyle = styleContext.addStyle(STYLE_ERROR, boldStyle);
		errorStyle.addAttribute(StyleConstants.Foreground, Color.RED);

		final Style warningStyle = styleContext.addStyle(STYLE_WARNING, boldStyle);
		warningStyle.addAttribute(StyleConstants.Foreground, Color.decode(MY_ORANGE));

		final Style niceStyle = styleContext.addStyle(STYLE_NICE, boldStyle);
		niceStyle.addAttribute(StyleConstants.Foreground, Color.decode(MY_GREEN));

		final Style infoStyle = styleContext.addStyle(STYLE_INFO, boldStyle);
//		infoStyle.addAttribute(StyleConstants.Italic, true);
		infoStyle.addAttribute(StyleConstants.Foreground, Color.BLUE);
	}

	private JComboBox<String> studentSelector;
	private JComboBox<String> skillSelector;
	private JCheckBox showDetailsCheckbox;

	private void addStudentSelectionPanel() {
		JPanel studentSelectionPanel = new JPanel(new BorderLayout());
		addBorder(studentSelectionPanel, "MainWindow-StudentSelectionArea-Title");

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

		JButton runButton = new JButton(resources.getString("MainWindow-RunDiagnosticsButton-Label"));
		runButton.setActionCommand("RunClips");
		runButton.addActionListener(this);
		selectorsPanel.add(runButton);

		studentSelectionPanel.add(selectorsPanel, BorderLayout.NORTH);
//		addCheerfulIcon(studentSelectionPanel);

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
	 * This is a convenience method. It passes the {@code expression} onto the
	 * {@link net.sf.clipsrules.jni.Environment#eval}
	 * method and displays
	 * the result in the {@link #outputArea}.
	 *
	 * @param expression
	 * 		the CLIPS expression to pass onto the <code><b>Environment.eval</b>(String)</code> method
	 *
	 * @return the result of the {@code Environment.eval(String)} method
	 *
	 * @see net.sf.clipsrules.jni.Environment#eval(String)
	 */
	private PrimitiveValue eval(String expression) {
		printToOutput(expression + "\n\n", STYLE_DEFAULT);

		// Escaping the back slashes in the expression before passing the string to the environment
		// (each '\' should be changed to "\\"),
		// then running CLIPS and getting the result
		PrimitiveValue res = clips.eval(expression.replace("\\", "\\\\"));

		printToOutput(printPrimitiveValue(res), STYLE_DEFAULT);

		return res;
	}

	private String printPrimitiveValue(PrimitiveValue pv) {
		String res = "";

		// Checking if the primitive value is a multi-filed value
		if (pv instanceof MultifieldValue) {
			MultifieldValue mv = (MultifieldValue) pv;
			for (int i = 0; i < mv.size(); i++) {
				res += mv.get(i).toString() + "\n";
			}

			res += "\n" + resources.getString("CLIPS-Prompt");
		}
		// ... or a void value
		else if (pv instanceof VoidValue) {
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
	 * Invokes the {@code (run)} command in the CLIPS environment in a separate thread.
	 */
	private void runDiagnostics() {
		Runnable runThread =
				() -> {
					clips.run();

					SwingUtilities.invokeLater(
							() -> {
								printToOutput(resources.getString("CLIPS-Prompt"), STYLE_DEFAULT);
								clipsIsRunning = false;

								filePathTF.setEnabled(true);
								clipsCommandTF.setEnabled(true);
								setCursor(Cursor.getDefaultCursor());
							}
					);
				};

		outputArea.setText("\nDiagnostics for " + studentSelector.getSelectedItem() + "\n\n");
		initializeClips();
		clipsIsRunning = true;

		filePathTF.setEnabled(false);
		clipsCommandTF.setEnabled(false);

		Thread executionThread = new Thread(runThread);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		executionThread.start();
	}

	private void initializeClips() {
		clips.reset();

		String student = (String) studentSelector.getSelectedItem();
		StudentHistory history = dataManager.getHistory(student);
		int lastLesson = dataManager.getLastLesson(student);

		clips.assertString(String.format("(diagnose \"%s\")", student.replace("\\", "\\\\")
		                                                             .replace("\"", "\\\"")));
		clips.assertString(String.format("(student (ID \"%s\") (lesson %d))",
		                                 student.replace("\\", "\\\\")
		                                        .replace("\"", "\\\""),
		                                 lastLesson
		                   )
		);

		Set<String> skills = history.keySet();

		for (String skill : skills) {
			StudentHistory.SkillHistory skillHistory = history.get(skill);
			double level = skillHistory.getSkillLevel(), trend = skillHistory.trend();

			clips.assertString(String.format("(student-skill (student-ID \"%s\") (skill-ID \"%s\") " +
			                                 "(level %s) (trend %s) (count %d))",
			                                 student.replace("\\", "\\\\")
			                                        .replace("\"", "\\\""),
			                                 skill.replace("\\", "\\\\")
			                                      .replace("\"", "\\\""),
			                                 level < 0.6 ?"F"
			                                             :level < 0.7 ?"D"
			                                                          :level < 0.8 ?"C"
			                                                                       :level < 0.9 ?"B"
			                                                                                    :"A",
			                                 trend < -0.05 ?"DOWN"
			                                               :trend <= 0.05 ?"EVEN"
			                                                              :"UP",
			                                 skillHistory.recentPureOutcomesSize()
			                   )
			);
		}
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
//		else if (e.getActionCommand().equals("ShowFacts")
//		         || e.getSource() == factFilterTF) {
//			showFacts();
//		}
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

		int lastLesson = dataManager.getLastLesson(studentID);
		StudentHistory history = dataManager.getHistory(studentID);

		outputArea.setText("\n");
		printToOutput(studentID + "\nLast lesson: " + lastLesson + "\n\n", STYLE_DEFAULT);

		Set<String> skills;
		if (selectedSkill.equals("") || e.getSource() == studentSelector) {
			skills = history.keySet();
		}
		else {
			skills = new HashSet<>();
			skills.add(selectedSkill);
		}

		if (e.getSource() == studentSelector) {
			skillSelector.setModel(new DefaultComboBoxModel<>(new Vector<>(new ConcurrentSkipListSet<>(skills))));
			skillSelector.insertItemAt("", 0);
			skillSelector.setSelectedIndex(0);
		}

		for (String skill : skills) {
			StudentHistory.SkillHistory skillHistory = history.get(skill);
			printToOutput(String.format("  %s:\n",
			                            skill.toUpperCase()),
			              STYLE_INFO);

			Set<StudentHistory.RecordKey> responses = skillHistory.descendingKeySet();

			final String[] OUTCOME = {"fail ", "pass ", "fail*"};
			boolean showDetails = this.showDetailsCheckbox.isSelected();

			for (StudentHistory.RecordKey
					response : responses) {
				int outcome = skillHistory.get(response).getOutcome();

				if (showDetails) {

					printToOutput("          " + response.getQuestionID() + "\n", STYLE_DEFAULT);
					printToOutput(skillHistory.get(response).numberOfOtherSkills() > 0
					              ?"                            additional skills:\n"
					              :"", STYLE_INFO);
				}

				printToOutput(String.format("          %s", OUTCOME[outcome]),
				              outcome == StudentHistory.SkillHistory.PASS
				              ?STYLE_NICE
				              :outcome == StudentHistory.SkillHistory.FAIL_A_SINGLE_SKILL
				               ?STYLE_ERROR
				               :STYLE_WARNING);

				printToOutput(String.format(" -> %1.3f    %s\n",
				                            skillHistory.getSkillLevel(response),
				                            showDetails ?skillHistory.get(response).printOtherSkills() :""),
				              STYLE_DEFAULT);
			}
			printToOutput(String.format("           TREND =% 1.3f\n" + (showDetails ?"\n\n" :""),
			                            skillHistory.trend()),
			              STYLE_DEFAULT);

			printToOutput("\n", STYLE_DEFAULT);
		}

		outputArea.setCaretPosition(0);
	}

	private void executeClipsCommand() {
		if (studentStats) {
			printToOutput(Environment.getVersion(), STYLE_INFO);
			printToOutput("\n\n" + resources.getString("CLIPS-Prompt"), STYLE_DEFAULT);

			studentStats = false;
		}

		eval(clipsCommandTF.getText());
		outputArea.setCaretPosition(output.getLength());
		clipsCommandTF.setText("");
		clips.printBanner();
	}

//	private void showFacts() {
//		MultifieldValue mv = (MultifieldValue) clips.eval("(find-fact " + factFilterTF.getText() + ")");
//		if (mv.size() == 0) {
//			JOptionPane.showMessageDialog(this,
//			                              "There are no facts that match the query:\n" + factFilterTF.getText(),
//			                              "Facts not found",
//			                              JOptionPane.INFORMATION_MESSAGE);
//			return;
//		}
//
//		new FactsFrame(this, factFilterTF.getText());
//	}

//	private void loadCLPFromPath() {
//		File file = new File(filePathTF.getText());
//
//		if (file.exists()) {
//			eval("(load \"" + filePathTF.getText() + "\")");
//			clips.reset();
//		}
//		else {
//			JOptionPane.showMessageDialog(this,
//			                              "Specified file doesn't exist.",
//			                              "Error",
//			                              JOptionPane.ERROR_MESSAGE);
//		}
//	}

	private int browseAndLoad(String fileDescription, String fileExt) {
		// The file filter used for browsing the system for input file
		FileNameExtensionFilter fnef = new FileNameExtensionFilter(fileDescription, fileExt);
		fileChooser.setFileFilter(fnef);

		return fileChooser.showOpenDialog(this);
	}

//	private class MyOutputStream extends OutputStream
//	{
//		@Override
//		public void write(int b) throws IOException {
//			printToOutput("" + (char) b, STYLE_INFO);
//		}
//	}

	/**
	 * An implementation of the Router interface that is needed for routing CLIPS output to this application.
	 */
	private class OutputRouter implements Router
	{
		private static final String JAVA_REG = "java-reg";
		private static final String JAVA_BOLD = "java-bold";
		private static final String JAVA_NICE = "java-nice";
		private static final String JAVA_WARNING = "java-warn";
		private static final String JAVA_ERROR = "java-error";
		private static final String JAVA_INFO = "java-info";
		private static final String WPROMPT = "wprompt";
		private static final String WTRACE = "wtrace";
		private static final String WDISPLAY = "wdisplay";
		private static final String WDIALOG = "wdialog";
		private static final String T = "t";

		@Override
		public int getPriority() {
			return 40;
		}

		@Override
		public String getName() {
			return JAVA_REG;
		}

		@Override
		public boolean query(String routerName) {
			return routerName != null
			       && (routerName.equals(JAVA_ERROR) || routerName.equals(JAVA_WARNING)
			           || routerName.equals(JAVA_INFO) || routerName.equals(JAVA_NICE)
			           || routerName.equals(JAVA_BOLD) || routerName.equals(JAVA_REG)
			           || routerName.equals(T) || routerName.equals(WTRACE)
			           || routerName.equals(WPROMPT) || routerName.equals(WDISPLAY)
			           || routerName.equals(WDIALOG));
		}

		@Override
		public void print(String routerName, String printString) {
			switch (routerName) {
			case JAVA_ERROR:
				printToOutput(printString, STYLE_ERROR);
				break;
			case JAVA_WARNING:
			case WTRACE:
				printToOutput(printString, STYLE_WARNING);
				break;
			case JAVA_INFO:
			case WDISPLAY:
			case WPROMPT:
				printToOutput(printString, STYLE_INFO);
				break;
			case JAVA_NICE:
				printToOutput(printString, STYLE_NICE);
				break;
			case JAVA_BOLD:
				printToOutput(printString, STYLE_BOLD);
				break;
			default:
				printToOutput(printString, STYLE_DEFAULT);
				break;
			}
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
	}
}
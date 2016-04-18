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
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * This is the main class of the diagnostics application.
 * It provides the user interface and communication with the CLIPS system.
 */
public class Diagnostics extends JFrame implements ActionListener
{
	// Different static Strings, mainly UI-related
	private static ResourceBundle resources;

	/**
	 * @apiNote Make sure to specify {@code -Djava.library.path="<path to CLIPSJNI directory>"}.
	 * This is where the CLIPSJNI.dll is.
	 * In IntelliJ Idea, the option is specified under VM Options in "Run/Debug Configurations"
	 */
	private static Environment clips;

	private static OutputRouter router;


/*******************/
/* The constructor */
/*******************/

	/**
	 * The default constructor that sets up the main window of the application and creates a CLIPS environment with the
	 * diagnostics' "defrules" and "deftemplates" loaded from the resources.
	 */
	private Diagnostics() {

		// Accessing the resources file
		if (!loadResources()) {
			JOptionPane.showMessageDialog(this,
			                              "Unable to load the resources.",
			                              "Error",
			                              JOptionPane.ERROR_MESSAGE);
			return;
		}

	/*############################*/
	/* Setting up the main window */
	/*############################*/

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

		// The student data selection area
		this.addStudentSelectionPanel();

		// Setting up the final layout and location
		this.pack();
		this.setLocation(300, 200);
		this.setVisible(true);


	/*##########################################*/
	/* Loading the data from the local database */
	/*##########################################*/

		refreshStudentData();


	/*###################################*/
	/* Starting up the CLIPS environment */
	/*###################################*/

		clips = new Environment();

		// This is needed for getting CLIPS routerOutput to the main window
		router = new OutputRouter(output);
		clips.addRouter(router);

		clips.loadFromResource(resources.getString("CLIPS-Definitions"));
		clips.loadFromResource(resources.getString("CLIPS-SkillsData"));
		clips.loadFromResource(resources.getString("CLIPS-Rules"));


	/*################*/
	/* Initial output */
	/*################*/

		printToOutput(Environment.getVersion(), output, STYLE_INFO);
		printToOutput("\n\n" + resources.getString("CLIPS-Prompt"), output, STYLE_DEFAULT);
	}

	Environment getClips() {
		return clips;
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

	ResourceBundle getResources() {
		return resources;
	}

	OutputRouter getRouter() {
		return router;
	}


/*********************************/
/* Methods for setting up the UI */
/*********************************/

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

	//	private JTextField clipsCommandTF;
	private JTextPane outputArea;
	private StyledDocument output;

	private void addClipsPanel(JPanel parent) {
		JPanel clipsPanel = new JPanel(new BorderLayout());

		// The log text area

		StyleContext styleContext = new StyleContext();

		output = new DefaultStyledDocument(styleContext);
		outputArea = new JTextPane(output);

		// note: this line should remain after the line "new outputArea = JTextPane(routerOutput);"
		// otherwise, the FontFamily = "monospaced" in defineOutputStyles() doesn't appear to work
		defineOutputStyles(styleContext);

		outputArea.setPreferredSize(new Dimension(500, 400));
		outputArea.setEditable(false);
		outputArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		outputArea.setAutoscrolls(true);
		JScrollPane outputScrollPane = new JScrollPane(outputArea);
		clipsPanel.add(outputScrollPane, BorderLayout.CENTER);

		parent.add(clipsPanel, BorderLayout.CENTER);
	}

	static final String STYLE_DEFAULT = "default";
	private static final String STYLE_BOLD = "bold";
	private static final String STYLE_NICE = "nice";
	private static final String STYLE_INFO = "info";
	private static final String STYLE_WARNING = "warning";
	private static final String STYLE_ERROR = "error";
	private static final String MY_ORANGE = "0xD88844";
	private static final String MY_GREEN = "0x007F00";

	static void defineOutputStyles(StyleContext styleContext) {
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
		infoStyle.addAttribute(StyleConstants.Foreground, Color.BLUE);
	}

	static void printToOutput(String printString, StyledDocument output, String style) {
		try {
			output.insertString(output.getLength(), printString, output.getStyle(style));
		}
		catch (BadLocationException ble) {
			System.out.println("Couldn't print to the output area.");
		}
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
		studentSelector.setModel(new DefaultComboBoxModel<>(new String[]{""}));
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
		addCheerfulIcon(studentSelectionPanel);

		this.add(studentSelectionPanel, BorderLayout.WEST);
	}

	private void addCheerfulIcon(JPanel parent) {
		JLabel iconLabel = new JLabel();
		URL imageURL = getClass().getClassLoader()
		                         .getResource("org/reasoningmind/diagnostics/resources/data_input_label.png");

		if (imageURL != null) {
			iconLabel.setIcon(new ImageIcon(imageURL));

			parent.add(iconLabel, BorderLayout.SOUTH);
		}
	}

	static void addBorder(JPanel panel, String resourceID) {
		panel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				resources.getString(resourceID)
		));
	}


/*********************************/
/*             Main              */
/*********************************/

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


/****************************************/
/* Methods for communicating with CLIPS */
/****************************************/

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
		printToOutput(expression + "\n\n", output, STYLE_DEFAULT);

		// Escaping the back slashes in the expression before passing the string to the environment
		// (each '\' should be changed to "\\"),
		// then running CLIPS and getting the result
		PrimitiveValue res = clips.eval(expression.replace("\\", "\\\\"));

		printToOutput(printPrimitiveValue(res), output, STYLE_DEFAULT);

		return res;
	}

	static String printPrimitiveValue(PrimitiveValue pv) {
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
	 * Initializes CLIPS with student data and invokes the CLIPS
	 * {@code (run)} command in a separate thread.
	 */
	private void runDiagnostics() {
		Runnable runThread =
				() -> {
					initializeClips();
					clips.run();

					SwingUtilities.invokeLater(
							() -> {
								clipsIsRunning = false;

								filePathTF.setEnabled(true);
								studentSelector.setEnabled(true);
								skillSelector.setEnabled(true);
								showDetailsCheckbox.setEnabled(true);
								setCursor(Cursor.getDefaultCursor());

								router.setOutput(output);
							}
					);
				};

		clipsIsRunning = true;

		filePathTF.setEnabled(false);
		studentSelector.setEnabled(false);
		skillSelector.setEnabled(false);
		showDetailsCheckbox.setEnabled(false);

		Thread executionThread = new Thread(runThread);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		new OutputFrame((String) studentSelector.getSelectedItem(), this);
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


/******************/
/* Data retrieval */
/******************/

	private final StudentDataManager dataManager = new StudentDataManager();

	private void refreshStudentData() {
		dataManager.fetchStudentsAndQuestions(this);

		studentSelector.setModel(new DefaultComboBoxModel<>(new Vector<>(dataManager.getStudentIDs())));
		studentSelector.insertItemAt("", 0);
		studentSelector.setSelectedIndex(0);

		skillSelector.setModel(new DefaultComboBoxModel<>(new String[]{""}));
	}


/***********************/
/* UI listener actions */
/***********************/

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

			if (selectFile(resources.getString("CLIPS-FileDescription"),
			               resources.getString("CLIPS-FileExt"))
			    == JFileChooser.APPROVE_OPTION) {

				File file = fileChooser.getSelectedFile();
				filePathTF.setText(file.getPath());

				eval("(load \"" + file.getPath() + "\")");
				clips.reset();
			}
		}
		else if (e.getActionCommand().equals(resources.getString("CSV-LoadingCommand"))) {

			if (selectFile(resources.getString("CSV-FileDescription"),
			               resources.getString("CSV-FileExt"))
			    == JFileChooser.APPROVE_OPTION) {

				File file = fileChooser.getSelectedFile();
				filePathTF.setText(file.getPath());

				dataManager.loadCSV(file);
				refreshStudentData();
			}
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

//		studentStats = true;

		int lastLesson = dataManager.getLastLesson(studentID);
		StudentHistory history = dataManager.getHistory(studentID);

		outputArea.setText("\n");
		printToOutput(studentID + "\nLast lesson: " + lastLesson + "\n\n", output, STYLE_DEFAULT);

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
			                            skill.toUpperCase()), output,
			              STYLE_INFO);

			Set<StudentHistory.RecordKey> responses = skillHistory.descendingKeySet();

			final String[] OUTCOME = {"fail ", "pass ", "fail*"};
			boolean showDetails = this.showDetailsCheckbox.isSelected();

			for (StudentHistory.RecordKey
					response : responses) {
				int outcome = skillHistory.get(response).getOutcome();

				if (showDetails) {

					printToOutput("          " + response.getQuestionID() + "\n", output, STYLE_DEFAULT);
					printToOutput(skillHistory.get(response).numberOfOtherSkills() > 0
					              ?"                            additional skills:\n"
					              :"", output, STYLE_INFO);
				}

				printToOutput(String.format("          %s", OUTCOME[outcome]), output,
				              outcome == StudentHistory.SkillHistory.PASS
				              ?STYLE_NICE
				              :outcome == StudentHistory.SkillHistory.FAIL_A_SINGLE_SKILL
				               ?STYLE_ERROR
				               :STYLE_WARNING);

				printToOutput(String.format(" -> %1.3f    %s\n",
				                            skillHistory.getSkillLevel(response),
				                            showDetails ?skillHistory.get(response).printOtherSkills() :""),
				              output,
				              STYLE_DEFAULT);
			}
			printToOutput(String.format("           TREND =% 1.3f\n" + (showDetails ?"\n\n" :""),
			                            skillHistory.trend()), output,
			              STYLE_DEFAULT);

			printToOutput("\n", output, STYLE_DEFAULT);
		}

		outputArea.setCaretPosition(0);
	}

	// Need only one of each for the life of the application
	private final JFileChooser fileChooser = new JFileChooser();

	private int selectFile(String fileDescription, String fileExt) {
		// The file filter used for browsing the system for input file
		FileNameExtensionFilter fnef = new FileNameExtensionFilter(fileDescription, fileExt);
		fileChooser.setFileFilter(fnef);

		return fileChooser.showOpenDialog(this);
	}


/*************************/
/* Router implementation */
/*************************/

	/**
	 * An implementation of the Router interface that is needed
	 * for routing CLIPS routerOutput to this application.
	 */
	class OutputRouter implements Router
	{
		private StyledDocument routerOutput;

		OutputRouter(StyledDocument output) {
			routerOutput = output;
		}

		void setOutput(StyledDocument output) {
			routerOutput = output;
		}

		StyledDocument getOutput()
		{
			return routerOutput;
		}

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
				printToOutput(printString, routerOutput, STYLE_ERROR);
				break;
			case JAVA_WARNING:
			case WTRACE:
				printToOutput(printString, routerOutput, STYLE_WARNING);
				break;
			case JAVA_INFO:
			case WDISPLAY:
			case WPROMPT:
				printToOutput(printString, routerOutput, STYLE_INFO);
				break;
			case JAVA_NICE:
				printToOutput(printString, routerOutput, STYLE_NICE);
				break;
			case JAVA_BOLD:
				printToOutput(printString, routerOutput, STYLE_BOLD);
				break;
			default:
				printToOutput(printString, routerOutput, STYLE_DEFAULT);
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
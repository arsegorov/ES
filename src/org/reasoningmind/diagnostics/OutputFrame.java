package org.reasoningmind.diagnostics;

import net.sf.clipsrules.jni.Environment;
import net.sf.clipsrules.jni.PrimitiveValue;

import javax.swing.*;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import static org.reasoningmind.diagnostics.Diagnostics.*;

/**
 * This class is used to display a selection of facts that use a common template
 */
class OutputFrame extends JFrame implements ActionListener
{
	private static Environment clips;
	private static Diagnostics.OutputRouter router;
	private String student;

	OutputFrame(String student, Diagnostics host) {
		this.student = student;
		clips = host.getClips();
		router = host.getRouter();

		ResourceBundle resources = host.getResources();
		this.setTitle(resources.getString("OutputWindow-Title") + student);
		this.setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel(new BorderLayout());
		this.add(mainPanel, BorderLayout.CENTER);

		JPanel buttonsPanel = new JPanel(new GridBagLayout());
		mainPanel.add(buttonsPanel, BorderLayout.NORTH);

		JButton refreshButton = new JButton(resources.getString("OutputWindow-RefreshButton-Label"));
		refreshButton.setActionCommand("Refresh");
		refreshButton.addActionListener(this);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(refreshButton, constraints);

		// The "command line interface"
		this.addClipsPanel(mainPanel);
		router.setOutput(output);

		this.pack();
		this.setVisible(true);
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

		// note: this line should remain after the line "new outputArea = JTextPane(routerOutput);"
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

		// Concurrency-related fields
	private boolean clipsIsRunning = false;

	@Override
	public void actionPerformed(ActionEvent e) {
		if (clipsIsRunning) {
			return;
		}

		if (e.getSource() == clipsCommandTF) {
			executeClipsCommand();
		}
		else if (e.getActionCommand().equals("Refresh")) {
			final StyledDocument currentOutput = router.getOutput();
			router.setOutput(output);

			Runnable runThread =
					() -> {
						clips.eval(String.format("(do-for-fact ((?fact output)) (eq ?fact:student-ID \"%1$s\") (modify ?fact (student-ID \"%1$s\")))",
						                         student));
						clips.run();

						SwingUtilities.invokeLater(
								() -> {
									clipsIsRunning = false;
									setCursor(Cursor.getDefaultCursor());

									clipsCommandTF.setEnabled(true);
									router.setOutput(currentOutput);
								}
						);
					};

			clipsIsRunning = true;

			outputArea.setText("");
			clipsCommandTF.setEnabled(false);

			Thread executionThread = new Thread(runThread);
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			executionThread.start();
		}
	}

	private void executeClipsCommand() {
		eval(clipsCommandTF.getText());
		outputArea.setCaretPosition(output.getLength());
		clipsCommandTF.setText("");
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
		// temporarily rerouting the output to here
		StyledDocument currentOutput = router.getOutput();
		router.setOutput(output);

		printToOutput(expression + "\n\n", output, STYLE_DEFAULT);

		// Escaping the back slashes in the expression before passing the string to the environment
		// (each '\' should be changed to "\\"),
		// then running CLIPS and getting the result
		PrimitiveValue res = clips.eval(expression.replace("\\", "\\\\"));

		printToOutput(printPrimitiveValue(res), output, STYLE_DEFAULT);

		router.setOutput(currentOutput);
		return res;
	}
}

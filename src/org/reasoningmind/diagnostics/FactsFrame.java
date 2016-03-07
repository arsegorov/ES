package org.reasoningmind.diagnostics;

import net.sf.clipsrules.jni.Environment;
import net.sf.clipsrules.jni.FactAddressValue;
import net.sf.clipsrules.jni.MultifieldValue;
import net.sf.clipsrules.jni.PrimitiveValue;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * This class is used to display a selection of facts that use a common template
 */
public class FactsFrame extends JFrame {

	Diagnostics host;

	// Text resources
	ResourceBundle resources;
	Environment clips;

	JTable factsView;

	FactsFrame(Diagnostics host, String filter) {
		this.host = host;
		resources = host.getResources();
		clips = host.getClips();

		this.setTitle(resources.getString("FactsFrameTitle") + " [ " + filter + " ]");
		this.setLayout(new BorderLayout());

		MultifieldValue mv = (MultifieldValue) clips.eval("(do-for-fact " + filter + " (fact-slot-names ?fact))");
		Vector columnHeaders = new Vector();
		for (int i = 0; i < mv.size(); i++) {
			columnHeaders.add(mv.get(i).toString());
		}

		Vector rows = new Vector();
		mv = (MultifieldValue) clips.eval("(find-all-facts " + filter + ")");

		try {
			for (int i = 0; i < mv.size(); i++) {
				Vector row = new Vector(columnHeaders.size());
				FactAddressValue fv = (FactAddressValue) mv.get(i);

				for (int j = 0; j < columnHeaders.size(); j++) {
					PrimitiveValue pv = fv.getFactSlot((String) columnHeaders.get(j));

					if (pv.getClass().getSimpleName().equals("MultifieldValue")) {
						MultifieldValue mv1 = (MultifieldValue) pv;

						if (mv1.size() > 0) {
							String res = mv1.get(0).toString();

							for (int k = 1; k < mv1.size(); k++) {
								res += (", " + mv1.get(k).toString());
							}

							row.add(res);
						}
						else {
							row.add("");
						}
					}
					else if (pv.getClass().getSimpleName().equals("VoidValue")) {
						row.add("");
					}
					else {
						row.add(pv.toString());
					}
				}

				rows.add(row);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		factsView = new JTable(rows, columnHeaders);

		JScrollPane tableScroller = new JScrollPane(factsView);

		this.add(tableScroller, BorderLayout.CENTER);
		this.pack();
		this.setVisible(true);
	}
}

package GUI;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import listeners.PhotoSourceListener;
import listeners.TextClearingListener;
import listeners.TextSourceListener;
import backend.NewSourceHandler;
import backend.SourceEntry;

//Used for creation of a reference/source citation
@SuppressWarnings("serial")
public class SourceCreator extends JFrame {

	public NewSourceHandler nsh;

	public SourceCreator(int type) {
		super.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JPanel pane = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.anchor = GridBagConstraints.LINE_START;

		JButton done = new JButton("Done");
		switch (type) {
		case SourceEntry.PHOTO:
			JLabel photographerLabel = new JLabel("Photographer:");
			pane.add(photographerLabel, constraints);
			constraints.gridx++;
			JTextField photographer = new JTextField(10);
			pane.add(photographer, constraints);
			constraints.gridx++;

			JLabel licenceLabel = new JLabel("Licence:");
			pane.add(licenceLabel, constraints);
			constraints.gridx++;
			JTextField licence = new JTextField(10);
			pane.add(licence, constraints);
			constraints.gridx++;

			PhotoSourceListener photoListener = new PhotoSourceListener(new JTextField[] {photographer, licence});
			JButton addPhoto = new JButton("Add Photo");
			addPhoto.setActionCommand("CHOOSE");
			addPhoto.addActionListener(photoListener);
			pane.add(addPhoto, constraints);
			constraints.gridx = 0;
			constraints.gridy++;
			done.addActionListener(photoListener);
			done.setActionCommand("DONE");
			break;

		case SourceEntry.SITE:
			JLabel siteLabel = new JLabel("Site:");
			pane.add(siteLabel, constraints);
			constraints.gridx++;
			JTextField site = new JTextField(10);
			pane.add(site, constraints);
			constraints.gridx++;

			JLabel retrievalDateLabel = new JLabel("Day:");
			pane.add(retrievalDateLabel, constraints);
			constraints.gridx++;
			JTextField retrievalDate = new JTextField(3);
			retrievalDate.addFocusListener(new TextClearingListener(retrievalDate));
			pane.add(retrievalDate, constraints);
			constraints.gridx++;

			JLabel retrievalMonthLabel = new JLabel("Month");
			pane.add(retrievalMonthLabel, constraints);
			constraints.gridx++;
			JTextField retrievalMonth = new JTextField(3);
			retrievalMonth.addFocusListener(new TextClearingListener(retrievalMonth));
			pane.add(retrievalMonth, constraints); 
			constraints.gridx++;

			JLabel retrievalYearLabel = new JLabel("Year:");
			pane.add(retrievalYearLabel, constraints);
			constraints.gridx++;
			JTextField retrievalYear = new JTextField(3);
			retrievalYear.addFocusListener(new TextClearingListener(retrievalYear));
			pane.add(retrievalYear, constraints); 
			constraints.gridx++;
			
			Calendar calendar = Calendar.getInstance();
			retrievalDate.setText(calendar.get(Calendar.DATE) + "");
			retrievalMonth.setText(calendar.get(Calendar.MONTH) + "");
			retrievalYear.setText(calendar.get(Calendar.YEAR) + "");
			done.addActionListener(new TextSourceListener(new JTextField[] {retrievalDate, retrievalMonth, retrievalYear, site}));
			break;
		}

		done.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				done();
			}
		});

		pane.add(done, constraints);

		super.add(pane);
		super.pack();
		super.setVisible(true);
	}

	public void done() {
		super.dispose();
	}

}

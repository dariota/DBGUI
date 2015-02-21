package GUI;

import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import backend.Arrays;
import backend.ConnectDB;
import backend.DBThreadedCreator;
import backend.ErrorHandler;

//Search dialog
public class Search extends JFrame {

	private static final long serialVersionUID = 1L;
	private ConnectDB db = null;
	private Thread t;
	private DBThreadedCreator dbCreator;
	private String errSource = "S";
	private int currentx = 0;
	private int currenty = 0;
	private Container pane;
	private JComboBox<String> type;
	private JComboBox<String> column;
	private JButton submit;
	private JTextField searchTerm;
	private JCheckBox exact;

	public Search() {
		dbCreator = new DBThreadedCreator();
		t = new Thread(dbCreator);
		t.start();
		initComponents();
		super.pack();
		super.setVisible(true);   
	}

	private void initComponents() {
		super.setTitle("Search");
		super.setIconImage(MainWindow.image);
		pane = super.getContentPane();
		pane.setLayout(new GridBagLayout());

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;

		JLabel title = new JLabel("Search Database");
		title.setFont(new Font("Tahoma", 1, 14));
		addComp(title, constraints, 0, 1);

		Font tahomaFont = new Font("Tahoma", 1, 12);

		JLabel typeLabel = new JLabel("Search in:");
		typeLabel.setFont(tahomaFont);
		addComp(typeLabel, constraints, 1, 0);
		type = new JComboBox<String>();
		type.setModel(new DefaultComboBoxModel<String>(Arrays.types()));
		type.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				controlSubmitButton();
			}
		});
		addComp(type, constraints, -currentx, 1);

		JLabel columnLabel = new JLabel("By:");
		columnLabel.setFont(tahomaFont);
		addComp(columnLabel, constraints, 1, 0);
		column = new JComboBox<String>();
		String[] cols = Arrays.columnsAsArray();
		String[] display = new String[cols.length+1];
		display[0] = "";
		for(int j = 1; j <= cols.length; j++) {
			display[j] = cols[j-1];
		}
		column.setModel(new DefaultComboBoxModel<String>(display));
		column.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				controlSubmitButton();
			}
		});
		addComp(column, constraints, -currentx, 1);
		
		JLabel searchTermLabel = new JLabel("Where:");
		searchTermLabel.setFont(tahomaFont);
		addComp(searchTermLabel, constraints, 1, 0);
		searchTerm = new JTextField();
		searchTerm.addKeyListener(new KeyListener() {			
			@Override
			public void keyPressed(KeyEvent ke) {
				if (ke.getExtendedKeyCode() == 10 || ke.getExtendedKeyCode() == 13) 
					submit();
			}

			@Override
			public void keyReleased(KeyEvent e) {}

			@Override
			public void keyTyped(KeyEvent e) {}
		});
		addComp(searchTerm, constraints, -currentx, 1);
		
		exact = new JCheckBox("Exact Match?");
		exact.setFont(tahomaFont);
		addComp(exact, constraints, 1, 0);
		
		submit = new JButton();
		submit.setEnabled(false);
		submit.setText("Search!");
		constraints.fill = GridBagConstraints.VERTICAL;
		submit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				submit();
			}
		});
		addComp(submit, constraints, 0, 0);
	}

	private void addComp(JComponent c, GridBagConstraints GBC, int x, int y) {
		GBC.gridx = currentx;
		currentx += x;
		GBC.gridy = currenty;
		currenty += y;
		pane.add(c, GBC);
	}

	private void controlSubmitButton() {
		submit.setEnabled(type.getSelectedIndex() > 0 && column.getSelectedIndex() > 0);
	}

	private void submit() {
		String searchBy = searchTerm.getText();
		try {
			db = dbCreator.getAccess();
			while (db == null) {
				Thread.sleep(200);
				db = dbCreator.getAccess();
			}
			new SearchResults((String) type.getSelectedItem(), db.search(searchBy, 
					Arrays.columnsAsArray()[column.getSelectedIndex()-1], !exact.isSelected(), 
					(String) type.getSelectedItem()));
			super.dispose();
			db.close();
		} catch (InterruptedException e) {
			ErrorHandler.error(e, errSource);
		}
	}

}
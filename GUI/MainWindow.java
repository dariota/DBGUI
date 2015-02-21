package GUI;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import backend.Arrays;
import backend.ConnectDB;
import backend.DBThreadedCreator;
import backend.Run;
import fileFilter.IncompleteFilter;

//Contains buttons to launch different parts of the application
@SuppressWarnings("serial")
public class MainWindow extends JFrame {

	private JButton continueOld;
	private JButton displayAll;
	private JButton newSubmission;
	private JButton search;
	public static BufferedImage image = null;
	private JFileChooser fileDialog;
	private int currentx = 0;
	private int currenty = 0;
	private JPanel pane = new JPanel();
	private DBThreadedCreator dbCreator = new DBThreadedCreator();

	public MainWindow() {
		Thread t = new Thread(dbCreator);
		t.start();
		initComponents();
		try {
			image = ImageIO.read(getClass().getResource("/img/CRFG.png"));
		} catch (IOException e) {
		}
		super.setIconImage(image);
		super.setTitle("Control Panel");
		super.pack();
		super.setResizable(false);
		super.setVisible(true);
	}

	private void initComponents() {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth = 2;
		constraints.insets = new Insets(0, 0, 0, 0);
		pane.setLayout(new GridBagLayout());
		fileDialog = new JFileChooser(Run.incompleteDir);

		JLabel titleText = new JLabel("CR Field Guide Submission Control Panel");
		titleText.setFont(new Font("Tahoma", 1, 14));
		addComp(titleText, constraints, 0, 1);

		constraints.insets = new Insets(5, 0, 0, 0);

		newSubmission = new JButton("New Submission");
		newSubmission.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				newSubmission();
			}
		});
		constraints.gridwidth = 1;
		addComp(newSubmission, constraints, 1, 0);

		search = new JButton("Search");
		search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				search();
			}
		});
		addComp(search, constraints, -currentx, 1);

		continueOld = new JButton("Load Unfinished");
		continueOld.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				continueOld();
			}
		});
		addComp(continueOld, constraints, 1, 0);

		displayAll = new JButton("Display All");
		displayAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				displayAll();
			}
		});
		addComp(displayAll, constraints, 0, 0);

		super.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		super.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				JFrame tempFrame = new JFrame();
				int n = JOptionPane.showConfirmDialog(tempFrame, "Are you sure? This will close all windows.", "Exit?", JOptionPane.YES_NO_OPTION, 0, (Icon) Run.i);
				tempFrame.dispose();
				if (n == JOptionPane.YES_OPTION)
					onExit();
			}
		});
		super.add(pane);
	}

	private void onExit() {
		File c = new File(Run.tempDir + "");
		if(c.exists()) Run.deleteContents(c);
		super.dispose();
		System.exit(0);
	}

	private void newSubmission() {
		new Submission();
	}                                             

	private void search() {
		new Search();
	}                                      

	private void displayAll() {
		ConnectDB DBV2 = dbCreator.getAccess();
		while (DBV2 == null) {
			try {
				Thread.sleep(200);
				DBV2 = dbCreator.getAccess();
			} catch (InterruptedException e) {}
		}
		new SearchResults(DBV2.displayAll(Arrays.onlyValidDBTypes()));
	}                                          

	private void continueOld() {
		fileDialog.setFileFilter(new IncompleteFilter());
		JFrame jf = new JFrame();
		jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		fileDialog.setCurrentDirectory(new File(Run.incompleteDir));
		fileDialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int returnVal = fileDialog.showOpenDialog(jf);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fileDialog.getSelectedFile();
			new Submission(f);
		}	
	}

	private void addComp(JComponent c, GridBagConstraints GBC, int x, int y) {
		GBC.gridx = currentx;
		currentx += x;
		GBC.gridy = currenty;
		currenty += y;
		pane.add(c, GBC);
	}

}
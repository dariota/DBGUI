package GUI;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import fileFilter.ImageFilterPng;
import backend.Arrays;
import backend.ConnectDB;
import backend.DBThreadedCreator;
import backend.ErrorHandler;
import backend.S3FileIO;
import backend.ValueHolder;
import backend.ZipUnzipIt;
import backend.Run;

@SuppressWarnings("serial")
public class Submission extends JFrame {

	private JFrame frame;
	private ImageIcon i;
	private ConnectDB dbInterface = null;
	private S3FileIO s3Interface = new S3FileIO();
	private JFileChooser fileDialog;
	private byte[] rangeMap = null;
	private GridBagConstraints labelConstraints = new GridBagConstraints();
	private GridBagConstraints selectConstraints = new GridBagConstraints();
	private GridBagConstraints fieldConstraints = new GridBagConstraints();
	private GridBagConstraints viewerConstraints = new GridBagConstraints();
	private Container pane;
	@SuppressWarnings("unchecked")
	private JComboBox<String>[] dropdowns = new JComboBox[8]; 
	private JTextField[] fields = new JTextField[14];
	private JTextArea[] largeFields = new JTextArea[2];
	private JButton[] buttons = new JButton[5];
	private JLabel[] labels = new JLabel[20];
	private JCheckBox[] checkboxes = new JCheckBox[6];
	private ArrayList<ValueHolder> infoKeyValues = new ArrayList<>();
	private int currentx = 0;
	private int currenty = 0;
	private char[] chars = new char[53];
	private int[] charWidths = new int[53];
	private String table;
	private String id = null;
	private Thread t;
	private Thread s3Thread;
	private DBThreadedCreator dbCreator = new DBThreadedCreator();
	private String errSource = "NS";


	private Submission(boolean finalise) {
		super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		t = new Thread(dbCreator);
		t.start();
		i = Run.i;
		s3Thread = new Thread(s3Interface);
		textWidth();
		super.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				onExit();
			}
		});
		super.setIconImage(MainWindow.image);
		setConstraints();
		initComponents();
		if (finalise) {
			super.pack();	
			super.setResizable(false);
			super.setVisible(true);
		}
	}
	/**
	 * Creates a new, blank submission window
	 */
	public Submission() {
		this(true);
		File c = new File(Run.tempDir);
		if(c.exists()) 
			Run.deleteContents(c);
	}

	/**
	 * Creates a submission window with fields/info populated from the file.
	 * @param f
	 * File of saved data
	 */
	public Submission(File f) {
		this(false);
		fillComponentsFromSaved(f);
		super.pack();
		super.setResizable(false);
		super.setVisible(true);
	}

	/**
	 * Creates a submission window from search results return.
	 * @param fieldInfo
	 * Map of String to Attribute Value containing the info for fields to be filled in.
	 * @param doSrcUp
	 * Whether to upload sources when updating the info
	 */
	public Submission(Map<String, AttributeValue> fieldInfo, boolean doSrcUp) {
		this(false);
		this.id = fieldInfo.get("Animal ID").getS();
		fillComponentsFromKVPairs(fieldInfo, doSrcUp);
		buttons[4].setText("Update");
		buttons[4].setActionCommand(Boolean.toString(doSrcUp));
		dropdowns[0].setEnabled(false);
		super.pack();
		super.setResizable(false);
		super.setVisible(true);
	}

	private void fillComponentsFromSaved(File saved) {
		Scanner s = null;
		String name = saved.getName().split(".txt")[0];
		ZipUnzipIt zui = new ZipUnzipIt();
		zui.unzipIt(Run.incompleteDir + "/sources_" + name + ".zip", "sources_" + name);
		Run.sm.currSrcLoc = Run.tempDir + "unzip/sources_" + name + "/";
		File map = new File(Run.sm.currSrcLoc + "locMap.png");	
		if(map.exists()) {	
			fileDialog.setSelectedFile(map);
			try {
				FileInputStream fis = new FileInputStream(map);
				rangeMap = new byte[(int) map.length()];
				fis.read(rangeMap);
				fis.close();
			} catch (IOException e) {
				ErrorHandler.error(e, errSource);
			}
		}
		try {
			s = new Scanner(saved);
			s.useDelimiter("\\Z");
		} catch (FileNotFoundException e) {
			ErrorHandler.error(e, errSource);
		}
		String fileInfo = "";
		while (s.hasNext()) //should only be needed once, but caused problems before
			fileInfo += s.next();
		String[] contents = fileInfo.split("`");
		HashMap<String, String> fieldsByLabel = new HashMap<>();

		String splitter = ":\\{";
		for (String str : contents) {
			String[] c = str.split(splitter);
			c[1] = c[1].replaceAll("}", "");
			fieldsByLabel.put(c[0], c[1]);
		}

		dropdowns[0].setSelectedItem(fieldsByLabel.get("Type"));

		for (ValueHolder vh : infoKeyValues) {
			vh.setValue(fieldsByLabel.get(vh.key()));
		}
	}

	private void fillComponentsFromKVPairs(Map<String, AttributeValue> pairs, boolean submitSources) {
		String type = pairs.get("Table").getS();
		String name = type + "_" + id;
		ZipUnzipIt zui = new ZipUnzipIt();
		if (submitSources) {
			zui.unzipIt(Run.tempDir + "downloaded/" + name, "sources_" + name);
			Run.sm.currSrcLoc = Run.tempDir + "unzip/sources_" + name + "/";
			File map = new File(Run.sm.currSrcLoc + "locMap.png");	
			if(map.exists()) {	
				fileDialog.setSelectedFile(map);
				try {
					FileInputStream fis = new FileInputStream(map);
					rangeMap = new byte[(int) map.length()];
					fis.read(rangeMap);
					fis.close();
				} catch (IOException e) {
					ErrorHandler.error(e, errSource);
				}
			}
		}

		dropdowns[0].setSelectedItem(type);

		for (ValueHolder vh : infoKeyValues) {
			try {
				vh.setValue(pairs.get(vh.key()).getS());
			} catch (NullPointerException e) {
			}
		}
	}

	private void setConstraints() {
		labelConstraints.fill = GridBagConstraints.HORIZONTAL;
		labelConstraints.anchor = GridBagConstraints.LINE_START;
		labelConstraints.gridwidth = 1;
		Insets ins = new Insets(0, 5, 0, 5);
		labelConstraints.insets = fieldConstraints.insets = viewerConstraints.insets = ins;
		selectConstraints.gridwidth = 1;
		selectConstraints.anchor = GridBagConstraints.LINE_END;
		selectConstraints.fill = GridBagConstraints.HORIZONTAL;
		viewerConstraints.gridwidth = 2;
		viewerConstraints.gridheight = 2;
		viewerConstraints.fill = GridBagConstraints.BOTH;
		fieldConstraints.anchor = GridBagConstraints.LINE_END;
	}

	private void textWidth() {
		String st = "a,7;b,8;c,6;d,8;e,7;f,5;g,8;h,8;i,3;j,4;k,7;l,3;m,11;n,8;o,8;p,8;q,8;r,5;s,6;"
				+ "t,6;u,8;v,7;w,11;x,7;y,7;z,6; ,4;A,9;B,8;C,8;D,9;E,7;F,6;G,8;H,9;I,5;J,6;K,8;L,7;"
				+ "M,11;N,8;O,9;P,8;Q,9;R,9;S,8;T,7;U,8;V,8;W,12;X,8;Y,7;Z,7;";
		String[] sa = st.split(";");
		int i = 0;
		for(String s : sa) {
			chars[i] = s.split(",")[0].toCharArray()[0];
			charWidths[i] = Integer.parseInt(s.split(",")[1]);
			i++;
		}
	}

	private void addComp(JComponent c, GridBagConstraints GBC, int x, int y) {
		GBC.gridx = currentx;
		currentx += x;
		GBC.gridy = currenty;
		currenty += y;
		pane.add(c, GBC);
	}

	private void addValueComp(JComponent value, String key, GridBagConstraints GBC, int x, int y) {
		value.setName(key);
		infoKeyValues.add(new ValueHolder(key, value));
		GBC.gridx = currentx;
		currentx += x;
		GBC.gridy = currenty;
		currenty += y;
		pane.add(value, GBC);
	}

	private void initComponents() {
		Run.sm.currSrcLoc = Run.user + "/temp/sources/";
		fileDialog = new JFileChooser();
		fileDialog.setFileFilter(new ImageFilterPng());

		pane = new JPanel();
		super.add(pane);
		super.setTitle("New Submission");
		pane.setLayout(new GridBagLayout());
		labels = new JLabel[22];
		labels[0] = new JLabel("Type");
		labels[0].setFont(new Font("Tahoma", 1, 12));
		ArrayList<String> labelContents = Arrays.fieldLabels();

		//creates labels, sets text and font
		for(int j = 1; j < labels.length; j++) {
			labels[j] = new JLabel(labelContents.get(j));
			labels[j].setFont(new Font("Tahoma", 1, 12));
		}

		//creates comboboxes
		dropdowns[0] = new JComboBox<>(new DefaultComboBoxModel<>(Arrays.types()));
		dropdowns[1] = new JComboBox<>(new DefaultComboBoxModel<>(new String[] {""}));
		dropdowns[2] = new JComboBox<>(new DefaultComboBoxModel<>(Arrays.validPatterns()));
		dropdowns[3] = new JComboBox<>(new DefaultComboBoxModel<>(Arrays.prevalence()));
		for (int i = 4; i < 7; i++) {
			dropdowns[i] = new JComboBox<>(new DefaultComboBoxModel<>(Arrays.getColours()));
		}
		dropdowns[7] = new JComboBox<>(new DefaultComboBoxModel<>(Arrays.conservationStatus()));

		//creates blank text fields
		for(int j = 0; j < fields.length; j++) {
			fields[j] = new JTextField();
			fields[j].setPreferredSize(new Dimension(100, 23));
			fields[j].setMinimumSize(new Dimension(100, 23));
		}
		fields[4].setEnabled(false);

		//creates text areas, sets their viewer and formats both
		JScrollPane[] viewers = new JScrollPane[largeFields.length];
		for(int j = 0; j < largeFields.length; j++) {
			largeFields[j] = new JTextArea();
			largeFields[j].setColumns(20);
			largeFields[j].setRows(5);
			largeFields[j].setLineWrap(true);
			largeFields[j].setWrapStyleWord(true);
			viewers[j] = new JScrollPane();
			viewers[j].setPreferredSize(new Dimension(223, 83));
			viewers[j].setMinimumSize(new Dimension(223, 83));
			viewers[j].setViewportView(largeFields[j]);
			viewers[j].setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			viewers[j].setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		}

		String[] buttonText = { "Map", "View", "Sources", "Save", "Submit"};
		for(int j = 0; j < buttons.length; j++) {
			buttons[j] = new JButton(buttonText[j]);
		}

		buttons[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					addRangeMap();
				} catch (IOException e) {
					ErrorHandler.error(e, errSource);
				}
			}
		});

		try {
			fileDialog.getSelectedFile().getName();
		} catch (NullPointerException e) {
			buttons[1].setEnabled(false);
		}

		buttons[1].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					JFrame jf = new JFrame();
					ImagePanel imPan = new ImagePanel(fileDialog.getSelectedFile());
					jf.add(imPan);
					jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					jf.setMinimumSize(new Dimension(imPan.w, imPan.h));
					jf.setPreferredSize(new Dimension(imPan.w, imPan.h));
					jf.setTitle(fileDialog.getSelectedFile().getName());
					jf.pack();
					jf.setVisible(true);
				} catch (NullPointerException e) {
					buttons[1].setEnabled(false);
				}
			}
		});

		buttons[2].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				manageSources();
			}
		});

		buttons[3].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					saveContents();
				} catch (HeadlessException | IOException e) {
					ErrorHandler.error(e, errSource);
				}
			}
		});

		buttons[4].setEnabled(false);
		buttons[4].setActionCommand("true");
		buttons[4].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					submitContents(evt);
				} catch (IOException e) {
					ErrorHandler.error(e, errSource);
				}
			}
		});

		dropdowns[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				typeSelected();
			}
		});

		dropdowns[1].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				subtypeSelected();
			}
		});

		int currentSelect = 0;
		int currentField = 0;
		int currentViewer = 0;
		int currentLabel = 0;
		labelConstraints.insets = new Insets(0, 5, 0, 5);

		//GUI layout is the worst thing ever
		addComp(labels[currentLabel++], labelConstraints, 1, 0); //type
		addComp(dropdowns[currentSelect++], selectConstraints, 1, 0); //typeSelect
		addComp(labels[currentLabel++], labelConstraints, 1, 0); //English Name
		addValueComp(fields[currentField++], labels[currentLabel - 1].getText(), fieldConstraints, -currentx, 1);
		addComp(labels[currentLabel++], labelConstraints, 1, 0); //subtype
		addValueComp(dropdowns[currentSelect++], labels[currentLabel - 1].getText(), selectConstraints, 1, 0); 
		addComp(labels[currentLabel++], labelConstraints, 1, 0); //Spanish Name
		addValueComp(fields[currentField++], labels[currentLabel - 1].getText(), fieldConstraints, -currentx, 1);
		addComp(labels[currentLabel++], labelConstraints, 1, 0); //pattern
		addValueComp(dropdowns[currentSelect++], labels[currentLabel - 1].getText(), selectConstraints, 1, 0);
		addComp(labels[currentLabel++], labelConstraints, 1, 0); //Latin Name
		addValueComp(fields[currentField++], labels[currentLabel - 1].getText(), fieldConstraints, -currentx, 1);
		addComp(labels[currentLabel++], labelConstraints, 1, 0); //Abundancy
		addValueComp(dropdowns[currentSelect++], labels[currentLabel - 1].getText(), selectConstraints, 1, 0);
		addComp(labels[currentLabel++], labelConstraints, 1, 0); //Other English
		addValueComp(fields[currentField++], labels[currentLabel - 1].getText(), fieldConstraints, -currentx, 1);
		addComp(labels[currentLabel++], labelConstraints, 1, 0); //Wingspan
		addValueComp(fields[currentField++], labels[currentLabel - 1].getText(), fieldConstraints, 1, 0);
		addComp(labels[currentLabel++], labelConstraints, 1, 0); //Other Spanish
		addValueComp(fields[currentField++], labels[currentLabel - 1].getText(), fieldConstraints, -currentx, 1);
		addComp(labels[currentLabel++], labelConstraints, 1, 0); //Reproduction
		addValueComp(fields[currentField++], labels[currentLabel - 1].getText(), fieldConstraints, 1, 0);
		addComp(labels[currentLabel++], labelConstraints, 1, 0); //Size
		addValueComp(fields[currentField++], labels[currentLabel - 1].getText(), fieldConstraints, -currentx, 1);
		addComp(labels[currentLabel++], labelConstraints, 1, 0); //Longevity
		addValueComp(fields[currentField++], labels[currentLabel - 1].getText(), fieldConstraints, 1, 0);
		addComp(labels[currentLabel++], labelConstraints, 1, 0); //Weight
		addValueComp(fields[currentField++], labels[currentLabel - 1].getText(), fieldConstraints, -currentx, 1);
		addComp(labels[currentLabel++], labelConstraints, 1, 0); //Diet
		addValueComp(fields[currentField++], labels[currentLabel - 1].getText(), fieldConstraints, 1, 0);
		addComp(labels[currentLabel++], labelConstraints, 1, 0); //Range
		addValueComp(fields[currentField++], labels[currentLabel - 1].getText(), fieldConstraints, -currentx, 1);
		addComp(labels[currentLabel++], labelConstraints, 1, 0); //Sociability
		addValueComp(fields[currentField++], labels[currentLabel - 1].getText(), fieldConstraints, 1, 0);
		addComp(labels[currentLabel++], labelConstraints, 1, 0); //Elevation
		addValueComp(fields[currentField++], labels[currentLabel - 1].getText(), fieldConstraints, -currentx, 1);
		addComp(labels[currentLabel++], labelConstraints, 2, 0); //Colours
		addComp(labels[currentLabel++], labelConstraints, -currentx, 1); //Conservation Status
		int k = findLargest(dropdowns[currentSelect].getModel()) + 32; //used to set the colour selects size
		selectConstraints.gridwidth = 2;
		selectConstraints.fill = GridBagConstraints.NONE;
		for (int i = 0; i < 3; i++) {
			Insets ins = new Insets(0, 0, 0, k * i);
			selectConstraints.insets = ins;
			addValueComp(dropdowns[currentSelect++], "Colour " + (3 - i), selectConstraints, 0, 0);
		}
		selectConstraints.fill = GridBagConstraints.HORIZONTAL;
		selectConstraints.insets = new Insets(0, 5, 0, 5);
		currentx = 2;
		addValueComp(dropdowns[currentSelect++], labels[currentLabel-1].getText(), selectConstraints, -currentx, 1); //Conservation Select
		String[] checkboxText = {"Diurnal", "Nocturnal", "Terrestrial", "Arboreal", "Complete", "Aquatic"};
		for (int i = 0; i < checkboxes.length; i++) {
			checkboxes[i] = new JCheckBox(checkboxText[i]);
		}
		int currentCheck = 0;
		int currentButton = 0;
		addValueComp(checkboxes[currentCheck++], checkboxes[currentCheck - 1].getText(), labelConstraints, 1, 0); //Diurnal
		addValueComp(checkboxes[currentCheck++], checkboxes[currentCheck - 1].getText(), labelConstraints, 1, 0); //Nocturnal
		addComp(buttons[currentButton++], labelConstraints, 1, 0); //Map
		addComp(buttons[currentButton++], labelConstraints, -currentx, 1); //View
		addValueComp(checkboxes[currentCheck++], checkboxes[currentCheck - 1].getText(), labelConstraints, 1, 0); //Terrestrial
		addValueComp(checkboxes[currentCheck++], checkboxes[currentCheck - 1].getText(), labelConstraints, 1, 0); //Arboreal
		addComp(buttons[currentButton++], labelConstraints, 1, 0); //Sources
		addValueComp(checkboxes[currentCheck++], checkboxes[currentCheck - 1].getText(), labelConstraints, -currentx, 1); //Complete
		addValueComp(checkboxes[currentCheck++], checkboxes[currentCheck - 1].getText(), labelConstraints, 0, 1); //Aquatic
		addComp(labels[currentLabel++], labelConstraints, 2, 0); //Did You Know
		addComp(labels[currentLabel++], labelConstraints, -currentx, 1); //Comment
		largeFields[currentViewer].setName(labels[currentLabel - 2].getText());
		infoKeyValues.add(new ValueHolder("Did You Know", largeFields[currentViewer]));
		addComp(viewers[currentViewer++], viewerConstraints, 2, 0); //Did You Know viewer
		largeFields[currentViewer].setName(labels[currentLabel - 1].getText());
		infoKeyValues.add(new ValueHolder("Comment", largeFields[currentViewer]));
		addComp(viewers[currentViewer++], viewerConstraints, -currentx, 2); //Comment viewer
		addComp(buttons[currentButton++], labelConstraints, 2, 0); //Save
		addComp(buttons[currentButton++], labelConstraints, 0, 0); //Submit
	}

	protected void addRangeMap() throws IOException {
		JFrame jf = new JFrame();
		jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		int returnVal = fileDialog.showOpenDialog(jf);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			buttons[1].setEnabled(true);
			File t = fileDialog.getSelectedFile();
			FileInputStream fis = new FileInputStream(t);
			rangeMap = new byte[(int) t.length()];
			fis.read(rangeMap);
			fis.close();
		}
	}

	protected void saveContents() throws HeadlessException, IOException { 
		File n = new File(Run.incompleteDir + "s.txt");
		n.mkdirs();
		n.delete(); 
		File tmp = new File(Run.sm.currSrcLoc);
		if(!tmp.exists()) {
			tmp.mkdirs();
		}
		File loc = new File(Run.sm.currSrcLoc + "locMap.png");
		if(rangeMap != null) {
			try{
				FileOutputStream fos = new FileOutputStream(loc);
				fos.write(rangeMap);
				fos.flush();
				fos.close();
			} catch (IOException e) {
				ErrorHandler.error(e, errSource);
			}
		}
		if (validSources(tmp)) {
			String filename = JOptionPane.showInputDialog("File name? (no extension)");
			if(filename != null) {
				File fi = new File(Run.incompleteDir + filename + ".txt");
				File fil = new File(Run.incompleteDir + "sources_" + filename + ".zip");
				fi.delete(); fil.delete();
				Writer output = new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream(Run.incompleteDir + filename + ".txt"), "UTF-8"));
				StringBuilder content = new StringBuilder();
				content.append("Type:{" + (String)dropdowns[0].getSelectedItem() + "}`");
				for(ValueHolder vh : infoKeyValues) {
					content.append(vh.key() + ":{" + vh.attribute().getS() + "}`");
				}
				char[] out = content.toString().toCharArray();
				output.write(out);
				output.flush();
				output.close();

				File src = new File(Run.sm.currSrcLoc);
				if(src.exists()) {
					ZipUnzipIt zui = new ZipUnzipIt();
					if(zui.zip(Run.sm.currSrcLoc, "/sources_" + filename)) {
						File from = new File(Run.tempDir + "zips/sources_" + filename + ".zip");
						File to = new File(Run.incompleteDir + "sources_" + filename + ".zip");
						Files.copy(from.toPath(), to.toPath());
					}
				}
				Run.deleteContents(new File(Run.tempDir + ""));
				super.dispose();
			}
		}
	}

	protected void typeSelected() { //enables/disables submission and sets subtype box contents
		if(dropdowns[0].getSelectedIndex() > 0) {
			buttons[4].setEnabled(true);
		} else {
			buttons[4].setEnabled(false);
		}
		dropdowns[1].setModel(new DefaultComboBoxModel<>(Arrays.getSubtypes(dropdowns[0].getSelectedIndex())));
		subtypeSelected();
	}

	private void subtypeSelected() {
		if(dropdowns[0].getSelectedItem().equals("Bird") || 
				dropdowns[1].getSelectedItem().equals("Bats")) {
			fields[4].setEnabled(true);
		} else {
			fields[4].setText("");
			fields[4].setEnabled(false);
		}
	}

	private void manageSources() {
		Run.sm.refresh();
	}
	
	private boolean validSources(File sourceDirectory) {
		String[] l = sourceDirectory.list();
		boolean hasMap = false;
		boolean hasOtherSources = false;
		for (String s : l) {
			if (s == "locMap.png")
				hasMap = true;
			else 
				hasOtherSources = true;
			if (hasMap && hasOtherSources) 
				break;
		}
		if(!(hasMap && hasOtherSources)) {
			String description = null;
			if(!hasMap && !hasOtherSources) {
				description = "no sources";
			}
			if(hasMap && !hasOtherSources) {
				description = "only a map";
			}
			if(!hasMap && hasOtherSources) {
				description = "images/books/sites only";
			}
			int q = JOptionPane.showConfirmDialog(
					frame, "Did you mean to add " + description + "?",
					"Missing Sources!", JOptionPane.YES_NO_OPTION);
			return q == JOptionPane.YES_OPTION;
		} else {
			return true;
		}
	}

	private void submitContents(ActionEvent evt) throws IOException {
		for (int j = infoKeyValues.size() - 1; j > -1; j--) {
			ValueHolder vh = infoKeyValues.get(j);
			if (vh.attribute().getS().equals("")) {
				infoKeyValues.remove(j);
			}
		}

		boolean doSrcUp = Boolean.parseBoolean(evt.getActionCommand());
		File tmp = new File(Run.tempDir + "sources/");
		if(!tmp.exists()) {
			tmp.mkdirs();
		}
		File loc = new File(Run.sm.currSrcLoc + "locMap.png");
		if(rangeMap != null) {
			try{
				FileOutputStream fos = new FileOutputStream(loc);
				fos.write(rangeMap);
				fos.flush();
				fos.close();
			} catch (IOException e) {
				ErrorHandler.error(e, errSource);
			}
		} else {
			loc.delete();
		}
		
		if (validSources(tmp)) {
			//makes insert array
			table = ((String) dropdowns[0].getSelectedItem());
			boolean submitted = true;
			try {
				while(dbInterface == null) {
					dbInterface = dbCreator.getAccess();
					if (dbInterface == null) {
						Thread.sleep(200);
					}
				}
				if(id == null) {
					id = dbInterface.latestID(table);
					infoKeyValues.add(new ValueHolder("Animal ID", new JTextField(id)));
					dbInterface.insertInto(table, infoKeyValues);
					dbInterface.incrementID(table, Integer.parseInt(id));
				} else {
					dbInterface.update(table, infoKeyValues, id);
				}
				JOptionPane.showMessageDialog(frame,
						"Successfully submitted info!",
						"Success!",
						JOptionPane.INFORMATION_MESSAGE,
						i);
			} catch (Exception e) {
				submitted = false;
				ErrorHandler.error(e, errSource);
			}
			if(submitted && doSrcUp) {
				ZipUnzipIt zui = new ZipUnzipIt();
				if(zui.zip(Run.sm.currSrcLoc, table + "_" + id)) {
					File toUp = new File(Run.tempDir + "zips/" + table + "_" + id + ".zip");
					s3Interface.animalName = fields[0].getText();
					s3Interface.toUpload = toUp;
					s3Thread.start();
				}
			}
			super.dispose();
		}
	}

	protected void onExit() {
		File c = new File(Run.tempDir);
		if(c.exists()) 
			Run.deleteContents(c);
		
		c = new File(Run.sm.currSrcLoc);
		if(c.exists()) 
			Run.deleteContents(c);
		
		if(dbInterface != null) {
			dbInterface.close();
			dbInterface = null;
		}
		
		s3Interface.close();
	}

	private int getTextLength(String text) {
		int w = 0;
		for(char c : text.toCharArray()) {
			int i = 0;
			while(c != chars[i]) {
				i++;
			}
			w += charWidths[i];
		}
		return w + 7;	
	}

	private int findLargest(ComboBoxModel<String> model) {
		int t = 0;
		for(int j = 0; j < model.getSize(); j++) {
			if (model.getElementAt(j).length() > t) 
				t = getTextLength(model.getElementAt(j)); 
		}
		t += 10;
		return t;
	}

}
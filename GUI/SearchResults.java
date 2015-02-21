package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import listeners.InfoDisplayListener;
import listeners.LinkListener;
import listeners.UpdateListener;
import backend.Arrays;
import backend.ErrorHandler;
import backend.S3FileIO;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

//Displays search results
@SuppressWarnings("serial")
public class SearchResults extends JFrame {

	private static final int HEIGHT_MARGIN = 97, ROW_HEIGHT = 26, WIDTH_BORDER = 200;
	private String errSource = "SR";
	private GridBagConstraints constraints = new GridBagConstraints();
	private GridBagConstraints buttonConstraints = new GridBagConstraints();
	private JPanel pane;
	private int currentx = 0;
	private int currenty = 0;
	private int row = 0;

	/**
	 * Displays the result of a search
	 * @param title
	 * Title to be displayed
	 * @param contents
	 * Just pass directly from ConnectDBV2.search()
	 */
	public SearchResults(String title, ArrayList<Map<String, AttributeValue>> contents) {
		super.setTitle(title);
		super.setIconImage(MainWindow.image);
		super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		int height = HEIGHT_MARGIN + (contents.size()*ROW_HEIGHT);
		height = height > 800 ? 800 : height;
		int width = (int) (GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0]
									.getDefaultConfiguration().getBounds().getWidth() - WIDTH_BORDER);
		super.setPreferredSize(new Dimension(width, height));
		pane = new JPanel();
		JScrollPane viewer = new JScrollPane(pane);
		viewer.getVerticalScrollBar().setUnitIncrement(16);
		super.add(viewer);
		setUp();
		for (Map<String, AttributeValue> animalInfo : contents) {
			addContents(animalInfo, title);
		}
		super.pack();
		super.setVisible(true);
	}
	
	/**
	 * Displays results of an "All Animals" call, displaying headers to separate types
	 * @param results
	 * Just pass directly from ConnectDBV2.displayAll()
	 */
	public SearchResults(ArrayList<SimpleEntry<String, ArrayList<Map<String, AttributeValue>>>> results) {
		super.setTitle("All Animals");
		super.setIconImage(MainWindow.image);
		super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		int contentSize = 0;
		for (SimpleEntry<String, ArrayList<Map<String, AttributeValue>>> typeGroup : results) {
			contentSize++; //header has a row to itself
			contentSize += typeGroup.getValue().size();
		}
		int height = HEIGHT_MARGIN + (contentSize * ROW_HEIGHT);
		height = height > 800 ? 800 : height;
		int width = (int) (GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0]
				.getDefaultConfiguration().getBounds().getWidth() - WIDTH_BORDER);
		super.setPreferredSize(new Dimension(width, height));
		pane = new JPanel();
		JScrollPane viewer = new JScrollPane(pane);
		viewer.getVerticalScrollBar().setUnitIncrement(16);
		super.add(viewer);
		setUp();
		for (SimpleEntry<String, ArrayList<Map<String, AttributeValue>>> typeGroup : results) {
			JLabel jl = new JLabel(typeGroup.getKey());
			String type = typeGroup.getKey();
			jl.setFont(new Font("Tahoma", 1, 14));
			addComp(jl, constraints, 0, 1);
			for (Map<String, AttributeValue> animalInfo : typeGroup.getValue()) {
				addContents(animalInfo, type);
			}
		}
		super.pack();
		super.setVisible(true);
	}
	
	private void setUp() {
		pane.setLayout(new GridBagLayout());
		constraints.insets = new Insets(0, 5, 0, 5);
		constraints.anchor = GridBagConstraints.LINE_START;
		buttonConstraints.anchor = GridBagConstraints.CENTER;
		GridBagConstraints sizeLabelConstraint = new GridBagConstraints();
		sizeLabelConstraint.gridwidth = 10;
		sizeLabelConstraint.anchor = GridBagConstraints.LINE_START;
		JLabel sizing = new JLabel("Size: " + (new S3FileIO()).dbSize());
		addComp(sizing, sizeLabelConstraint, 0, 1);
		
		for (String s : Arrays.columnsAsArray()) {
			if (s.equals("Animal ID")) 
				s = "Source File";
			JLabel jl = new JLabel(s);
			jl.setFont(new Font("Tahoma", 1, 14));
			jl.setPreferredSize(new Dimension(s.length() * 10, ROW_HEIGHT));
			addComp(jl, constraints, 1, 0);
		}
		
		currenty++;
		currentx = 0;
	}

	private void addContents(Map<String, AttributeValue> animalInfo, String sourceTable) {
		Font f = new Font("Tahoma", 0, 12);
		Border buttonBorder = BorderFactory.createRaisedSoftBevelBorder();
		boolean complete = animalInfo.get("Complete").getS().equalsIgnoreCase("true");
		Color colour = complete ? Color.BLACK : (row % 2 == 0 ? Color.RED : Color.BLUE);
		for (String column : Arrays.columnsAsArray()) {
			if (!(column.equals("Did You Know") || column.equals("Animal ID"))) {
				String fill = animalInfo.containsKey(column) ? animalInfo.get(column).getS() : "";
				JLabel content = new JLabel(fill);
				content.setFont(f);
				content.setForeground(colour);
				addComp(content, constraints, 1, 0);
			} else if (column.equals("Animal ID")) {
				JButton srcLink = new JButton("Source");
				try {
					srcLink.addActionListener(new LinkListener(
							new URI([REDACTED] 
										+ sourceTable + "_" + animalInfo.get(column).getS())));
				} catch (URISyntaxException e) {
					srcLink.setEnabled(false);
					ErrorHandler.error(e, errSource);
				}
				srcLink.setBorder(buttonBorder);
				addComp(srcLink, buttonConstraints, 1, 0);
			} else {
				char br = 13;
				String q = br + "";
				String newLine = new String(new char[]{13, 10});
				if (animalInfo.containsKey(column)) {
					AttributeValue fixedDYK = new AttributeValue();
				 	fixedDYK.setS(animalInfo.get(column).getS().replaceAll(q, newLine).replaceAll(newLine, "")
				 								.replaceAll(" -", newLine + "- ")); //Amazon removes line break 0xA
				 	animalInfo.put(column, fixedDYK);
				}
				JButton didYouKnow = new JButton(column);
				didYouKnow.setBorder(buttonBorder);
				didYouKnow.addActionListener(new InfoDisplayListener(animalInfo.containsKey("English Name") ? 
						animalInfo.get("English Name").getS() + ": " : "", 
						animalInfo.containsKey(column) ? animalInfo.get(column).getS() : "", column));
				addComp(didYouKnow, buttonConstraints, 1, 0);
			}
		}
		JButton update = new JButton("Update");
		AttributeValue av = new AttributeValue();
		av.setS(sourceTable);
		animalInfo.put("Table", av);
		update.addActionListener(new UpdateListener(animalInfo, true));
		update.setBorder(buttonBorder);
		addComp(update, buttonConstraints, 1, 0);
		JButton noSourceUpdate = new JButton("No Sources");
		noSourceUpdate.addActionListener(new UpdateListener(animalInfo, false));
		noSourceUpdate.setBorder(buttonBorder);
		addComp(noSourceUpdate, buttonConstraints, -currentx, 1);
		row++;
	}
	
	private void addComp(JComponent c, GridBagConstraints GBC, int x, int y) {
    	GBC.gridx = currentx;
    	currentx += x;
    	GBC.gridy = currenty;
    	currenty += y;
    	pane.add(c, GBC);
    }

}

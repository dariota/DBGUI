package GUI;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import backend.ErrorHandler;
import backend.SourceEntry;
import backend.Run;

//Allows viewing, deleting and creation of sources for the current animal
@SuppressWarnings("serial")
public class SourceManager extends JFrame {

	SourceEntry[] sources;
	private String errSource = "NSH";
	public String currSrcLoc;

	public SourceManager(String srcLoc) {
		currSrcLoc = srcLoc;
		super.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		File sourceDirectory = new File(srcLoc);
		sourceDirectory.mkdirs();
		String[] fileList = sourceDirectory.list();
		int srcCount = fileList.length;
		for (int i = 0; i < fileList.length; i++) { 
			if (fileList[i].contains(".jpg")) 
				srcCount--; //images are displayed by their metadata, in a separate file
		}
		sources = new SourceEntry[srcCount];
		int curr = 0;
		for (int i = 0; i < fileList.length; i++) {
			if (!fileList[i].contains(".jpg")) 
				sources[curr++] = new SourceEntry(srcLoc + fileList[i]);
		}

		placeInfo();
	}

	public SourceManager() {
		currSrcLoc = Run.user + "sources/";
	}

	//lays out source info
	public void placeInfo() { 
		JPanel pane = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = 0; 
		gbc.anchor = GridBagConstraints.LINE_START;

		for (int i = 0; i < sources.length; i++) { //set up source information
			SourceEntry source = sources[i];
			JLabel sourceInfo = new JLabel(source.displayText);
			pane.add(sourceInfo, gbc); gbc.gridx++;

			if (source.type == SourceEntry.PHOTO) {
				JButton viewImage = new JButton("View");
				viewImage.setActionCommand(source.photo.getAbsolutePath() + " | " + source.displayText);
				viewImage.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) { //on button press, open image viewing panel
						try {
							File imageFile = new File(ae.getActionCommand().split(" | ")[0]);
							String title = ae.getActionCommand().split(" | ")[1];
							JFrame window = new JFrame();
							ImagePanel imageViewer = new ImagePanel(imageFile);
							window.add(imageViewer);
							window.setTitle(title);
							window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
							window.pack();
							Dimension size = new Dimension(imageViewer.getWidth(), imageViewer.getHeight());
							window.setPreferredSize(size);
							window.setMinimumSize(size);
							window.setMaximumSize(size);
							window.setVisible(true);
						} catch (NumberFormatException e) {
							ErrorHandler.error(e, errSource);
						}
					}
				});
				pane.add(viewImage, gbc); gbc.gridx++;
			}

			JButton del = new JButton("Delete");
			del.setActionCommand(i + "");
			del.addActionListener(new ActionListener() { //on button press, delete source file info
				public void actionPerformed(ActionEvent e) {
					int index = Integer.parseInt(e.getActionCommand());
					SourceEntry se = sources[index];
					sources[index].delete();
					sources[index] = null;
					JOptionPane.showMessageDialog(new JFrame(),
							"Source deleted: " + se.displayText, "Deleted", 
							JOptionPane.INFORMATION_MESSAGE, null);
					refresh();
				}
			});
			pane.add(del, gbc); 
			gbc.gridx = 0; 
			gbc.gridy++;
		}

		ActionListener sourceCreatorGen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unused")
				SourceCreator sc = new SourceCreator(Integer.parseInt(e.getActionCommand()));	
			}
		};
		
		JButton newPhoto = new JButton("Add Photo");
		newPhoto.setActionCommand(SourceEntry.PHOTO + "");
		newPhoto.addActionListener(sourceCreatorGen);
		JButton newSite = new JButton("Add Site");
		newSite.setActionCommand(SourceEntry.SITE + "");
		newSite.addActionListener(sourceCreatorGen);
		pane.add(newPhoto, gbc); 
		gbc.gridx++;
		pane.add(newSite, gbc); 
		gbc.gridx++;

		JButton deleteAll = new JButton("Delete All");
		deleteAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				deleteAll(false);
			}
		});
		gbc.gridx = 0; gbc.gridy++;
		pane.add(deleteAll, gbc);

		super.add(pane);
		super.pack();
		super.setResizable(false);
		super.setVisible(true);

	}

	public void deleteAll(boolean bypass) {
		int q = JOptionPane.YES_OPTION - 1;
		if (!bypass) {
			q = JOptionPane.showConfirmDialog(new JFrame(), "Are you sure you wish to delete all sources?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
		} else {
			q = JOptionPane.YES_OPTION;
		}
		if(q == JOptionPane.YES_OPTION) {
			for (int i = 0; i < sources.length; i++) {
				sources[i].delete();
				sources[i] = null;
			}
			if (!bypass) refresh();
		}
	}

	public void refresh() {
		super.dispose();
		Run.newSourceManager();
	}

}

package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;

import fileFilter.ImageFilter;
import backend.NewSourceHandler;
import backend.Run;

//Allows selection of a photo file to be written with info given
public class PhotoSourceListener implements ActionListener {
	
	private static final int CHOOSE_FILE = 0, COMPLETE = 1;
	private JTextField[] info;
	private File photo;
	
	public PhotoSourceListener(JTextField[] info) {
		this.info = info;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch(Integer.parseInt(e.getActionCommand())) {
		case CHOOSE_FILE:
			JFileChooser jfc = new JFileChooser();
			jfc.setAcceptAllFileFilterUsed(false);
			jfc.addChoosableFileFilter(new ImageFilter());
			JFrame jf = new JFrame();
			jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			int returnVal = jfc.showOpenDialog(jf);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				photo = jfc.getSelectedFile();
			}
			break;
			
		case COMPLETE:
			NewSourceHandler.doFile(info, photo);
			Run.sm.refresh();
			break;
		}
	}

}

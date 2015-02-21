package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;

import backend.NewSourceHandler;
import backend.Run;

//Writes info for purely text based sources
public class TextSourceListener implements ActionListener {
	
	private JTextField[] info;
	
	public TextSourceListener(JTextField[] info) {
		this.info = info;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		NewSourceHandler.doFile(info);
		Run.sm.refresh();
	}

}

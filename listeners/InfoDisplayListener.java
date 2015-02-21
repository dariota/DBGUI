package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import GUI.InfoWindow;

//Displays info for particularly long winded fields, in its own window
public class InfoDisplayListener implements ActionListener {
	
	private String animalName;
	private String contentText;
	private String type;

	public InfoDisplayListener(String animalName, String contentText, String type) {
		this.animalName = animalName;
		this.contentText = contentText;
		this.type = type;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		new InfoWindow(animalName + type, contentText, false);
	}

}

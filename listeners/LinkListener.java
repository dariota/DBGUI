package listeners;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import backend.ErrorHandler;
import backend.Run;

//Opens a link in the default browser (if supported) when a button is pressed
public class LinkListener implements ActionListener {
	
	private URI uri;
	
	public LinkListener(URI uri) {
		this.uri = uri;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(uri);
			} catch (IOException e) {
				ErrorHandler.error(e, "LL");
			}
		} else {
			JOptionPane.showMessageDialog(new JFrame(),
					"This operating system does not\nsupport links.", "Error",
					JOptionPane.ERROR_MESSAGE, Run.i);
		}
	}

}

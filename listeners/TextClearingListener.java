package listeners;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

//clears text from a pre-populated textbox when it is selected
public class TextClearingListener implements FocusListener {
	
	JTextField jtf;
	
	public TextClearingListener(JTextField jtf) {
		this.jtf = jtf;
	}
	
	@Override
	public void focusLost(FocusEvent arg0) {}
	
	@Override
	public void focusGained(FocusEvent arg0) {
		jtf.setText("");
	}

}

package GUI;

import java.awt.Dimension;
import java.awt.FontMetrics;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

//Displays text content in a plain window
@SuppressWarnings("serial")
public class InfoWindow extends JFrame {

	private JScrollPane jsp = new JScrollPane();
	private JTextArea jta = new JTextArea();
	private static int BORDER = 40;
	
	public InfoWindow(String title, String content, boolean alwaysOnTop) {
		super.setAlwaysOnTop(alwaysOnTop);
		FontMetrics fm = jta.getFontMetrics(jta.getFont());
		jta.setText(content);
		jta.setLineWrap(true);
		jta.setWrapStyleWord(true);
		jta.setEditable(false);
		jta.setColumns(70);
		jsp.setViewportView(jta);
		int height = ((content).length()/70) * fm.getHeight() + BORDER;
		height = height < 90 ? 90 : (height > 400 ? 400 : height);
		super.setMinimumSize(new Dimension(jta.getColumns() * 10, height));
		super.setPreferredSize(new Dimension(jta.getColumns() * 10, height));
		super.add(jsp);
		super.setTitle(title);
		super.setIconImage(MainWindow.image);
		super.pack();
		super.setVisible(true);
		super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

}
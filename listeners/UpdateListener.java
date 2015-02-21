package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import GUI.Submission;

import backend.S3FileIO;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

//Sets up the submission window to update a submission rather than overwrite it
public class UpdateListener implements ActionListener {
	
	Map<String, AttributeValue> info;
	boolean doSrcUp;
	
	public UpdateListener(Map<String, AttributeValue> msav, boolean doSrcUp) {
		info = msav;
		this.doSrcUp = doSrcUp;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(doSrcUp) {
			S3FileIO S3FIO = new S3FileIO();
			if (!S3FIO.download(info.get("Table").getS() + "_" + info.get("Animal ID").getS())) {
				doSrcUp = false;
				JOptionPane.showMessageDialog(new JFrame(),
						"Source file download failed. Continuing\nwithout sources.");
			} else {
				System.out.println("Finished downloading");
			}
		}
		new Submission(info, doSrcUp);
	}

}

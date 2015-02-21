package backend;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JTextField;

public class NewSourceHandler { //TODO change doFile() to have unordered text fields
	
	public File photo;
	private static String errSource = "NSH";
	
	/**
	 * Writes a standard file with the sources given
	 * @param parseType
	 * The type of sources given (SITE, BOOK)
	 * @param info
	 * Array of JTextField in the order required
	 */
	public static void doFile(JTextField[] info) {
		int rank = 0;
		
		File tmp = new File(Run.tempDir + "sources/");
		if(!tmp.exists()) {
			tmp.mkdirs();
		}
		
		 //populates output string with info for source writing
		String formatted = Bibliography.formatWebsite(info[0].getText(), info[1].getText(), 
										 info[2].getText(), info[3].getText());
		while (new File(Run.tempDir + "sources/" + rank + "_site.txt").exists())
			rank++;
		
		Writer output;
		try {
			output = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(Run.tempDir + "sources/" + rank + "_site.txt"), "UTF-8"));
			output.write(formatted);
			output.flush();
			output.close();
			output = null;
			System.gc(); //known bug preventing file deletion unless gc is called and writer nulled
		} catch (IOException e) {
			ErrorHandler.error(e, errSource);
		}
	}
	
	/**
	 * Writes sources for a photo
	 * @param info
	 * JTextFields containing photo sourcing information
	 * @param photo
	 * Photo File
	 */
	public static void doFile(JTextField[] info, File photo) { //used for photo sources, same as above + writes photo
		String out = Bibliography.formatPhoto(info[0].getText(), info[1].getText());
		int rank = 0;
		
		File tmp = new File(Run.tempDir + "sources/");
		if(!tmp.exists())
			tmp.mkdirs();
		
		while (new File(Run.tempDir + "sources/" + rank + ".jpg").exists())
			rank++;

		if(photo != null) {
			try {
				File f = new File(Run.tempDir + "sources/" + rank + ".jpg");
				f.delete();
				Path path = Paths.get(photo.getAbsolutePath());
				byte[] photoData = Files.readAllBytes(path);
				FileOutputStream fos = new FileOutputStream(f);
				fos.write(photoData);
				fos.flush();
				fos.close();
				System.gc();
			} catch (IOException e) {
				ErrorHandler.error(e, errSource);
			}
		}
		
		Writer output;
		try {
			output = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(Run.tempDir + "sources/" + rank + "_img.txt"), "UTF-8"));
			output.write(out);
			output.flush();
			output.close();
			output = null;
			System.gc();
		} catch (IOException e) {
			ErrorHandler.error(e, errSource);
		}
	}

}
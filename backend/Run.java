package backend;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import GUI.InfoWindow;
import GUI.MainWindow;
import GUI.SourceManager;

//Main class, also holds values used throughout program
public class Run { 
	
	public static ImageIcon i;
	public static String user;
	public static String incompleteDir;
	public static String tempDir;
	public static String errorDir;
	public static String curVersion = "2.6";
	public static String bucket = [Redacted];
	private static String lastVersion;
	private static String errSource = "R";
	public static SourceManager sm = new SourceManager();
	public static String accessKey = null;
	public static String secretKey = null;
	
	public static void main(String[] args) throws IOException { 
		try {
			i = new ImageIcon(ImageIO.read(Run.class.getResource("/img/CRFG.png")), "a");
			user = args[0];
			accessKey = args[1];
			secretKey = args[2];
			tempDir = user + "/temp/";
			incompleteDir = user + "/incomplete/";
			errorDir = user + "/errors/";
			new MainWindow();
			File config = new File(user + "/cfg.txt");
			if(config.exists()) {
				Scanner s = null;
				try {
					s = new Scanner(config);
				} catch (FileNotFoundException e) {
					ErrorHandler.error(e, errSource);
				}
				lastVersion = s.nextLine();
				if(!curVersion.equals(lastVersion)) {
					try {
						Scanner f = new Scanner(new File("changelog.txt"));
						StringBuilder changes = new StringBuilder();
						String nl = new String(new char[] {13, 10});
						while (f.hasNextLine()) {
							changes.append(f.nextLine());
							changes.append(nl);
						}
						new InfoWindow("Changes", changes.toString(), true);
						f.close();
					
						Writer output = null;
						try {
							output = new BufferedWriter(new OutputStreamWriter(
										new FileOutputStream(Run.user + "/cfg.txt"), "UTF-8"));
						} catch (UnsupportedEncodingException | FileNotFoundException e) {
							ErrorHandler.error(e, errSource);
						}
						try {
							output.write(curVersion);
							output.flush();
							output.close();
						} catch (IOException e) {
							ErrorHandler.error(e, errSource);
						}
					} catch (IOException e) {}
				}
			} else {
				Writer output = null;
				try {
					output = new BufferedWriter(new OutputStreamWriter(
								new FileOutputStream(Run.user + "/cfg.txt"), "UTF-8"));
				} catch (UnsupportedEncodingException | FileNotFoundException e) {
					ErrorHandler.error(e, errSource);
				}
				try {
					output.write(curVersion);
					output.flush();
					output.close();
				} catch (IOException e) {
					ErrorHandler.error(e, errSource);
				}
			}
		} catch (Exception e) {
			System.out.println("Unhandled error found. Exiting.");
			ErrorHandler.error(e, errSource);
			System.exit(-1);
		}
	}

	public static void newSourceManager() {
		sm = new SourceManager(sm.currSrcLoc);
	}
	
	//Recursively deletes everything in a directory
	public static void deleteContents(File a) {
		if(a.isDirectory()) {
			for(String b : a.list()) {
				File c = new File(a.getAbsolutePath() + "/" + b);
				deleteContents(c);
			}
		}
		a.delete();
		a.deleteOnExit();
	}

}
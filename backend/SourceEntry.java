package backend;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

//Holds values for references/sources
public class SourceEntry {
	
	public static final int PHOTO = 0, SITE = 1, BOOK = 2, ERROR = -1;
	public String displayText;
	public int type;
	public File photo;
	private File srcFile;
	private String errSource = "SE";
	
	public SourceEntry(String src) {
		srcFile = new File(src);
		Scanner s = null;
		try {
			s = new Scanner(srcFile);
		} catch (FileNotFoundException e) {
			displayText = "";
			type = ERROR;
			ErrorHandler.error(e, errSource);
		}
		if (s != null) {
			if(srcFile.getName().contains("img")) {
				type = PHOTO;
				displayText = s.nextLine().replaceFirst(":", "") + "'s photo under " + 
								s.nextLine().replaceFirst(":", "");
				s.close();
				System.gc();
				photo = new File(srcFile.getAbsolutePath().replaceAll("_img.txt", ".jpg"));
			} else if (srcFile.getName().contains("site")) {
				type = SITE;
				while (s.hasNextLine())
					displayText = s.nextLine();
				s.close();
				System.gc();
			} else if (srcFile.getName().contains("book")) {
				//Book sources can't be created anymore but are handled for legacy
				type = BOOK;
				String surname = s.nextLine().replaceFirst(":", "");
				s.nextLine(); s.nextLine(); s.nextLine();
				displayText = surname + "'s" + s.nextLine().replaceFirst(":", "");
				s.close();
				System.gc();
			}
		}
	}
	
	public void delete() {
		Run.deleteContents(srcFile);
		if(photo != null) Run.deleteContents(photo);
		if (type == PHOTO) {
			photo = null;
		}
	}

}

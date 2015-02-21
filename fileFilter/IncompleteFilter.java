package fileFilter;

import java.io.File;
import javax.swing.filechooser.*;
 
//Limits file dialog to text files, the format in which incomplete submission are saved
public class IncompleteFilter extends FileFilter {
 
    public boolean accept(File f) {
        String extension = getExtension(f);
        return extension != null && extension.equals("txt");
    }
 
    //The description of this filter
    public String getDescription() {
        return "Text Files";
    }
    
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}
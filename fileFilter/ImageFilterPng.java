package fileFilter;

import java.io.File;
import javax.swing.filechooser.*;
 
//Limits file selection dialog to png files
public class ImageFilterPng extends FileFilter {
 
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
 
        String extension = getExtension(f);
        return extension != null && extension.equals("png");
    }
 
    //The description of this filter
    public String getDescription() {
        return "Images";
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
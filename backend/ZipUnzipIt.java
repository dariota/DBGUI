package backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

//Zips and unzips files for S3
public class ZipUnzipIt {
	
	private String errSource = "ZUI";
	
	//attempts to zip up a given directory, giving the zip file a given name
	public boolean zip(String toZipDir, String name) { //give folder location, not file
		boolean success = true;
		String zipDir = Run.tempDir + "zips/";
		File z = new File(zipDir);
		z.mkdirs();
		ArrayList<String> fileNames = new ArrayList<String>();
		File n = new File(toZipDir);
		String[] contents = n.list();
		for(String file : contents) {
			fileNames.add(file);
		}
		
		byte[] buffer = new byte[1024];
		
		try {
			ZipOutputStream zipWriter = new ZipOutputStream(new FileOutputStream(zipDir + name + ".zip"));
			for(String file : fileNames) {
				ZipEntry ze = new ZipEntry(file);
				zipWriter.putNextEntry(ze);
				FileInputStream in = new FileInputStream(toZipDir + file);
				
				int len;
				while ((len = in.read(buffer)) > 0) {
					zipWriter.write(buffer, 0, len);
				}
				
				in.close();
				zipWriter.closeEntry();
			}
			zipWriter.close();
		} catch (IOException e) { 
			ErrorHandler.error(e, errSource);
			success = false;
		}
		return success;
	}
	
	//Attempts to unzip a given directory
	public boolean unzipIt(String zipFile, String ID) {
		zipFile += ".zip";
		boolean success = true;
		byte[] buffer = new byte[1024];
		
		try {
			String unzipDir = Run.tempDir + "unzip/" + ID + "/";
			File outFolder = new File(unzipDir);
			outFolder.mkdirs();
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry ze = zis.getNextEntry();
			
			while(ze != null) {
				String fileName = ze.getName();
				File tempFile = new File(unzipDir + fileName);
				
				new File(tempFile.getParent()).mkdirs(); //allows nested directories
				
				FileOutputStream fos = new FileOutputStream(tempFile);
				
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				
				fos.close();
				ze = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
		} catch(Exception e) {
			if (e instanceof FileNotFoundException) System.out.println("Error: File not found: " + Run.tempDir + "unzip/" + ID + "/");
			else {
				ErrorHandler.error(e, errSource);
				success = false;
			}
		}
		return success;
	}

}
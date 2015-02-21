package backend;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

//Handles interaction with Amazon S3, threaded to allow multiple up/downloads at once
public class S3FileIO implements Runnable {
	
	private static String errSource = "S3U";
	private static DecimalFormat df = new DecimalFormat("#,##0.00");
	
	private AmazonS3Client client;
	public File toUpload;
	public String animalName;
	public int rep = 0;
	
	public S3FileIO() {
		BasicAWSCredentials bawsc = new BasicAWSCredentials(Run.accessKey, Run.secretKey);
		client = new AmazonS3Client(bawsc);
	}
	
	//Attempts to upload the file until it works or the user tells it to stop
	public boolean upload(File toUpload, String animal, int rep) {
		boolean attemptUpload = true;
		boolean success = false;
		if(rep > 1) {
			int q = JOptionPane.showConfirmDialog(new JFrame(),
		            "Submission of sources for " + animal + " has failed " + rep + " time(s). "
		            + "Try again?", "Source Upload", JOptionPane.YES_NO_OPTION);
			if (q == JOptionPane.NO_OPTION || q == -1) attemptUpload = false;
			else attemptUpload = true;
		}
		if(attemptUpload) {
			String key = toUpload.getName().split(".zip")[0];
		
			try {
				client.putObject(Run.bucket, key, toUpload);
				success = true;
			} catch (Exception e) {
				ErrorHandler.error(e, errSource);
			}
			if (!success) { 
				rep++;
				return upload(toUpload, animal, rep);
			}
			else {
				JFrame tmp = new JFrame();
				tmp.setAlwaysOnTop(true);
				JOptionPane.showMessageDialog(tmp, "Sources of " + animal + " submitted!",
						"Sources submitted", JOptionPane.INFORMATION_MESSAGE, Run.i);
				Run.deleteContents(new File(Run.tempDir + ""));
			} // returns true for fully successful upload, false for invalid file, exception if response fails (connection errors)
		}
		return success;
	}
	
	//Attemps to download a file of a given name from S3
	public boolean download(String name) { // returns true if file created in temp/downloaded/
		try {
			S3Object object = client.getObject(Run.bucket, name);
			try {
				S3ObjectInputStream content = object.getObjectContent();
				byte[] buffer = new byte[102400];
				File n = new File(Run.tempDir + "downloaded/n.txt");
				n.mkdirs();
				n.delete();
				FileOutputStream fos = new FileOutputStream(Run.tempDir + "downloaded/" + name + ".zip");
				int len;
				while ((len = content.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				content.close();
				System.gc(); //known bug with deleting files if gc is not called
			} catch (IOException e) {
				ErrorHandler.error(e, errSource);
				return false;
			}
		} catch (AmazonS3Exception e) {
			return false;
		}
		return true;
	}
	
	//Returns a String representing the size of each table in MB for display on search results
	public String dbSize() {
		HashMap<String, Long> tableToSize = new HashMap<>();
		for (String s : Arrays.onlyValidDBTypes()) {
			tableToSize.put(s, 0L);
		}
		
		for (S3ObjectSummary fileData : client.listObjects(Run.bucket).getObjectSummaries()) {
			tableToSize.put(fileData.getKey().split("_")[0], 
						tableToSize.get(fileData.getKey().split("_")[0]) + fileData.getSize());
		}
		
		StringBuilder tableSizes = new StringBuilder();
		for (Entry<String, Long> tableSizeValues : tableToSize.entrySet()) {
			if (tableSizeValues.getValue() == 0) {
				tableSizes.append(tableSizeValues.getKey() + ": 0MB   ");
			} else {
				float size = tableSizeValues.getValue() / 1048576F;
				tableSizes.append(tableSizeValues.getKey() + ": " + df.format(size) + "MB\t");
			}
		}
		return tableSizes.toString();
	}
	
	public void close() {
		client.shutdown();
	}

	@Override
	public void run() {
		upload(toUpload, animalName, rep);
	}

}

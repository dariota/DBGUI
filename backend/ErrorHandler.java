package backend;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

//Writes error files for reporting bugs
public class ErrorHandler {
	
	/**
	 * Prints the stacktrace of a given error to a file in errorDir/errSource<number>.txt
	 * @param Exception
	 * The exception thrown to be printed
	 * @param errSource
	 * A String code for the error originator, to aid in tracking down issues
	 */
	public static void error(Exception e, String errSource) {
		try {
			int code = 0;
			File dir = new File(Run.errorDir);
			dir.mkdirs();
			File out = new File(Run.errorDir + errSource + code + ".txt");
			while (out.exists()) {
				out = new File(Run.errorDir + errSource + ++code + ".txt");
			}
			System.out.println("Error in " + errSource + ", writing error file.");
			PrintStream ps = new PrintStream(out);
			e.printStackTrace(ps);
			System.out.println("Wrote error file to " + errSource + code + ".txt");
		} catch (IOException e1) {
			System.out.println("Error writing error file.");
			System.out.println("Original error: ");
			e.printStackTrace();
			System.out.println("\nFile writing error: ");
			e1.printStackTrace();
		}
	}

}

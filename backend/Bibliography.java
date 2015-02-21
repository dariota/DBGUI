package backend;

import java.util.HashMap;

//String formatting static class for sources/references
public class Bibliography {
	
	private static String newLine = new String(new char[] {13, 10});
	private static HashMap<String, String> abbreviations = new HashMap<>();
	
	//Abbreviations for input convenience
	static {
		abbreviations.put("CB", "CC-BY");
		abbreviations.put("CBS", "CC-BY-SA");
		abbreviations.put("ARR", "All Rights Reserved");
		abbreviations.put("PD", "Public Domain");
	}
	
	//Strings for convenience, as values are passed from text fields
	public static String formatWebsite(String dayRetrieved, String monthRetrieved, String yearRetrieved, 
										String sourceSite) {
		String formatted = ":" + dayRetrieved + newLine + ":" + monthRetrieved + newLine + ":" 
						+ yearRetrieved + newLine + ":";
		formatted += "http://" + sourceSite.replaceAll("http://", "").trim();
		return formatted;
	}
	
	public static String formatPhoto(String author, String licence) {
		if (licence.contains("-")) {
			licence = licence.toUpperCase();
		}
		if (abbreviations.containsKey(licence.toUpperCase()))
			licence = abbreviations.get(licence.toUpperCase());
		return ":" + capsAndTrim(author) + newLine + ":" + capsAndTrim(licence);
	}
	
	private static String capsAndTrim(String in) {
		String[] words = in.split(" ");
		String fixed = "";
		for (String s : words) {
			try {
				s = ("" + s.charAt(0)).toUpperCase() + s.substring(1, s.length());
			} catch (StringIndexOutOfBoundsException e) {
				s = s.toUpperCase();
			}
			fixed += s + " ";
		}
		fixed = fixed.trim();
		return fixed;
	}

}
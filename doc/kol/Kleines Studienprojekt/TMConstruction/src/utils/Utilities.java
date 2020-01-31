package utils;

import java.util.Arrays;

/**
 * The Class Utilities.
 */
public class Utilities {
	
	/**
	 * true if array contains the key
	 *
	 * @param arr the arr
	 * @param key the key
	 * @return true, if successful
	 */
	public static boolean contains(String[] arr, String key) {
		for (String string : arr) {
			if (key.equals(string)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Index of.
	 *
	 * @param arr the arr
	 * @param val the val
	 * @return the int
	 */
	// utils funktion die Position im array angibt
	public static int indexOf(String[] arr, String val) {
		return Arrays.asList(arr).indexOf(val);
	}
	
	/**
	 * Builds the static beginning of the tex. file
	 *
	 * @return the string
	 */
	public static String buildStaticTex() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("\\documentclass[10pt, a4paper]{article}\n");
		sb.append("\\usepackage[utf8]{inputenc}\n");
		sb.append("\\usepackage[margin=1in]{geometry}\n");
		sb.append("\\begin{document}\n");
		sb.append("\\section*{Output}\n\n");
		
		return sb.toString();
	}


}
